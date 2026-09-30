/*
 * ZyNova Launcher
 * Copyright (C) 2025 ZyNova Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.download.resources

import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.getVersionByLocalFile
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.saves.unpackSaveZip
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

private const val TAG = "ResourceInstall"

/**
 * 资源安装状态
 */
enum class ResourceInstallState {
    /** 未安装 */
    NOT_INSTALLED,

    /** 已安装 */
    INSTALLED,

    /** 已安装，但存在可用的新版本 */
    UPDATE_AVAILABLE
}

/**
 * 版本匹配失败
 *
 * 26.2.2 之前，匹配失败只会返回 `null`，界面上不管是「网络查询失败」
 * 还是「实例信息读不出来」，都统一显示成一句
 * `No compatible version found for this instance`（未本地化的英文），
 * 导致用户无法判断到底是自己的实例不兼容，还是网络出了问题。
 *
 * 现在把失败原因拆开：调用方可以据此给出明确的、本地化的提示。
 */
sealed class ResourceMatchException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    /** 目标实例的版本信息不可用（版本 JSON 解析失败或实例不完整） */
    class InstanceInfoUnavailable :
        ResourceMatchException("The target instance has no readable version information")

    /** 查询资源版本时失败（网络异常、来源不可用、来源不支持该资源类型等） */
    class QueryFailed(cause: Throwable) :
        ResourceMatchException("Failed to query the resource versions: ${cause.message}", cause)

    /**
     * 没有任何与目标实例兼容的资源版本
     *
     * @param minecraftVersion 目标实例的 Minecraft 版本
     * @param loaderName 目标实例的模组加载器名称；原版实例为 null
     */
    class NoCompatibleVersion(
        val minecraftVersion: String,
        val loaderName: String?
    ) : ResourceMatchException(
        "No compatible version for $minecraftVersion / ${loaderName ?: "vanilla"}"
    )
}

/**
 * 待安装的一个资源版本
 */
data class ResourceInstallEntry(
    val version: ResourceVersion,
    /** 是否为前置依赖 */
    val isDependency: Boolean,
    /**
     * 是否为**必需**条目。
     *
     * 主资源恒为 `true`；前置依赖取它自己声明的类型。
     * 下载时用它决定「这个文件下载失败要不要中断整批」——
     * 必需前置下载失败**绝不能**像以前那样被当成可选文件静默跳过。
     */
    val required: Boolean = true
) {
    val fileName: String? get() = version.file?.fileName
}

/**
 * 一个**没能装上**的必需前置依赖
 *
 * @param provider 依赖来源
 * @param projectId 依赖项目 ID
 * @param reason 失败原因（解析不到 / 与实例不兼容 / 下载失败）
 */
data class ResourceMissingDependency(
    val provider: Platform,
    val projectId: String,
    val reason: String
) {
    val key: String get() = "$provider:$projectId"

    override fun toString(): String = "$key（$reason）"
}

/**
 * 一次安装的完整计划
 *
 * @param entries 需要下载并安装的条目（主资源 + 已解析出的前置依赖）
 * @param unresolvedRequiredDependencies 无法解析的**必需**前置依赖
 *
 * 前置依赖解析失败时不会让整次安装失败（主资源依旧可用），
 * 但也不能像以前那样静默丢弃：这里会把它记下来，由调用方提示用户，
 * 避免出现「装上了模组，进游戏却因为缺少前置而崩溃」的情况。
 */
data class ResourceInstallPlan(
    val entries: List<ResourceInstallEntry>,
    val unresolvedRequiredDependencies: List<ResourceMissingDependency> = emptyList()
) {
    val hasUnresolvedRequiredDependencies: Boolean
        get() = unresolvedRequiredDependencies.isNotEmpty()
}

/**
 * 一次安装的结果
 *
 * @param installedCount 实际落到实例目录里的文件数
 * @param missingRequiredDependencies 没能装上的**必需**前置依赖
 *        （解析不到、与实例不兼容、或下载失败）
 *
 * ⚠️ 关键点：`missingRequiredDependencies` 非空意味着**安装是残缺的**，
 * 调用方必须提示用户，否则用户以为装好了、进游戏却因为缺前置崩溃。
 */
data class ResourceInstallResult(
    val installedCount: Int,
    val missingRequiredDependencies: List<ResourceMissingDependency> = emptyList()
) {
    /** 是否有必需前置没装上 */
    val hasMissingRequiredDependencies: Boolean
        get() = missingRequiredDependencies.isNotEmpty()

    /** 汇总成一行可读文本，方便直接塞进提示 */
    fun missingDependenciesText(): String =
        missingRequiredDependencies.joinToString("\n") { it.toString() }
}

