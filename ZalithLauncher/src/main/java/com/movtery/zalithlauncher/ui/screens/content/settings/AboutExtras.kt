/*
 * ZyNova Launcher — 关于页：联系与关注 / 吉祥物
 * GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.ui.screens.content.settings

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onItemColor

/**
 * 吉祥物条目：圆形头像 + 名字 + 「吉祥物」小标签。
 *
 * 视觉上弱于维护者条目（字号/透明度略低），不抢 zzy 的位置；
 * 读屏整行念作「吉祥物：无聊的克林」，头像本身不单独朗读（避免重复）。
 */
@Composable
fun MascotRow(modifier: Modifier = Modifier) {
    val desc = stringResource(R.string.about_mascot_desc)
    Surface(
        modifier = modifier,
        color = itemColor(),
        contentColor = onItemColor(),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .semantics(mergeDescendants = true) { contentDescription = desc },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                painter = painterResource(R.drawable.img_mascot_kelin),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.about_mascot_title),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            text = stringResource(R.string.about_mascot_tag),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = stringResource(R.string.about_mascot_text),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * 「联系与关注」分组：官方 X + 两个生态邮箱。
 *
 * 交互约定（议题 #50）：
 * - 点按：X 打开浏览器；邮箱调起邮件 App，没有邮件 App 时退回复制；
 * - 长按：复制到剪贴板并 Toast 提示（三项都支持）。
 */
@Composable
fun ContactSection(
    modifier: Modifier = Modifier,
    openLink: (String) -> Unit
) {
    val context = LocalContext.current
    val xUrl = "https://x.com/ZyNovaProject"
    val mailCn = "ZyNovaLauncher@163.com"
    val mailGlobal = "ZyNovaLauncher@outlook.com"

    SettingsCard(
        modifier = modifier,
        position = CardPosition.Single
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            CardTitleLayout {
                Text(
                    modifier = Modifier.padding(all = 16.dp),
                    text = stringResource(R.string.about_contact_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ContactItem(
                    icon = painterResource(R.drawable.ic_public),
                    title = stringResource(R.string.about_contact_x),
                    value = "@ZyNovaProject",
                    onClick = { openLink(xUrl) },
                    onLongClick = { copyToClipboard(context, xUrl) }
                )
                ContactItem(
                    icon = painterResource(R.drawable.ic_link),
                    title = stringResource(R.string.about_contact_email_cn),
                    value = mailCn,
                    onClick = { openMail(context, mailCn) },
                    onLongClick = { copyToClipboard(context, mailCn) }
                )
                ContactItem(
                    icon = painterResource(R.drawable.ic_link),
                    title = stringResource(R.string.about_contact_email_global),
                    value = mailGlobal,
                    onClick = { openMail(context, mailGlobal) },
                    onLongClick = { copyToClipboard(context, mailGlobal) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContactItem(
    icon: Painter,
    title: String,
    value: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = itemColor(),
        contentColor = onItemColor(),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp)),
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = value,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}

/** 调起邮件 App；没有可用 App 时退回复制（议题 #50 的降级要求） */
private fun openMail(context: Context, address: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        copyToClipboard(context, address)
    } catch (_: SecurityException) {
        copyToClipboard(context, address)
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    manager?.setPrimaryClip(ClipData.newPlainText("ZyNova", text))
    Toast.makeText(context, R.string.about_contact_copied, Toast.LENGTH_SHORT).show()
}
