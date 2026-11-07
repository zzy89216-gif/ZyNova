/*
 * ZyNova Launcher
 * Copyright (C) 2026 zzy89216-gif and contributors
 * GPL-3.0-or-later
 */

package com.movtery.zalithlauncher.ai.agent.tools

import android.os.Environment
import com.movtery.zalithlauncher.ai.agent.AITool
import com.movtery.zalithlauncher.ai.agent.AIToolRisk
import com.movtery.zalithlauncher.ai.agent.AIToolSchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * 本地文档生成（议题：AI 生成 PPT / Word）。
 *
 * 不引入 Apache POI（会让包体涨十几 MB）：docx 与 pptx 本质是 **zip + OOXML**，
 * 这里手写最小的合法结构，够用即可 —— 标题 + 正文 / 标题 + 要点列表。
 *
 * 约束：
 * - 只能写到公有存储（需要「所有文件访问权限」），默认落 `Download/`；
 * - 文件名会做清洗，避免覆盖系统文件；
 * - 生成过程在 IO 线程。
 */
private const val DOCX_CT = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>"""

private const val RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="%s"/>
</Relationships>"""

private fun esc(s: String): String = s
    .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    .replace("\"", "&quot;").replace("'", "&apos;")

internal fun documentTools(): List<AITool> = listOf(
    aiTool(
        name = "create_docx",
        description = """
            生成一个 .docx（Word）文档并保存到公有存储（默认 Download）。
            用户要「写个文档 / 报告 / 说明」时用它。先把内容组织成纯文本（可含换行），再调用本工具。
            需要「所有文件访问权限」，没有会返回可读错误。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "title" to AIToolSchema.string("文档标题，会作为一级标题写在最前面"),
                "content" to AIToolSchema.string("正文；用 \\n 分段，用 - 开头表示要点"),
                "save_path" to AIToolSchema.string("保存路径，默认 Download/<标题>.docx"),
            ),
            required = listOf("title", "content"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, _ ->
        val title = args.strOr("title", "文档")
        val content = args.str("content").orEmpty()
        if (!Environment.isExternalStorageManager()) {
            return@aiTool "缺少「所有文件访问权限」，请到：设置 → AI 设置 → 公有目录读写，点「去授权」后重试。"
        }
        val target = resolveDocTarget(args.str("save_path"), title, "docx")
        withContext(Dispatchers.IO) {
            target.parentFile?.mkdirs()
            ZipOutputStream(target.outputStream().buffered()).use { zip ->
                zip.putNextEntry(ZipEntry("[Content_Types].xml")); zip.write(DOCX_CT.toByteArray()); zip.closeEntry()
                zip.putNextEntry(ZipEntry("_rels/.rels")); zip.write(RELS.format("word/document.xml").toByteArray()); zip.closeEntry()
                zip.putNextEntry(ZipEntry("word/document.xml")); zip.write(docxBody(title, content).toByteArray()); zip.closeEntry()
            }
        }
        "已生成 Word 文档：${target.absolutePath}"
    },

    aiTool(
        name = "create_pptx",
        description = """
            生成一个 .pptx（PowerPoint）文件并保存到公有存储（默认 Download）。
            `slides` 是一个 **JSON 数组字符串**，每项形如：
            [{"title":"第一页标题","points":["要点1","要点2"]}, {"title":"第二页","points":["…"]}]
            用户在要「做一份 PPT / 演示文稿」时用它。先自己提炼大纲，再调用。
            需要「所有文件访问权限」。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "title" to AIToolSchema.string("演示文稿标题（用于文件名与封面）"),
                "slides" to AIToolSchema.string("JSON 数组字符串，元素含 title 与 points"),
                "save_path" to AIToolSchema.string("保存路径，默认 Download/<标题>.pptx"),
            ),
            required = listOf("title", "slides"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, _ ->
        val title = args.strOr("title", "演示文稿")
        if (!Environment.isExternalStorageManager()) {
            return@aiTool "缺少「所有文件访问权限」，请到：设置 → AI 设置 → 公有目录读写，点「去授权」后重试。"
        }
        val slides = parseSlides(args.str("slides").orEmpty())
        if (slides.isEmpty()) return@aiTool "slides 解析为空，请传一个 JSON 数组，例如 [{\"title\":\"封面\",\"points\":[\"要点\"]}]"
        val target = resolveDocTarget(args.str("save_path"), title, "pptx")
        withContext(Dispatchers.IO) {
            target.parentFile?.mkdirs()
            writePptx(target, title, slides)
        }
        "已生成 PPT：${target.absolutePath}（共 ${slides.size} 页）"
    },
)

