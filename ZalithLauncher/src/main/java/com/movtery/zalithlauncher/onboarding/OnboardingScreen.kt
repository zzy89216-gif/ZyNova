/*
 * ZyNova Launcher — 首次启动引导（EULA / 隐私政策 / 第三方声明 / 教程 / 年龄确认）
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings

/**
 * 首次启动的全屏引导。
 *
 * 顺序：EULA → 隐私政策 → 第三方开源合规声明 → 使用教程 → 同意 →
 * 选择年龄（未满 18 / 已满 18）→ 进入主界面。
 *
 * ⚠️ 两个开关都是 28.1.0 新增的键，**老用户升级后同样会走一次**，读完并选完年龄段才进主界面。
 */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    // 0..4 = 五份文档，5 = 选年龄
    var step by remember { mutableStateOf(0) }
    var minor by remember { mutableStateOf(true) }

    val titles = listOf(
        "《最终用户许可协议》",
        "《隐私政策》",
        "《第三方开源合规声明》",
        "《使用教程》",
        "《适用地区与法律管辖》"
    )
    val bodies = listOf(
        LegalTexts.eula,
        LegalTexts.privacy,
        LegalTexts.thirdParty,
        LegalTexts.tutorial,
        LegalTexts.regions
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (step < 5) "${step + 1} / 5  ${titles[step]}" else stringResource(R.string.onboarding_age_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (step < 5) {
                    Text(text = bodies[step], style = MaterialTheme.typography.bodySmall)
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
                            "选择「已满 18 周岁」不设时间限制。你可以随时卸载重装重新选择，请如实填写。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (step < 5) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (step > 0) {
                        OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f)) { Text("上一份") }
                    }
                    Button(
                        modifier = Modifier.weight(2f),
                        onClick = { step++ }
                    ) {
                        Text(if (step == 4) stringResource(R.string.onboarding_accept_all) else "我已阅读，继续")
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
