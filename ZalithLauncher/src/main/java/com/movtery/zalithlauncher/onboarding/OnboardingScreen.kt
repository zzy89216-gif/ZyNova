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
import com.movtery.zalithlauncher.setting.AllSettings

/**
 * 首次启动的全屏引导。
 *
 * 顺序：四份文档（EULA / 隐私政策 / 第三方开源合规声明 / 使用教程）→ 同意 →
 * 选择年龄（未满 18 / 已满 18）→ 进入主界面。
 *
 * ⚠️ 只在首次安装出现：老用户升级不会被打断（见 [OnboardingState.needsOnboarding]）。
 */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    var step by remember { mutableStateOf(0) } // 0 = 文档, 1 = 选年龄
    var minor by remember { mutableStateOf(true) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (step == 0) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Section(
                        "《最终用户许可协议》（EULA）",
                        "本软件为开源软件，按 GNU GPL-3.0 分发。你在使用本软件时需自行准备 Minecraft 账号（正版或离线），" +
                            "本软件不提供游戏本体与账号。禁止将本软件用于任何违反当地法律法规的用途。" +
                            "完整条款见仓库根目录的 LICENSE；若不同意，请立即停止使用并卸载。"
                    )
                    Section(
                        "《隐私政策》（Privacy Policy）",
                        "本软件把数据保存在本机：账号信息、实例配置、日志；不主动上传。\n" +
                            "只有在你主动启用 AI 功能时，对话内容与相关上下文才会发送到你自行配置的第三方模型服务；" +
                            "启用网络搜索时，查询词会发送到搜索引擎。除此之外没有其他外发行为。\n" +
                            "卸载应用会删除应用私有目录中的数据，但不会删除你在外部存储上的游戏文件。"
                    )
                    Section(
                        "《第三方开源合规声明》",
                        "本软件包含多个第三方开源组件，各自适用其许可证：\n" +
                            "• Terracotta（陶瓦联机）—— AGPL-3.0（二进制未修改；若你修改后再分发，需公开对应源码）\n" +
                            "• MobileGlues —— LGPL-2.1；sora-editor —— LGPL-2.1；LTW-Turbo —— LGPL-3.0\n" +
                            "• frp / frpc（樱花定制构建）—— Apache-2.0\n" +
                            "• GL4ES / ByteHook / OpenNBT / skinview3d —— MIT；ANGLE / LWJGL / MMKV —— BSD-3-Clause\n" +
                            "完整清单与许可证原文见「设置 → 关于 → 开源许可」，以及仓库的 THIRD_PARTY_NOTICES.md。"
                    )
                    Section(
                        "《使用教程》",
                        "1. 首页：选择实例 → 启动游戏；没有实例时点「下载一个实例」。\n" +
                            "2. 下载：资源核心可搜索模组 / 整合包 / 资源包 / 光影，自动解析前置依赖。\n" +
                            "3. 联机：多人联机里可选用陶瓦（P2P）或樱花内网穿透（需要樱花账号）。\n" +
                            "4. AI 助手：可以直接让它装游戏、装模组、读日志、改配置；联网搜索固定使用 DuckDuckGo。\n" +
                            "5. 出问题：设置 → 关于 → 日志，可导出日志用于反馈。"
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { step = 1 }
                ) {
                    Text(stringResource(com.movtery.zalithlauncher.R.string.onboarding_accept_all))
                }
            } else {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(com.movtery.zalithlauncher.R.string.onboarding_age_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(com.movtery.zalithlauncher.R.string.onboarding_age_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { minor = true },
                            enabled = minor,
                            modifier = Modifier.weight(1f)
                        ) { Text(if (minor) "已选择：未满 18 周岁" else "未满 18 周岁") }
                        OutlinedButton(
                            onClick = { minor = false },
                            enabled = !minor,
                            modifier = Modifier.weight(1f)
                        ) { Text(if (!minor) "已选择：已满 18 周岁" else "已满 18 周岁") }
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        AllSettings.onboardingAccepted.save(true)
                        AllSettings.onboardingIsMinor.save(minor)
                        AllSettings.onboardingAgeConfirmed.save(true)
                        onDone()
                    }
                ) {
                    Text(stringResource(if (minor) com.movtery.zalithlauncher.R.string.onboarding_enter_minor else com.movtery.zalithlauncher.R.string.onboarding_enter_adult))
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Text(body, style = MaterialTheme.typography.bodySmall)
    }
}
