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
 * ⚠️ 老用户升级后同样会走一遍（见 [OnboardingState.needsOnboarding]）。
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

    val docs = remember(noticesAsset) {
        listOf(
            "《最终用户许可协议》" to LegalTexts.eula,
            "《隐私政策》" to LegalTexts.privacy,
            "《第三方开源合规声明》" to (noticesAsset ?: LegalTexts.thirdParty),
            "《ZyNova 使用教程》" to LegalTexts.tutorial,
            "《适用地区与法律管辖》" to LegalTexts.regions,
            "《补充免责与风险告知》" to LegalTexts.risks
        )
    }

    var step by remember { mutableStateOf(0) }        // 0..5 文档，6 选年龄
    var agreed by remember { mutableStateOf(false) }  // 每页都要重新勾选
    var minor by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()
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
                text = if (step < docs.size) "${step + 1} / ${docs.size}  ${docs[step].first}"
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
                        text = highlightLegal(docs[step].second),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = if (atBottom) "—— 已到末尾 ——" else "请继续向下滑动阅读至末尾",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (atBottom) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        stringResource(R.string.onboarding_age_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = { minor = true }, enabled = minor, modifier = Modifier.weight(1f)) {
                            Text(if (minor) "✓ 未满 18 周岁" else "未满 18 周岁")
                        }
                        OutlinedButton(onClick = { minor = false }, enabled = !minor, modifier = Modifier.weight(1f)) {
                            Text(if (!minor) "✓ 已满 18 周岁" else "已满 18 周岁")
                        }
                    }
                    Text(
                        "选择「未满 18 周岁」后，只能在本软件内于周五、周六、周日和法定节假日的 20:00–21:00 启动游戏；" +
                            "选择「已满 18 周岁」不设时间限制。请如实填写。",
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
                        text = "我已阅读并同意本份文档的全部内容",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (step > 0) {
                        OutlinedButton(
                            onClick = { step--; agreed = false },
                            modifier = Modifier.weight(1f)
                        ) { Text("上一份") }
                    }
                    Button(
                        modifier = Modifier.weight(2f),
                        enabled = agreed && atBottom,
                        onClick = { step++; agreed = false }
                    ) {
                        Text(if (step == docs.size - 1) "已读完全部，去选择年龄" else "下一步")
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
 * - `不承担` / `免责` / `风险` / `无法使用` → 错误色
 * - 一级标题（以《 开头的一行）→ 加粗 + 主题色
 */
private fun highlightLegal(text: String): androidx.compose.ui.text.AnnotatedString {
    val danger = androidx.compose.ui.graphics.Color(0xFFE53935)
    val builder = androidx.compose.ui.text.AnnotatedString.Builder()
    text.lines().forEachIndexed { index, line ->
        val isTitle = line.trimStart().startsWith("《")
        val base = androidx.compose.ui.text.SpanStyle(
            fontWeight = if (isTitle) androidx.compose.ui.text.font.FontWeight.SemiBold
            else androidx.compose.ui.text.font.FontWeight.Normal,
            color = if (isTitle) androidx.compose.ui.graphics.Color(0xFF3B6FF6)
            else androidx.compose.ui.graphics.Color.Unspecified
        )
        builder.pushStyle(base)
        builder.append(line)
        builder.popStyle()
        if (index != text.lines().lastIndex) builder.append("\n")
    }
    //再整体把关键词染红
    val raw = builder.toAnnotatedString()
    return androidx.compose.ui.text.buildAnnotatedString {
        append(raw)
        val regex = Regex("AGPL|LGPL|不承担任何|免责|风险|无法使用|不允许|必须")
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
