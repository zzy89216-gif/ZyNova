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

package com.movtery.zalithlauncher.ui.screens.main.home_feed

import android.util.Xml
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.path.DownloadUrls
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.path.GLOBAL_JSON
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.network.httpGetJson
import com.movtery.zalithlauncher.utils.logging.Logger
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Parameters
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.StringReader
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val TAG = "HomeFeed"

/**
 * 主页「自定义主页」资讯卡片的来源
 *
 * 所有来源都是**公开且无需用户配置**（CurseForge 例外：它需要构建期写入的 API Key）：
 * - Minecraft 新闻 / Java 版本更新 / Minecraft 社区活动：Mojang 官方启动器内容与官网 RSS
 * - Modrinth 动态：Modrinth 官方开放接口
 * - CurseForge 资讯：CurseForge 官方接口（未配置 Key 时静默返回空）
 *
 * ⚠️ 这些来源都是**可失败**的：任何请求异常、断网、返回体格式变化都只能影响这一张卡片，
 * 不能抛到主页上（见 [HomeFeedRepository.load]）。
 */
enum class HomeFeedSource(val id: String, val titleRes: Int) {
    MINECRAFT_NEWS("minecraft_news", R.string.home_feed_source_minecraft_news),
    JAVA_UPDATES("java_updates", R.string.home_feed_source_java_updates),
    MODRINTH("modrinth", R.string.home_feed_source_modrinth),
    CURSEFORGE("curseforge", R.string.home_feed_source_curseforge),
    MINECRAFT_EVENTS("minecraft_events", R.string.home_feed_source_minecraft_events);

    companion object {
        fun fromId(id: String): HomeFeedSource? = entries.firstOrNull { it.id == id }
    }
}

/**
 * 一条资讯
 *
 * 只保留卡片真正要展示的字段；解析失败/缺图都允许为空，
 * 页面会退化为「无图占位 + 文字」，而不是整张卡片消失。
 */
@Serializable
data class HomeFeedItem(
    val id: String = "",
    val sourceId: String = "",
    val title: String = "",
    val summary: String = "",
    val imageUrl: String? = null,
    /** 点击卡片要打开的来源地址 */
    val url: String = "",
    /** 已格式化好的日期文本（各来源格式不一致，统一在解析时处理） */
    val dateText: String = ""
)

/** 某个来源的缓存（内存 + 磁盘共用同一份结构） */
@Serializable
private data class HomeFeedCache(
    val items: List<HomeFeedItem> = emptyList(),
    /** 落缓存的时间戳，用于判断缓存是否还有效 */
    val fetchedAt: Long = 0L
)

/** 主页资讯来源的加载器：网络请求、XML / JSON 解析、缓存全都在这个文件里 */
object HomeFeedRepository {
    /** 缓存有效期：30 分钟内不重复请求，避免每次回主页都刷一遍资讯 */
    private const val CACHE_TTL_MS = 30 * 60 * 1000L

    /** 单个来源最多保留多少条（卡片只展示前几条，留一点余量） */
    private const val MAX_ITEMS = 6

    /** 单个请求的超时：主页资讯是装饰性内容，宁可空着也不能卡住界面 */
    private const val REQUEST_TIMEOUT_MS = 10_000L

    /** 内存缓存：同一次启动内反复进出主页不再读磁盘 */
    private val memoryCache = mutableMapOf<String, HomeFeedCache>()

    /**
     * 加载某个来源的资讯
     *
     * 顺序：内存缓存 → 磁盘缓存（仍然新鲜就直接用）→ 网络请求。
     * 网络失败时**返回旧的磁盘缓存**（哪怕是过期的），没有缓存则返回空列表。
     * 这个函数不会抛异常（协程取消除外），调用方不需要 try/catch。
     *
     * @param force 忽略缓存有效期，强制刷新（下拉刷新等场景用）
     */
    suspend fun load(
        source: HomeFeedSource,
        force: Boolean = false
    ): List<HomeFeedItem> = withContext(Dispatchers.IO) {
        val cached = memoryCache[source.id]
            ?: readCache(source.id)?.also { memoryCache[source.id] = it }

        val fresh = cached != null &&
                System.currentTimeMillis() - cached.fetchedAt < CACHE_TTL_MS
        if (!force && fresh) return@withContext cached!!.items

        val fetched = runCatching {
            withTimeout(REQUEST_TIMEOUT_MS) { fetch(source) }
        }.getOrElse { e ->
            if (e is CancellationException) throw e
            Logger.debug(TAG, "Failed to fetch ${source.id}: ${e.message}")
            emptyList()
        }

        if (fetched.isEmpty()) return@withContext cached?.items ?: emptyList()

        val newCache = HomeFeedCache(
            items = fetched,
            fetchedAt = System.currentTimeMillis()
        )
        memoryCache[source.id] = newCache
        writeCache(source.id, newCache)
        newCache.items
    }