private data class Slide(val title: String, val points: List<String>)

private fun parseSlides(raw: String): List<Slide> {
    val text = raw.trim().ifBlank { return emptyList() }
    return runCatching {
        val array = Json.parseToJsonElement(text) as? JsonArray ?: return emptyList()
        array.mapNotNull { item ->
            val obj = item as? JsonObject ?: return@mapNotNull null
            val t = (obj["title"] as? JsonPrimitive)?.content.orEmpty()
            val pts = (obj["points"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content } ?: emptyList()
            if (t.isBlank() && pts.isEmpty()) null else Slide(t, pts)
        }
    }.getOrDefault(emptyList())
}

private fun resolveDocTarget(savePath: String?, title: String, ext: String): File {
    val safeName = title.replace(Regex("[\\\\/:*?\"<>|\\r\\n]"), "_").take(60).ifBlank { "document" }
    val raw = savePath?.trim().orEmpty()
    val file = when {
        raw.isBlank() -> File("/storage/emulated/0/Download", "$safeName.$ext")
        raw.startsWith("/") -> File(raw)
        else -> File("/storage/emulated/0/Download", raw)
    }
    return if (file.extension.isEmpty()) File(file.parentFile, "${file.name}.$ext") else file
}

private fun docxBody(title: String, content: String): String = buildString {
    append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
    append("""<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body>""")
    append(paragraph(title, bold = true, size = 32))
    content.split('\n').forEach { line ->
        val t = line.trim()
        if (t.isNotEmpty()) {
            append(if (t.startsWith("-")) paragraph("• " + t.removePrefix("-").trim()) else paragraph(t))
        }
    }
    append("""<w:sectPr><w:pgSz w:w="11906" w:h="16838"/></w:sectPr>""")
    append("</w:body></w:document>")
}

private fun paragraph(text: String, bold: Boolean = false, size: Int = 24): String = buildString {
    append("<w:p><w:r><w:rPr>")
    if (bold) append("<w:b/>")
    append("""<w:sz w:val="$size"/><w:szCs w:val="$size"/>""")
    append("</w:rPr><w:t xml:space=\"preserve\">").append(esc(text)).append("</w:t></w:r></w:p>")
}

/**
 * 写一个最小但能被 PowerPoint 打开的 pptx。
 *
 * PowerShell 结构：presentation.xml + slideMaster + slideLayout + 每页 slide + theme，
 * 少任何一环 PowerPoint 都会报「需要修复」。
 */
private fun writePptx(target: File, title: String, slides: List<Slide>) {
    val count = slides.size
    ZipOutputStream(target.outputStream().buffered()).use { zip ->
        fun put(path: String, body: String) {
            zip.putNextEntry(ZipEntry(path))
            zip.write(body.toByteArray())
            zip.closeEntry()
        }

        val overrides = buildString {
            append("""<Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>""")
            append("""<Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>""")
            append("""<Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>""")
            append("""<Override PartName="/ppt/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>""")
            (1..count).forEach {
                append("""<Override PartName="/ppt/slides/slide$it.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>""")
            }
        }
        put("[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
$overrides
</Types>""")
        put("_rels/.rels", RELS.format("ppt/presentation.xml"))

        val slideIds = buildString {
            (1..count).forEach { append("""<p:sldId id="${255 + it}" r:id="rId${it + 2}"/>""") }
        }
        put("ppt/presentation.xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentation xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
<p:sldMasterIdLst><p:sldMasterId id="2147483648" r:id="rId1"/></p:sldMasterIdLst>
<p:sldIdLst>$slideIds</p:sldIdLst>
<p:sldSz cx="12192000" cy="6858000"/><p:notesSz cx="6858000" cy="9144000"/>
</p:presentation>""")

        put("ppt/_rels/presentation.xml.rels", buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
            append("""<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>""")
            (1..count).forEach {
                append("""<Relationship Id="rId${it + 2}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide$it.xml"/>""")
            }
            append("</Relationships>")
        })

        put("ppt/slideMasters/slideMaster1.xml", SLIDE_MASTER)
        put("ppt/slideMasters/_rels/slideMaster1.xml.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="../theme/theme1.xml"/>
</Relationships>""")
        put("ppt/slideLayouts/slideLayout1.xml", SLIDE_LAYOUT)
        put("ppt/slideLayouts/_rels/slideLayout1.xml.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="../slideMasters/slideMaster1.xml"/>
</Relationships>""")
        put("ppt/theme/theme1.xml", THEME)

        slides.forEachIndexed { index, slide ->
            val no = index + 1
            put("ppt/slides/slide$no.xml", slideXml(slide))
            put("ppt/slides/_rels/slide$no.xml.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
</Relationships>""")
        }
    }
}

private fun slideXml(slide: Slide): String = buildString {
    append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
    append("""<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"><p:cSld><p:spTree>""")
    append("""<p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>""")
    append(textBox(2, slide.title, 457200, 457200, 11277600, 1371600, 3200, bold = true))
    var shapeId = 3
    slide.points.forEachIndexed { i, point ->
        append(textBox(shapeId++, "• $point", 457200, (1828800 + i * 548640), 11277600, 502920, 1800, bold = false))
    }
    append("</p:spTree></p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr></p:sld>")
}

private fun textBox(
    id: Int, text: String, x: Int, y: Int, cx: Int, cy: Int, size: Int, bold: Boolean
): String = buildString {
    append("""<p:sp><p:nvSpPr><p:cNvPr id="$id" name="TextBox $id"/><p:cNvSpPr txBox="1"/><p:nvPr/></p:nvSpPr>""")
    append("""<p:spPr><a:xfrm><a:off x="$x" y="$y"/><a:ext cx="$cx" cy="$cy"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></p:spPr>""")
    append("""<p:txBody><a:bodyPr wrap="square"/><a:lstStyle/><a:p><a:pPr algn="l"/>""")
    append("""<a:r><a:rPr lang="zh-CN" sz="$size" b="${if (bold) 1 else 0}" dirty="0"/>""")
    append("<a:t>").append(esc(text)).append("</a:t></a:r><a:endParaRPr lang=\"zh-CN\"/></a:p></p:txBody></p:sp>")
}

private const val SLIDE_MASTER = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldMaster xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
<p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr/></p:spTree></p:cSld>
<p:clrMap bg1="lt1" tx1="dk1" bg2="lt2" tx2="dk2" accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" hlink="hlink" folHlink="folHlink"/>
<p:sldLayoutIdLst><p:sldLayoutId id="2147483649" r:id="rId1"/></p:sldLayoutIdLst>
</p:sldMaster>"""

private const val SLIDE_LAYOUT = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldLayout xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" type="blank" preserve="1">
<p:cSld name="Blank"><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr/></p:spTree></p:cSld>
<p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sldLayout>"""

private const val THEME = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<a:theme xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" name="ZyNova">
<a:themeElements>
<a:clrScheme name="ZyNova"><a:dk1><a:sysClr val="windowText" lastClr="000000"/></a:dk1><a:lt1><a:sysClr val="window" lastClr="FFFFFF"/></a:lt1><a:dk2><a:srgbClr val="1F1F1F"/></a:dk2><a:lt2><a:srgbClr val="EFEFEF"/></a:lt2><a:accent1><a:srgbClr val="3B6FF6"/></a:accent1><a:accent2><a:srgbClr val="F2A03D"/></a:accent2><a:accent3><a:srgbClr val="7BD88F"/></a:accent3><a:accent4><a:srgbClr val="FF6B6B"/></a:accent4><a:accent5><a:srgbClr val="9B8BFF"/></a:accent5><a:accent6><a:srgbClr val="4FD1C5"/></a:accent6><a:hlink><a:srgbClr val="3B6FF6"/></a:hlink><a:folHlink><a:srgbClr val="9B8BFF"/></a:folHlink></a:clrScheme>
<a:fontScheme name="ZyNova"><a:majorFont><a:latin typeface="Arial"/><a:ea typeface=""/><a:cs typeface=""/></a:majorFont><a:minorFont><a:latin typeface="Arial"/><a:ea typeface=""/><a:cs typeface=""/></a:minorFont></a:fontScheme>
<a:fmtScheme name="ZyNova"><a:fillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill><a:solidFill><a:schemeClr val="phClr"/></a:solidFill><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:fillStyleLst><a:lnStyleLst><a:ln w="6350"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln><a:ln w="12700"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln><a:ln w="19050"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln></a:lnStyleLst><a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle><a:effectStyle><a:effectLst/></a:effectStyle><a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst><a:bgFillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill><a:solidFill><a:schemeClr val="phClr"/></a:solidFill><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:bgFillStyleLst></a:fmtScheme>
</a:themeElements></a:theme>"""
