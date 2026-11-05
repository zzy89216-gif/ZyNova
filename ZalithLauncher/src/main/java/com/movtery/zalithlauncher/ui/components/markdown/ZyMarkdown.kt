/*
 * ZyNova Launcher
 * Copyright (C) 2026 zzy89216-gif and contributors
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

package com.movtery.zalithlauncher.ui.components.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ZyNova 自研的轻量 Markdown 渲染器（专供 AI 消息使用）。
 *
 * 性能约定（改这个文件前请先读）：
 * - 行内样式在**解析阶段**一次性构建成 [AnnotatedString]，渲染时不再逐帧重建；
 * - 解析结果用 `remember(content)` 缓存，内容不变就不会重新解析；
 * - 代码块只有横向滚动，不与外部 LazyColumn 抢手势；
 * - 表格固定列数 + weight 布局，避免每帧测量大量文本。
 *
 * 支持：标题、段落、加粗、斜体、删除线、行内代码、围栏代码块、表格、
 * 无序/有序列表、引用、分割线、链接。
 */
@Composable
fun ZyMarkdown(
    content: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    val blocks = remember(content) { parseMarkdown(content) }

    val codeBackground = if (MaterialTheme.colorScheme.surface.luminanceLike() < 0.5f) {
        Color.White.copy(alpha = 0.06f)
    } else {
        Color.Black.copy(alpha = 0.05f)
    }
    val quoteColor = color.copy(alpha = 0.25f)
    val mutedColor = color.copy(alpha = 0.72f)
    val dividerColor = color.copy(alpha = 0.12f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Heading -> Text(
                    text = block.text,
                    style = when (block.level) {
                        1 -> MaterialTheme.typography.titleLarge
                        2 -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.titleSmall
                    },
                    color = color,
                    modifier = Modifier.padding(top = if (block.level == 1) 6.dp else 2.dp)
                )

                is MdBlock.Paragraph -> Text(
                    text = block.text,
                    style = style,
                    color = color
                )

                is MdBlock.Bullet -> Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    block.items.forEach { item ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "•",
                                style = style,
                                color = mutedColor,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(text = item, style = style, color = color)
                        }
                    }
                }

                is MdBlock.Ordered -> Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    block.items.forEachIndexed { index, item ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "${index + 1}.",
                                style = style,
                                color = mutedColor,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(text = item, style = style, color = color)
                        }
                    }
                }

                is MdBlock.Quote -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(quoteColor, RoundedCornerShape(2.dp))
                    ) {}
                    Text(
                        text = block.text,
                        style = style,
                        color = mutedColor
                    )
                }

                is MdBlock.Code -> CodeBlock(
                    code = block.code,
                    language = block.language,
                    style = style,
                    background = codeBackground
                )

                is MdBlock.Table -> MarkdownTable(
                    header = block.header,
                    rows = block.rows,
                    style = style,
                    color = color,
                    dividerColor = dividerColor
                )

                is MdBlock.Divider -> HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = dividerColor
                )
            }
        }
    }
}

@Composable
private fun CodeBlock(
    code: String,
    language: String,
    style: TextStyle,
    background: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(vertical = 10.dp)
    ) {
        if (language.isNotBlank()) {
            Text(
                text = language,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Text(
            text = code,
            style = style.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun MarkdownTable(
    header: List<AnnotatedString>,
    rows: List<List<AnnotatedString>>,
    style: TextStyle,
    color: Color,
    dividerColor: Color,
) {
    val columns = header.size.coerceAtLeast(1)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            header.forEach { cell ->
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    text = cell,
                    style = style.copy(fontWeight = FontWeight.SemiBold),
                    color = color
                )
            }
        }

        HorizontalDivider(color = dividerColor)

        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                for (index in 0 until columns) {
                    val cell = row.getOrNull(index) ?: AnnotatedString("")
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 5.dp, horizontal = 4.dp),
                        text = cell,
                        style = style,
                        color = color
                    )
                }
            }
        }
    }
}

private fun Color.luminanceLike(): Float =
    0.2126f * red + 0.7152f * green + 0.0722f * blue

// ─────────────────────────────────────────────────────────────────
//  行内解析
// ─────────────────────────────────────────────────────────────────