    /** 清空某个来源的缓存（管理页「刷新」或调试用） */
    fun clearCache(source: HomeFeedSource) {
        memoryCache.remove(source.id)
        runCatching { cacheFile(source.id).delete() }
    }

    // ── 各来源的抓取 ────────────────────────────────────────────────────────

    private suspend fun fetch(source: HomeFeedSource): List<HomeFeedItem> = when (source) {
        HomeFeedSource.MINECRAFT_NEWS -> fetchMinecraftNews()
        HomeFeedSource.JAVA_UPDATES -> fetchJavaUpdates()
        HomeFeedSource.MODRINTH -> fetchModrinth()
        HomeFeedSource.CURSEFORGE -> fetchCurseForge()
        HomeFeedSource.MINECRAFT_EVENTS -> fetchMinecraftEvents()
    }

    /**
     * Minecraft 新闻：官方启动器使用的 `launchercontent.mojang.com` 新闻 JSON
     *
     * v2 接口更新更及时；失败时回退到旧的 v1 地址（字段结构一致）。
     */
    private suspend fun fetchMinecraftNews(): List<HomeFeedItem> {
        val response = runCatching {
            httpGetJson<MojangNewsResponse>(MOJANG_NEWS_URL_V2)
        }.getOrElse {
            httpGetJson<MojangNewsResponse>(MOJANG_NEWS_URL)
        }
        return response.entries.mapNotNull { entry ->
            val title = entry.title.orEmpty().trim()
            val link = entry.readMoreLink.orEmpty().trim()
            if (title.isEmpty() || link.isEmpty()) return@mapNotNull null
            HomeFeedItem(
                id = entry.id ?: link,
                sourceId = HomeFeedSource.MINECRAFT_NEWS.id,
                title = title,
                summary = entry.text.orEmpty().trim(),
                imageUrl = entry.playPageImage?.url?.let(::mojangImageUrl),
                url = link,
                dateText = formatDateText(entry.date)
            )
        }.take(MAX_ITEMS)
    }

    /**
     * Java 版本更新：官方启动器的 Java 补丁说明 JSON
     *
     * 条目本身没有网页链接，这里按官方文章地址规则拼接：
     * - 正式版：`article/minecraft-java-edition-<版本号>`
     * - 快照 / 预发布：`article/minecraft-<id>`
     * 个别尚未生成文章的条目可能 404，属于已知限制。
     */
    private suspend fun fetchJavaUpdates(): List<HomeFeedItem> {
        val response = httpGetJson<JavaPatchNotesResponse>(MOJANG_JAVA_PATCH_NOTES_URL)
        return response.entries.mapNotNull { entry ->
            val title = entry.title.orEmpty().trim()
            if (title.isEmpty()) return@mapNotNull null
            val version = entry.version.orEmpty().trim()
            val isRelease = entry.type.equals("release", ignoreCase = true)
            val url = if (isRelease && version.isNotEmpty()) {
                "https://www.minecraft.net/en-us/article/minecraft-java-edition-" +
                        version.replace('.', '-')
            } else {
                val id = entry.id.orEmpty()
                if (id.isEmpty()) "https://www.minecraft.net/en-us/download"
                else "https://www.minecraft.net/en-us/article/minecraft-$id"
            }
            HomeFeedItem(
                id = entry.id ?: title,
                sourceId = HomeFeedSource.JAVA_UPDATES.id,
                title = title,
                summary = entry.shortText.orEmpty().trim(),
                imageUrl = entry.image?.url?.let(::mojangImageUrl),
                url = url,
                dateText = formatDateText(entry.date)
            )
        }.take(MAX_ITEMS)
    }

