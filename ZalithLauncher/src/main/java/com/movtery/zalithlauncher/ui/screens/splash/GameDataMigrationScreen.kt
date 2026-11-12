/*
 * ZyNova Launcher — 游戏数据强制迁移界面
 * GPL-3.0-or-later
 *
 * 28.1.2 起游戏数据放在公共存储 `/ZyNova`。老用户第一次打开时会先走这里，
 * 把私有目录里的实例、存档、模组搬过去：有进度、不能跳过、失败自动回退。
 */
package com.movtery.zalithlauncher.ui.screens.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.path.GameDirMigration

@Composable
fun GameDataMigrationScreen(onDone: () -> Unit) {
    val progress by GameDirMigration.progress.collectAsState()
    val currentFile by GameDirMigration.currentFile.collectAsState()

    LaunchedEffect(Unit) {
        GameDirMigration.runMigration { onDone() }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.game_dir_migration_screen_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                progress = { progress }
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "${(progress * 100).toInt()}%  $currentFile",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier.padding(top = 18.dp),
                text = stringResource(R.string.game_dir_migration_screen_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