private fun inlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        when {
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end > i) {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append(inlineMarkdown(text.substring(i + 2, end)))
                    }
                    i = end + 2
                } else {
                    append("**")
                    i += 2
                }
            }

            text.startsWith("~~", i) -> {
                val end = text.indexOf("~~", i + 2)
                if (end > i) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append("~~")
                    i += 2
                }
            }

            text[i] == '`' -> {
                val end = text.indexOf('`', i + 1)
                if (end > i) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color.White.copy(alpha = 0.08f)
                        )
                    ) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append('`')
                    i++
                }
            }

            text[i] == '[' -> {
                val close = text.indexOf(']', i + 1)
                val open = if (close > i) text.indexOf('(', close + 1) else -1
                val endParen = if (open > close) text.indexOf(')', open + 1) else -1
                if (close > i && open == close + 1 && endParen > open) {
                    val label = text.substring(i + 1, close)
                    val url = text.substring(open + 1, endParen)
                    withLink(LinkAnnotation.Url(url)) {
                        withStyle(
                            SpanStyle(
                                color = Color(0xFF6EA8FF),
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(label)
                        }
                    }
                    i = endParen + 1
                } else {
                    append('[')
                    i++
                }
            }

            text[i] == '*' -> {
                val end = text.indexOf('*', i + 1)
                if (end > i) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(inlineMarkdown(text.substring(i + 1, end)))
                    }
                    i = end + 1
                } else {
                    append('*')
                    i++
                }
            }

            else -> {
                append(text[i])
                i++
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  块级解析
// ─────────────────────────────────────────────────────────────────

internal sealed interface MdBlock {
    data class Heading(val level: Int, val text: AnnotatedString) : MdBlock
    data class Paragraph(val text: AnnotatedString) : MdBlock
    data class Bullet(val items: List<AnnotatedString>) : MdBlock
    data class Ordered(val items: List<AnnotatedString>) : MdBlock
    data class Quote(val text: AnnotatedString) : MdBlock
    data class Code(val language: String, val code: String) : MdBlock
    data class Table(val header: List<AnnotatedString>, val rows: List<List<AnnotatedString>>) : MdBlock
    data object Divider : MdBlock
}

private val bulletRegex = Regex("^\\s*[-*+]\\s+(.*)$")
private val orderedRegex = Regex("^\\s*\\d+[.)]\\s+(.*)$")
private val tableSeparatorRegex = Regex("^\\s*\\|?[\\s:|-]+\\|?\\s*$")

internal fun parseMarkdown(source: String): List<MdBlock> {
    val lines = source.replace("\r\n", "\n").split('\n')
    val blocks = mutableListOf<MdBlock>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        when {
            line.trimStart().startsWith("```") -> {
                val language = line.trim().removePrefix("```").trim()
                val code = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                    if (code.isNotEmpty()) code.append('\n')
                    code.append(lines[i])
                    i++
                }
                if (i < lines.size) i++
                blocks += MdBlock.Code(language, code.toString())
            }

            line.startsWith("#") -> {
                val level = line.takeWhile { it == '#' }.length.coerceIn(1, 3)
                blocks += MdBlock.Heading(level, inlineMarkdown(line.dropWhile { it == '#' }.trim()))
                i++
            }

            line.trim() in setOf("---", "***", "___") -> {
                blocks += MdBlock.Divider
                i++
            }

            line.trimStart().startsWith(">") -> {
                val quote = StringBuilder()
                while (i < lines.size && lines[i].trimStart().startsWith(">")) {
                    if (quote.isNotEmpty()) quote.append('\n')
                    quote.append(lines[i].trimStart().removePrefix(">").trim())
                    i++
                }
                blocks += MdBlock.Quote(inlineMarkdown(quote.toString()))
            }

            isTableStart(lines, i) -> {
                val header = splitTableRow(lines[i])
                i += 2 // 跳过表头与分隔行
                val rows = mutableListOf<List<AnnotatedString>>()
                while (i < lines.size && lines[i].contains('|') && lines[i].isNotBlank()) {
                    rows += splitTableRow(lines[i]).map { inlineMarkdown(it) }
                    i++
                }
                blocks += MdBlock.Table(
                    header = header.map { inlineMarkdown(it) },
                    rows = rows
                )
            }

            bulletRegex.matches(line) -> {
                val items = mutableListOf<AnnotatedString>()
                while (i < lines.size) {
                    val match = bulletRegex.matchEntire(lines[i]) ?: break
                    items += inlineMarkdown(match.groupValues[1])
                    i++
                }
                blocks += MdBlock.Bullet(items)
            }

            orderedRegex.matches(line) -> {
                val items = mutableListOf<AnnotatedString>()
                while (i < lines.size) {
                    val match = orderedRegex.matchEntire(lines[i]) ?: break
                    items += inlineMarkdown(match.groupValues[1])
                    i++
                }
                blocks += MdBlock.Ordered(items)
            }

            line.isBlank() -> i++

            else -> {
                val paragraph = StringBuilder()
                while (i < lines.size) {
                    val current = lines[i]
                    if (current.isBlank()) break
                    if (current.trimStart().startsWith("```")) break
                    if (current.startsWith("#")) break
                    if (current.trimStart().startsWith(">")) break
                    if (bulletRegex.matches(current) || orderedRegex.matches(current)) break
                    if (isTableStart(lines, i)) break
                    if (paragraph.isNotEmpty()) paragraph.append('\n')
                    paragraph.append(current)
                    i++
                }
                blocks += MdBlock.Paragraph(inlineMarkdown(paragraph.toString()))
            }
        }
    }

    return blocks
}

private fun isTableStart(lines: List<String>, index: Int): Boolean {
    val header = lines.getOrNull(index) ?: return false
    val separator = lines.getOrNull(index + 1) ?: return false
    if (!header.contains('|')) return false
    if (!separator.contains('-')) return false
    if (!tableSeparatorRegex.matches(separator)) return false
    return splitTableRow(header).size >= 2
}

private fun splitTableRow(line: String): List<String> =
    line.trim().removePrefix("|").removeSuffix("|").split('|').map { it.trim() }