    /** Modrinth 动态：官方搜索接口按「最新创建」排序 */
    private suspend fun fetchModrinth(): List<HomeFeedItem> {
        val response = httpGetJson<ModrinthSearchResponse>(
            url = "${DownloadUrls.modrinthApi()}/search",
            parameters = Parameters.build {
                append("limit", MAX_ITEMS.toString())
                append("index", "newest")
                append("facets", """[["project_type:mod"]]""")
            }
        )
        return response.hits.mapNotNull { hit ->
            val title = hit.title.orEmpty().trim()
            val slug = hit.slug.orEmpty().trim().ifEmpty { hit.projectId.orEmpty() }
            if (title.isEmpty() || slug.isEmpty()) return@mapNotNull null
            HomeFeedItem(
                id = hit.projectId ?: slug,
                sourceId = HomeFeedSource.MODRINTH.id,
                title = title,
                summary = hit.description.orEmpty().trim(),
                imageUrl = hit.iconUrl,
                url = "https://modrinth.com/project/$slug",
                dateText = formatIsoDate(hit.dateCreated)
            )
        }.take(MAX_ITEMS)
    }

    /**
     * CurseForge 资讯：官方接口按「最新创建」排序的模组
     *
     * ⚠️ CurseForge 官方接口需要 API Key（构建期从 `.curseforge_api.txt` /
     * `curseforge_api_key` 注入，由 `GLOBAL_CLIENT` 自动加 `x-api-key` 头）。
     * 没 Key 的构建会收到 403，这里静默退化为空列表（卡片显示占位文案），
     * 不影响其它来源，也不影响主页。
     */
    private suspend fun fetchCurseForge(): List<HomeFeedItem> {
        val response = httpGetJson<CurseForgeSearchResponse>(
            url = "${DownloadUrls.curseForgeApi()}/mods/search",
            parameters = Parameters.build {
                append("gameId", "432") // Minecraft
                append("classId", "6") // Mods
                append("sortField", "11") // 最新创建
                append("sortOrder", "desc")
                append("pageSize", MAX_ITEMS.toString())
            }
        )
        return response.data.mapNotNull { mod ->
            val title = mod.name.orEmpty().trim()
            val slug = mod.slug.orEmpty().trim()
            if (title.isEmpty()) return@mapNotNull null
            HomeFeedItem(
                id = mod.id?.toString() ?: slug,
                sourceId = HomeFeedSource.CURSEFORGE.id,
                title = title,
                summary = mod.summary.orEmpty().trim(),
                imageUrl = mod.logo?.thumbnailUrl,
                url = mod.links?.websiteUrl?.takeIf { it.isNotBlank() }
                    ?: "https://www.curseforge.com/minecraft/mc-mods/$slug",
                dateText = formatIsoDate(mod.dateCreated)
            )
        }.take(MAX_ITEMS)
    }

    /**
     * Minecraft 活动：Minecraft 官网的官方 RSS
     *
     * 该 RSS 里混着快照 / 预发布 / 更新日志，这里按标题关键字过滤掉纯版本公告，
     * 只留下活动、社区与公告类文章（没有配图，卡片用无图占位）。
     */
    private suspend fun fetchMinecraftEvents(): List<HomeFeedItem> {
        val xml = fetchText(MINECRAFT_COMMUNITY_RSS)
        return parseRssItems(xml).mapNotNull { raw ->
            val title = raw.title.trim()
            if (title.isEmpty() || raw.link.isBlank()) return@mapNotNull null
            if (VERSION_ANNOUNCEMENT_REGEX.containsMatchIn(title)) return@mapNotNull null
            HomeFeedItem(
                id = raw.link,
                sourceId = HomeFeedSource.MINECRAFT_EVENTS.id,
                title = title,
                summary = raw.description.trim(),
                imageUrl = null,
                url = raw.link,
                dateText = formatRssDate(raw.pubDate)
            )
        }.take(MAX_ITEMS)
    }

    // ── 网络与解析工具 ──────────────────────────────────────────────────────