/**
 * ZyNova 统一资源安装管理器
 *
 * 统一负责「版本匹配 → 文件选择 → 下载 → 校验 → 安装」，
 * 资源页面不再各自实现下载与安装逻辑。
 */
object ResourceInstallManager {

    /**
     * 判断资源在当前实例中的安装状态（按文件名匹配）
     *
     * @param installedFileNames 当前实例该资源类别下已经安装的文件名
     * @param targetFileName 准备安装的文件名
     */
    fun installState(
        installedFileNames: Collection<String>,
        targetFileName: String?
    ): ResourceInstallState {
        if (targetFileName.isNullOrBlank()) return ResourceInstallState.NOT_INSTALLED
        return if (installedFileNames.any { it.equals(targetFileName, ignoreCase = true) }) {
            ResourceInstallState.INSTALLED
        } else {
            ResourceInstallState.NOT_INSTALLED
        }
    }

    /**
     * 判断资源在当前实例中的安装状态（按版本识别结果）
     *
     * 用于区分「已安装」与「可更新」：
     * 本地识别到的版本与最新兼容版本一致时是「已安装」，
     * 不一致时说明存在新版本，可以更新。
     *
     * @param installedVersionId 通过本地文件识别出的、已安装的版本 ID
     * @param latestVersionId 最新兼容版本的版本 ID
     */
    fun installState(
        installedVersionId: String?,
        latestVersionId: String?
    ): ResourceInstallState {
        if (latestVersionId.isNullOrBlank()) return ResourceInstallState.NOT_INSTALLED
        if (installedVersionId.isNullOrBlank()) return ResourceInstallState.NOT_INSTALLED
        return if (installedVersionId == latestVersionId) {
            ResourceInstallState.INSTALLED
        } else {
            ResourceInstallState.UPDATE_AVAILABLE
        }
    }

    /**
     * 通过本地已安装文件识别它对应的资源版本 ID
     *
     * @return 识别出的版本 ID；无法识别时返回 null
     */
    suspend fun identifyInstalledVersionId(file: File, sha1: String): String? {
        return runCatching {
            getVersionByLocalFile(file, sha1)?.platformId()
        }.getOrElse { e ->
            Logger.warning(TAG, "Failed to identify an installed resource file: ${file.name}", e)
            null
        }
    }

    /**
     * 读取某个资源类别下当前实例已经安装的文件名
     */
    fun installedFileNames(instance: Version, type: ResourceType): List<String> {
        val folder = File(instance.getGameDir(), type.classes.versionFolder.folderName)
        if (!folder.isDirectory) return emptyList()
        return folder.listFiles()?.filter { it.isFile }?.map { it.name }.orEmpty()
    }

    /**
     * 版本匹配：在资源的所有版本中挑选与当前实例兼容的最新版本
     *
     * 能自动判断，就不让用户选择。
     *
     * - 只有 Mod / 整合包才强制要求加载器匹配；
     *   资源包、光影、存档的加载器标注在来源之间并不统一，
     *   强制匹配会把本来可用的资源挡在外面。
     * - 查询失败与「确实没有兼容版本」会被区分开，不再都当成同一件事。
     *
     * @throws ResourceMatchException 匹配失败时抛出，携带具体原因
     */
    suspend fun resolveCompatibleVersion(
        provider: ResourceProvider,
        projectId: String,
        type: ResourceType,
        instance: Version
    ): ResourceVersion {
        val info = instance.getVersionInfo()
            ?: throw ResourceMatchException.InstanceInfoUnavailable()
        val loaderName = info.loaderInfo?.loader?.displayName

        val versions = try {
            provider.getVersions(projectId, type)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            throw ResourceMatchException.QueryFailed(e)
        }

        return versions
            .sortedByDescending { it.publishedAt }
            .firstOrNull { version ->
                isCompatibleWith(
                    version = version,
                    type = type,
                    minecraftVersion = info.minecraftVersion,
                    loaderName = loaderName
                )
            }
            ?: throw ResourceMatchException.NoCompatibleVersion(info.minecraftVersion, loaderName)
    }

    /**
     * 判断一个资源版本能否用在目标实例上
     *
     * 规则集中在这里，避免「主资源」与「前置依赖」用两套判断标准：
     * - 必须有可下载文件
     * - Minecraft 版本要匹配
     * - 只有需要加载器的资源类型（Mod / 整合包）才强制校验加载器；
     *   资源包、光影、存档的加载器标注在各来源之间并不统一，
     *   强制匹配会把本来可用的资源挡在外面
     */
    fun isCompatibleWith(
        version: ResourceVersion,
        type: ResourceType,
        minecraftVersion: String,
        loaderName: String?
    ): Boolean {
        if (version.file == null) return false
        if (!version.supportsGameVersion(minecraftVersion)) return false
        if (type.requiresLoader && !version.supportsLoader(loaderName)) return false
        return true
    }

