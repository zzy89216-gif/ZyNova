/*
 * ZyNova Launcher — 地区不可用阻断页
 * GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 未覆盖地区时全屏阻断。
 *
 * 只有一个「退出」按钮：点击后由上层执行**安全退出**（finishAffinity + 结束进程），
 * 不做任何伪装成崩溃的行为。
 */
@Composable
fun RegionBlockedScreen(onExit: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "你所在的地区无法使用该产品",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = "本产品目前仅在中国、美国、韩国、日本、俄罗斯提供。\n" +
                    "如果判断有误，请检查系统时区与语言设置后重新打开。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "检测到：${RegionGate.describe()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                onClick = onExit
            ) {
                Text("退出")
            }
        }
    }
}
