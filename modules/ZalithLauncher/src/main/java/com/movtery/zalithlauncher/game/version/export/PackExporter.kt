/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.game.version.export

import android.content.Context
import android.net.Uri
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.writeLocalFile
import com.movtery.zalithlauncher.coroutine.TaskFlowExecutor
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.coroutine.addTask
import com.movtery.zalithlauncher.coroutine.buildPhase
import com.movtery.zalithlauncher.game.version.export.platform.ModrinthPackExporter
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.file.zipDirectory
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.UUID

private const val TAG = "PackExporter"

/**
 * 整合包导出器
 * @param exportInfo 要导出的整合包的必要信息
 * @param scope 在有生命周期管理的scope中执行安装任务
 */
class PackExporter(
    val context: Context,
    val exportInfo: ExportInfo,
    private val scope: CoroutineScope,
) {
    private val taskExecutor = TaskFlowExecutor(scope)

    /**
     * 本次导出独占的临时目录（议题 #78）
     *
     * 以前所有导出共用 [PathManager.DIR_CACHE_MODPACK_EXPORTER] 根目录，清理时整个根目录删掉：
     * 两个导出（不同实例同时导出 / 导出过程中又点导出）会互删临时文件，
     * 生成出缺文件的压缩包。现在每次导出在根目录下用独立子目录，只清自己的。
     */
    private val exportCachePath: File = File(
        PathManager.DIR_CACHE_MODPACK_EXPORTER,
        "export_${UUID.randomUUID().toString().take(8)}"
    )
    val taskFlow: StateFlow<List<TitledTask>> = taskExecutor.tasksFlow

    //目前只保留 Modrinth 一种导出格式
    private val exporter: AbstractExporter = ModrinthPackExporter()

    /**
     * 开始导出整合包
     */
    fun startExport(
        outputUri: Uri,
        version: Version,
        isRunning: () -> Unit = {},
        onFinished: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (taskExecutor.isRunning()) {
            isRunning()
            return //正在运行中，拒绝导出
        }

        taskExecutor.executePhasesAsync(
            onStart = {
                val tasks = getTaskPhases(outputUri, version)
                taskExecutor.addPhases(tasks)
            },
            onComplete = {
                onFinished()
            },
            onError = { e ->
                onError(e)
            }
        )
    }

    private suspend fun getTaskPhases(
        outputUri: Uri,
        version: Version
    ) = withContext(Dispatchers.IO) {
        val tempPath = File(exportCachePath, "temp")
        val pack = File(exportCachePath, "${exportInfo.name}.${exporter.fileSuffix}")

        listOf(
            buildPhase {
                //清除上一次导出的缓存
                addTask(
                    id = "ExportModpack.Cleanup",
                    title = androidText(R.string.download_install_clear_temp),
                    icon = R.drawable.ic_auto_delete_outlined
                ) {
                    clearTempModPackDir()
                    tempPath.createDirAndLog()
                }

                with(exporter) {
                    buildTasks(
                        context = context,
                        version = version,
                        info = exportInfo,
                        tempPath = tempPath
                    )
                }

                addTask(
                    id = "ExportModpack.Pack",
                    title = androidText(R.string.versions_export_task_generate_pack),
                    icon = R.drawable.ic_build_outlined
                ) {
                    zipDirectory(
                        sourceDir = tempPath,
                        outputZipFile = pack,
                        preserveFileTime = false
                    )

                    context.writeLocalFile(
                        inputFile = pack,
                        outputUri = outputUri,
                        mimeType = "application/*"
                    )
                }

                addTask(
                    id = "ExportModpack.Cleanup_Finished",
                    title = androidText(R.string.download_install_clear_temp),
                    icon = R.drawable.ic_auto_delete_outlined
                ) {
                    clearTempModPackDir()
                }
            }
        )
    }

    /**
     * 清理临时整合包导出目录
     */
    private suspend fun clearTempModPackDir() = withContext(Dispatchers.IO) {
        //议题 #78：只清本次导出的子目录，绝不删 DIR_CACHE_MODPACK_EXPORTER 根目录
        exportCachePath.takeIf { it.exists() }?.let { folder ->
            FileUtils.deleteQuietly(folder)
            Logger.info(TAG, "Temporary modpack export directory cleared.")
        }
    }

    /**
     * 取消整合包导入
     */
    fun cancel() {
        taskExecutor.cancel()
    }

    private fun File.createDirAndLog(): File {
        this.mkdirs()
        Logger.debug(TAG, "Created directory: $this")
        return this
    }
}