    /**
     * 取纯文本响应（用于 RSS）
     *
     * Minecraft 官网 RSS 声明 `encoding="utf-16"` 且确实带 BOM，
     * 直接按 UTF-8 读会变成乱码，所以这里先拿字节、识别 BOM 再解码。
     */
    private suspend fun fetchText(url: String): String {
        val bytes = GLOBAL_CLIENT.get(url)
            .bodyAsChannel()
            .toInputStream()
            .use { it.readBytes() }
        return decodeText(bytes)
    }

    private fun decodeText(bytes: ByteArray): String {
        if (bytes.size >= 2) {
            if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
                return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
            }
            if (bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
                return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
            }
            if (bytes.size >= 3 &&
                bytes[0] == 0xEF.toByte() &&
                bytes[1] == 0xBB.toByte() &&
                bytes[2] == 0xBF.toByte()
            ) {
                return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
            }
        }
        return String(bytes, Charsets.UTF_8)
    }

    /** 官网 RSS 的一条原始 item（`a10:link` 是带 href 属性的写法，不是文本节点） */
    private data class RawRssItem(
        val title: String = "",
        val description: String = "",
        val link: String = "",
        val pubDate: String = ""
    )

    /**
     * 极简 RSS 解析
     *
     * 只关心官网 RSS 实际用到的字段（title / description / a10:link@href / pubDate），
     * 不做完整 RSS 规范实现；任何解析异常都向上抛给 [HomeFeedRepository.load] 兜底。
     */
    private fun parseRssItems(xml: String): List<RawRssItem> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xml))

        val items = mutableListOf<RawRssItem>()
        var title = ""
        var description = ""
        var link = ""
        var pubDate = ""
        var inItem = false

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    val name = parser.name.substringAfter(':')
                    when {
                        name == "item" -> {
                            inItem = true
                            title = ""
                            description = ""
                            link = ""
                            pubDate = ""
                        }
                        inItem && name == "title" -> title = readText(parser)
                        inItem && name == "description" -> description = readText(parser)
                        inItem && name == "link" -> {
                            link = parser.getAttributeValue(null, "href")
                                ?: readText(parser)
                        }
                        inItem && name == "pubDate" -> pubDate = readText(parser)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (inItem && parser.name.substringAfter(':') == "item") {
                        inItem = false
                        items.add(RawRssItem(title, description, link, pubDate))
                    }
                }
            }
            event = parser.next()
        }
        return items
    }

    /** 读当前标签的文本；空标签（`<description />`）不抛异常，返回空串 */
    private fun readText(parser: XmlPullParser): String = runCatching {
        parser.nextText() ?: ""
    }.getOrDefault("")

    // ── 日期格式化 ──────────────────────────────────────────────────────────

    /** Mojang 内容里的日期是 `yyyy-MM-dd` */
    private fun formatDateText(raw: String?): String {
        val value = raw.orEmpty().trim()
        if (value.isEmpty()) return ""
        return runCatching {
            val date = java.time.LocalDate.parse(value)
            date.format(FEED_DATE_FORMATTER)
        }.getOrDefault(value.take(10))
    }

    /** ISO-8601（Modrinth / CurseForge 的 `dateCreated`） */
    private fun formatIsoDate(raw: String?): String {
        val value = raw.orEmpty().trim()
        if (value.isEmpty()) return ""
        return runCatching {
            Instant.parse(value)
                .atZone(ZoneId.systemDefault())
                .format(FEED_DATE_FORMATTER)
        }.getOrDefault(value.take(10))
    }

    /** RFC-1123（官网 RSS 的 pubDate，如 `Tue, 24 Nov 2026 20:00:20 Z`） */
    private fun formatRssDate(raw: String): String {
        val value = raw.trim()
        if (value.isEmpty()) return ""
        return runCatching {
            val parser = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
            parser.parse(value)?.let { date ->
                java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            }.orEmpty()
        }.getOrDefault("")
    }

    // ── 磁盘缓存 ────────────────────────────────────────────────────────────

    private fun cacheDir(): File = File(PathManager.DIR_CACHE, "home_feed")

    private fun cacheFile(sourceId: String): File = File(cacheDir(), "$sourceId.json")

    private fun readCache(sourceId: String): HomeFeedCache? = runCatching {
        val file = cacheFile(sourceId)
        if (!file.isFile) return null
        GLOBAL_JSON.decodeFromString(
            HomeFeedCache.serializer(),
            file.readText()
        )
    }.getOrElse { e ->
        Logger.debug(TAG, "Failed to read cache of $sourceId: ${e.message}")
        null
    }

    private fun writeCache(sourceId: String, cache: HomeFeedCache) {
        runCatching {
            cacheDir().mkdirs()
            cacheFile(sourceId).writeText(
                GLOBAL_JSON.encodeToString(HomeFeedCache.serializer(), cache)
            )
        }.onFailure { e ->
            Logger.debug(TAG, "Failed to write cache of $sourceId: ${e.message}")
        }
    }
}