    /**
     * 构建安装计划：主资源 + 必需的（递归）前置依赖
     *
     * @param type 资源类别；同时决定前置依赖按哪种资源去查询
     * @param includeOptionalDependencies 是否同时安装可选依赖
     */
    suspend fun buildInstallPlan(
        version: ResourceVersion,
        type: ResourceType,
        instance: Version,
        includeOptionalDependencies: Boolean = false
    ): ResourceInstallPlan {
        val plan = mutableListOf<ResourceInstallEntry>()
        val unresolvedRequired = mutableListOf<ResourceMissingDependency>()
        val visited = mutableSetOf<String>()

        //同一个项目只保留一份：两个模组可能各自依赖同一前置的**不同版本**，
        //两份都装进 mods 目录会直接冲突（游戏可能崩，也可能加载错的那一份）
        val addedProjects = mutableMapOf<String, ResourceVersion>()

        val info = instance.getVersionInfo()
        val minecraftVersion = info?.minecraftVersion ?: instance.getVersionName()
        val loaderName = info?.loaderInfo?.loader?.displayName

        suspend fun collect(current: ResourceVersion, isDependency: Boolean, required: Boolean) {
            val versionKey = "${current.provider}:${current.projectId}:${current.versionId}"
            if (!visited.add(versionKey)) return

            //① 该版本没有可下载文件。
            //   以前这里直接 return，导致「已经解析成功但没有文件」的必需前置
            //   既不会被安装、也不会被记录成未解析 —— 静默缺前置，进游戏才崩。
            if (current.file == null) {
                if (isDependency && required) {
                    unresolvedRequired.add(
                        ResourceMissingDependency(
                            provider = current.provider,
                            projectId = current.projectId,
                            reason = "该版本没有提供可下载的文件"
                        )
                    )
                }
                Logger.warning(
                    TAG,
                    "Dependency ${current.provider}:${current.projectId} has no downloadable file"
                )
                return
            }

            //② 同一项目已加入过 → 不再重复安装
            val projectKey = "${current.provider}:${current.projectId}"
            val existing = addedProjects[projectKey]
            if (existing != null) {
                if (existing.versionId != current.versionId) {
                    Logger.warning(
                        TAG,
                        "Project $projectKey was already planned as version ${existing.versionId}; " +
                                "skipping the duplicate version ${current.versionId} to avoid installing two copies"
                    )
                }
                return
            }

            plan.add(ResourceInstallEntry(current, isDependency, required))
            addedProjects[projectKey] = current

            current.dependencies.forEach { dependency ->
                if (!dependency.isRequired && !includeOptionalDependencies) return@forEach

                val resolved = resolveDependency(
                    dependency = dependency,
                    type = type,
                    minecraftVersion = minecraftVersion,
                    loaderName = loaderName
                )
                if (resolved == null) {
                    //前置依赖解析失败不能静默丢弃：
                    //主资源会被装上，但游戏内会因为缺少前置而崩溃，用户却不知道原因
                    Logger.warning(
                        TAG,
                        "Unresolved dependency ${dependency.provider}:${dependency.projectId} " +
                                "(required=${dependency.isRequired}) for ${current.projectId}"
                    )
                    if (dependency.isRequired) {
                        unresolvedRequired.add(
                            ResourceMissingDependency(
                                provider = dependency.provider,
                                projectId = dependency.projectId,
                                reason = "找不到与该实例（$minecraftVersion${loaderName?.let { " / $it" } ?: ""}）兼容的版本"
                            )
                        )
                    }
                } else {
                    //把「是否为必需」传下去：递归到它自己没文件时才能被正确记录
                    collect(resolved, true, dependency.isRequired)
                }
            }
        }

        collect(version, false, true)
        return ResourceInstallPlan(
            entries = plan,
            unresolvedRequiredDependencies = unresolvedRequired
        )
    }

