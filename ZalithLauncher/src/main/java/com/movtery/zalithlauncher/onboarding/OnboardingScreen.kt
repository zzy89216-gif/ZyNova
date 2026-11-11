/*
 * ZyNova Launcher — 首次启动引导（条款 → 年龄）
 * GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R

/**
 * 首次启动引导。
 *
 * 严格流程：每份文档**必须滑到底部**并勾选「我已阅读并同意」，才能点下一步；
 * 全部读完后选择年龄段，才进入主界面。
 *
 * 文本来源：`LegalDocuments`（该语言正文 + 该地区补充条款）。
 * 所以同一个用户在何地、用什么语言，看到的是不同的一份。
 *
 * ⚠️ 老用户升级后**不再**重走（只有当 [OnboardingState.CURRENT_LEGAL_VERSION] 变化时才重走）。
 */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    //第三方声明：优先读取 APK 内 assets/licenses/THIRD_PARTY_NOTICES.md，
    //与仓库里的同名文件保持**一字不差**（同一份内容被打进包里）
    val context = LocalContext.current
    val noticesAsset = remember(context) {
        runCatching {
            context.assets.open("licenses/THIRD_PARTY_NOTICES.md").bufferedReader().use { it.readText() }
        }.getOrNull()
    }

    //地区与语言在进入引导时定一次：中途改系统语言不会把正在读的文档换掉
    val region = remember { RegionGate.region() ?: LegalRegions.byCountry("CN")!! }
    val lang = remember { LegalLanguage.of() }

    val docs = remember(noticesAsset, region, lang) {
        val terms = LegalDocuments.build(LegalDocId.TERMS, region, lang)
        val licenses = LegalDocuments.build(LegalDocId.LICENSES, region, lang)
        val guide = LegalDocuments.build(LegalDocId.GUIDE, region, lang)
        listOf(
            terms,
            //许可证全文仍用 APK 内那份，前面是本语言的摘要与本地区条款
            licenses.copy(body = licenses.body + "\n\n" + (noticesAsset ?: "")),
            guide
        )
    }

    var step by remember { mutableStateOf(0) }        // 0..2 文档（3 份），3 选年龄
    var agreed by remember { mutableStateOf(false) }  // 每页都要重新勾选
    var minor by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()
    //切换文档时把阅读位置重置回顶部。
    //否则上一份滑到底后，下一份会沿用同一个滚动偏移（内容变短时直接被夹到 maxValue），
    //一开始就判定「已到末尾」，用户不读也能勾选——等于绕过「每份必须滑到底」。
    LaunchedEffect(step) { scrollState.scrollTo(0) }
    // 滑到底部（留 8px 容差）才允许勾选
    val atBottom by remember {
        derivedStateOf {
            scrollState.maxValue == 0 || scrollState.value >= scrollState.maxValue - 8
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (step < docs.size) "${step + 1} / ${docs.size}  ${docs[step].title}"
                else stringResource(R.string.onboarding_age_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (step < docs.size) {
                    //把 AGPL / LGPL / 免责 等关键词标红，避免用户一扫而过
                    Text(
                        text = highlightLegal(docs[step].body),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = if (atBottom) stringResource(R.string.onboarding_reached_end)
                        else stringResource(R.string.onboarding_scroll_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (atBottom) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        stringResource(
                            if (region.playTimeWindow) R.string.onboarding_age_hint
                            else R.string.onboarding_age_hint_global
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //⚠️ 两个按钮都必须可点：
                        //之前写成了 enabled = minor / enabled = !minor，默认 minor=true 时
                        //「已满 18 周岁」永远处于禁用状态，用户根本选不了。
                        if (minor) {
                            Button(onClick = { minor = true }, modifier = Modifier.weight(1f)) {
                                Text("✓ " + stringResource(R.string.onboarding_age_under18))
                            }
                            OutlinedButton(onClick = { minor = false }, modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.onboarding_age_adult))
                            }
                        } else {
                            OutlinedButton(onClick = { minor = true }, modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.onboarding_age_under18))
                            }
                            Button(onClick = { minor = false }, modifier = Modifier.weight(1f)) {
                                Text("✓ " + stringResource(R.string.onboarding_age_adult))
                            }
                        }
                    }
                    Text(
                        stringResource(R.string.onboarding_age_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (step < docs.size) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = agreed,
                        // 没滑到底不允许勾选
                        enabled = atBottom,
                        onCheckedChange = { agreed = it }
                    )
                    Text(
                        text = stringResource(R.string.onboarding_checkbox),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (step > 0) {
                        OutlinedButton(
                            onClick = { step--; agreed = false },
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.onboarding_prev)) }
                    }
                    Button(
                        modifier = Modifier.weight(2f),
                        enabled = agreed && atBottom,
                        onClick = { step++; agreed = false }
                    ) {
                        Text(
                            stringResource(
                                if (step == docs.size - 1) R.string.onboarding_finish_docs
                                else R.string.onboarding_next
                            )
                        )
                    }
                }
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        OnboardingState.recordAcceptance(minor)
                        onDone()
                    }
                ) {
                    Text(
                        stringResource(
                            if (minor) R.string.onboarding_enter_minor else R.string.onboarding_enter_adult
                        )
                    )
                }
            }
        }
    }
}

/**
 * 法律文本关键词高亮：
 * - `AGPL` / `LGPL` → 错误色（许可证义务最重的部分）
 * - 免责 / 风险 / 制裁 / 无法使用 → 错误色
 * - 一级标题（以《 开头的一行）→ 加粗 + 主题色
 */
private fun highlightLegal(text: String): androidx.compose.ui.text.AnnotatedString {
    val danger = androidx.compose.ui.graphics.Color(0xFFE53935)
    val titleBlue = androidx.compose.ui.graphics.Color(0xFF3B6FF6)
    val builder = androidx.compose.ui.text.AnnotatedString.Builder()
    text.lines().forEachIndexed { index, line ->
        val isTitle = line.trimStart().startsWith("《")
        val base = androidx.compose.ui.text.SpanStyle(
            fontWeight = if (isTitle) androidx.compose.ui.text.font.FontWeight.SemiBold
            else androidx.compose.ui.text.font.FontWeight.Normal,
            color = if (isTitle) titleBlue else androidx.compose.ui.graphics.Color.Unspecified
        )
        builder.pushStyle(base)
        builder.append(line)
        builder.pop()
        if (index != text.lines().lastIndex) builder.append("\n")
    }
    //再整体把关键词染红
    val raw = builder.toAnnotatedString()
    return androidx.compose.ui.text.buildAnnotatedString {
        append(raw)
        val regex = Regex(
            "AGPL|LGPL|不承担任何|免责|风险|无法使用|不允许|必须|制裁|" +
                "not liable|no warranty|risk|unreachable|sanction|must not|prohibited"
        )
        regex.findAll(raw.text).forEach { m ->
            addStyle(
                androidx.compose.ui.text.SpanStyle(
                    color = danger,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                ),
                m.range.first, m.range.last + 1
            )
        }
    }
}
