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

package com.movtery.zalithlauncher.ui.screens.main.crashlogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.crash.CrashAnalyzer
import com.movtery.zalithlauncher.game.crash.CrashFinding
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile

/**
 * 游戏崩溃后的「怎么处理」卡片。
 *
 * 只读日志最后 512KB 做启发式分析：日志可能很大，但真正有用的异常栈通常在末尾。
 * 分析放在 IO 线程，失败时静默显示「未识别到明确原因」，不能拖慢崩溃页本身。
 */
@Composable
fun CrashAnalysisCard(
    logFile: File,
    modifier: Modifier = Modifier,
) {
    var findings by remember(logFile) { mutableStateOf<List<CrashFinding>?>(null) }

    LaunchedEffect(logFile) {
        findings = withContext(Dispatchers.IO) {
            runCatching {
                val text = readTail(logFile, 512 * 1024)
                CrashAnalyzer.analyze(text)
            }.getOrDefault(emptyList())
        }
    }

    BackgroundCard(
        modifier = modifier.fillMaxWidth(),
        influencedByBackground = false,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.crash_analysis_title),
                style = MaterialTheme.typography.titleMedium
            )

            val current = findings
            if (current == null) {
                Text(
                    text = stringResource(R.string.crash_analysis_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (current.isEmpty()) {
                Text(
                    text = stringResource(R.string.crash_analysis_none),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                current.forEach { finding ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(
                                finding.title,
                                *finding.args.toTypedArray()
                            ),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = stringResource(
                                finding.detail,
                                *finding.args.toTypedArray()
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(
                                finding.action,
                                *finding.args.toTypedArray()
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun readTail(file: File, maxBytes: Int): String {
    if (!file.isFile) return ""
    val length = file.length()
    if (length <= 0L) return ""
    val start = maxOf(0L, length - maxBytes)
    val size = (length - start).toInt()
    val bytes = ByteArray(size)
    RandomAccessFile(file, "r").use { raf ->
        raf.seek(start)
        raf.readFully(bytes)
    }
    return String(bytes, Charsets.UTF_8)
}