    /**
     * 解析单个依赖对应的可用版本
     *
     * 1. 优先使用作者指定的精确版本
     * 2. 但**必须**先确认该精确版本与当前实例兼容：
     *    作者钉死的版本可能是给别的 Minecraft 版本 / 加载器构建的，
     *    直接装上去比不装更容易崩
     * 3. 不兼容或解析不到时，回退为「选择适配当前实例的最新版本」
     */
    private suspend fun resolveDependency(
        dependency: ResourceDependency,
        type: ResourceType,
        minecraftVersion: String,
        loaderName: String?
    ): ResourceVersion? {
        val provider = runCatching { ResourceProviders.of(dependency.provider) }.getOrElse { e ->
            Logger.warning(TAG, "Unknown resource provider: ${dependency.provider} (${e.message})")
            return null
        }

        dependency.versionId?.let { versionId ->
            val pinned = runCatching {
                provider.getVersionById(versionId, dependency.projectId, type)
            }.onFailure { e ->
                Logger.warning(
                    TAG,
                    "Failed to load the dependency version $versionId of ${dependency.projectId}: ${e.message}"
                )
            }.getOrNull()

            if (pinned != null) {
                if (isCompatibleWith(pinned, type, minecraftVersion, loaderName)) {
                    return pinned
                }
                Logger.warning(
                    TAG,
                    "The pinned dependency version $versionId of ${dependency.projectId} is not compatible " +
                            "with $minecraftVersion / ${loaderName ?: "vanilla"}; " +
                            "falling back to the latest compatible version"
                )
            }
        }

        return runCatching {
            val versions = provider.getVersions(dependency.projectId, type)
            versions
                .sortedByDescending { it.publishedAt }
                .firstOrNull { isCompatibleWith(it, type, minecraftVersion, loaderName) }
        }.onFailure { e ->
            Logger.warning(
                TAG,
                "Failed to resolve a compatible version for the dependency ${dependency.projectId}: ${e.message}"
            )
        }.getOrNull()
    }

    /**
     * 执行安装：下载 → 校验 → 安装
     *
     * 严格按统一流程执行，调用方只需要提供安装计划与目标实例。
     *
     * @param plan 安装计划
     * @param type 资源类型（决定安装目录）
     * @param instance 目标游戏实例
     * @param onProgress 下载进度回调
     * @return 实际安装的文件数量
     */
    suspend fun install(
        plan: List<ResourceInstallEntry>,
        type: ResourceType,
        instance: Version,
        onProgress: (ResourceDownloadProgress) -> Unit = {}
    ): ResourceInstallResult {
        if (plan.isEmpty()) return ResourceInstallResult(installedCount = 0)

        val targetFolder = File(instance.getGameDir(), type.classes.versionFolder.folderName)
        if (!targetFolder.exists() && !targetFolder.mkdirs()) {
            throw IOException("Failed to create target folder: ${targetFolder.absolutePath}")
        }

        //1. 统一交给下载管理器（并发、重试、续传、校验都在这里完成）
        val cacheDir = File(PathManager.DIR_CACHE, "resources")
        val entryByFileName = linkedMapOf<String, ResourceInstallEntry>()
        val requests = plan.mapNotNull { entry ->
            entry.version.file?.let { file ->
                entryByFileName.putIfAbsent(file.fileName, entry)
                ResourceDownloadRequest(
                    fileName = file.fileName,
                    downloadUrls = file.downloadUrls,
                    sha1 = file.sha1,
                    size = file.size,
                    //主资源下载失败必须整体失败；
                    //前置依赖失败不中断整批，但会通过 failures 如实上报（见下一步），
                    //不再像以前那样「静默跳过、界面仍显示安装成功」
                    required = !entry.isDependency
                )
            }
        }

        val outcome = ResourceDownloadManager.downloadAllDetailed(
            cacheDir = cacheDir,
            requests = requests,
            onProgress = onProgress
        )

        //2. 必需的前置依赖下载失败 → 记录为缺失，交给上层提示用户
        val missingRequired = outcome.failures.mapNotNull { failure ->
            val entry = entryByFileName[failure.request.fileName] ?: return@mapNotNull null
            if (!entry.isDependency || !entry.required) return@mapNotNull null
            ResourceMissingDependency(
                provider = entry.version.provider,
                projectId = entry.version.projectId,
                reason = "下载失败：${failure.cause?.message ?: "未知原因"}"
            )
        }

        //3. 安装到目标实例
        var installed = 0
        outcome.results.forEach { result ->
            val targetFile = File(targetFolder, result.file.name)
            if (targetFile.exists() && !targetFile.delete()) {
                throw IOException("Failed to delete the existing resource file: ${targetFile.absolutePath}")
            }
            result.file.copyTo(targetFile, overwrite = true)

            //存档资源需要解压后才能被游戏识别
            if (type == ResourceType.SAVE) {
                runCatching {
                    unpackSaveZip(targetFile, targetFolder)
                }.onFailure { e ->
                    Logger.warning(TAG, "Failed to unpack save file ${targetFile.name}: ${e.message}")
                }
                //⚠️ 解压失败时 unpackSaveZip 不会删除压缩包，
                //必须在这里兜底清掉，否则存档目录里会留下无法识别的 .zip
                FileUtils.deleteQuietly(targetFile)
            }
            installed++
        }

        //4. 清理本次的缓存文件
        outcome.results.forEach { FileUtils.deleteQuietly(it.file) }
        return ResourceInstallResult(
            installedCount = installed,
            missingRequiredDependencies = missingRequired
        )
    }
}
