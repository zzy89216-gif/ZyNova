/*
 * ZyNova Launcher — 游戏数据强制迁移界面
 * GPL-3.0-or-later
 *
 * 28.1.2 起游戏数据放在公共存储的 `/ZyNova/.Minecraft`。**老用户**第一次打开时会先走这里，
 * 把私有目录里的实例、存档、模组搬过去：有进度、不能跳过、失败自动回退。
 *
 * 维护者要求（28.1.3）：
 * 1. 这个界面要**占满整块屏**（不是一小条进度，也不是弹窗），让人一眼知道“正在搬东西”；
 * 2. 搬完之后**不要自动关掉**：必须由用户点「下一步」才进主界面，
 *    否则进度条一闪而过，用户根本不知道发生过迁移，更不知道数据被搬到了哪里。
 */
package com.movtery.zalithlauncher.ui.screens.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.path.GameDirMigration

@Composable
fun GameDataMigrationScreen(onDone: () -> Unit) {
    val progress by GameDirMigration.progress.collectAsState()
    val currentFile by GameDirMigration.currentFile.collectAsState()

    /**
     * null = 还在搬；true = 搬到公共目录成功；false = 失败并已回退到旧目录
     */
    var result by remember { mutableStateOf<Boolean?>(null) }

    //整个迁移过程是全屏的：隐藏状态栏与导航栏，别让系统栏打断“正在搬数据”这件事
    FullscreenInsets()

    LaunchedEffect(Unit) {
        GameDirMigration.runMigration { ok ->
            result = ok
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 40.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val done = result

            Icon(
                modifier = Modifier.size(56.dp),
                painter = painterResource(
                    when (done) {
                        null -> R.drawable.ic_folder_outlined
                        true -> R.drawable.ic_check
                        false -> R.drawable.ic_warning_outlined
                    }
                ),
                contentDescription = null,
                tint = when (done) {
                    false -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = when (done) {
                    null -> stringResource(R.string.game_dir_migration_screen_title)
                    true -> stringResource(R.string.game_dir_migration_screen_done_title)
                    false -> stringResource(R.string.game_dir_migration_screen_failed_title)
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (done == false) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                modifier = Modifier.widthIn(max = 520.dp),
                text = when (done) {
                    null -> stringResource(R.string.game_dir_migration_screen_hint)
                    true -> stringResource(R.string.game_dir_migration_screen_done_hint)
                    false -> stringResource(R.string.game_dir_migration_screen_failed_hint)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            when (done) {
                null -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 520.dp),
                        progress = { progress }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 520.dp),
                        text = "${(progress * 100).toInt()}%  $currentFile",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                else -> {
                    Button(
                        modifier = Modifier.widthIn(min = 220.dp, max = 520.dp),
                        onClick = onDone
                    ) {
                        Text(text = stringResource(R.string.game_dir_migration_next))
                    }
                }
            }

            if (done == null) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    modifier = Modifier.widthIn(max = 520.dp),
                    text = stringResource(R.string.game_dir_migration_screen_keep_open),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 迁移期间隐藏系统栏（状态栏 / 导航栏），退出时恢复。
 */
@Composable
private fun FullscreenInsets() {
    val view = LocalView.current
    LaunchedEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