/** 资讯卡片日期统一显示成 `yyyy-MM-dd`，跨来源对齐，减少视觉噪音 */
private val FEED_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

/** 纯版本公告（快照 / 预发布 / 预览版更新日志）不算「活动」 */
private val VERSION_ANNOUNCEMENT_REGEX =
    Regex("(snapshot|pre-release|pre release|preview|changelog)", RegexOption.IGNORE_CASE)

private const val MOJANG_CONTENT_BASE = "https://launchercontent.mojang.com"

private const val MOJANG_NEWS_URL_V2 = "$MOJANG_CONTENT_BASE/v2/news.json"
private const val MOJANG_NEWS_URL = "$MOJANG_CONTENT_BASE/news.json"
private const val MOJANG_JAVA_PATCH_NOTES_URL = "$MOJANG_CONTENT_BASE/v2/javaPatchNotes.json"
private const val MINECRAFT_COMMUNITY_RSS = "https://www.minecraft.net/en-us/feeds/community-content/rss"

/** Mojang 内容里的图片是站内绝对路径（`/v2/images/...`），需要补全域名 */
private fun mojangImageUrl(raw: String?): String? {
    val value = raw.orEmpty().trim()
    if (value.isEmpty()) return null
    return if (value.startsWith("http://") || value.startsWith("https://")) {
        value
    } else {
        MOJANG_CONTENT_BASE + value
    }
}

// ── 各来源的响应模型（只声明用到的字段，未知字段交给 GLOBAL_JSON 忽略） ──────

@Serializable
private data class MojangNewsResponse(
    val entries: List<MojangNewsEntry> = emptyList()
)

@Serializable
private data class MojangNewsEntry(
    val title: String? = null,
    val id: String? = null,
    val date: String? = null,
    val text: String? = null,
    val readMoreLink: String? = null,
    @SerialName("playPageImage") val playPageImage: MojangImage? = null
)

@Serializable
private data class JavaPatchNotesResponse(
    val entries: List<JavaPatchNote> = emptyList()
)

@Serializable
private data class JavaPatchNote(
    val title: String? = null,
    val version: String? = null,
    val type: String? = null,
    val id: String? = null,
    val date: String? = null,
    @SerialName("shortText") val shortText: String? = null,
    val image: MojangImage? = null
)

@Serializable
private data class MojangImage(
    val url: String? = null
)

@Serializable
private data class ModrinthSearchResponse(
    val hits: List<ModrinthHit> = emptyList()
)

@Serializable
private data class ModrinthHit(
    @SerialName("project_id") val projectId: String? = null,
    val slug: String? = null,
    val title: String? = null,
    val description: String? = null,
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("date_created") val dateCreated: String? = null
)

@Serializable
private data class CurseForgeSearchResponse(
    val data: List<CurseForgeMod> = emptyList()
)

@Serializable
private data class CurseForgeMod(
    val id: Int? = null,
    val name: String? = null,
    val slug: String? = null,
    val summary: String? = null,
    @SerialName("dateCreated") val dateCreated: String? = null,
    val links: CurseForgeLinks? = null,
    val logo: CurseForgeLogo? = null
)

@Serializable
private data class CurseForgeLinks(
    @SerialName("websiteUrl") val websiteUrl: String? = null
)

@Serializable
private data class CurseForgeLogo(
    @SerialName("thumbnailUrl") val thumbnailUrl: String? = null
)
