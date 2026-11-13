/*
 * ZyNova Launcher — 樱花内网穿透：使用教程面板
 * GPL-3.0-or-later
 *
 * 版式与「陶瓦联机」页保持一致：左侧教程（分 Tab），右侧操作。
 * 这里的文字是**给第一次用的人看的**，所以按「准备 → 开服 → 排错」三段走，
 * 每一步都写清楚"点哪里、填什么、看到什么算成功"。
 */
package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.influencedByBackgroundColor
import com.movtery.zalithlauncher.ui.theme.cardTitleColor

/** 教程分区标题 */
private data class GuideTab(val text: Int)

/**
 * 樱花穿透教程面板（左侧）。
 *
 * 与陶瓦页的教程一致：三页 Tab + 左右翻页；内容按「准备 / 开服 / 排错」组织。
 */
@Composable
fun FrpTutorialMenu(modifier: Modifier = Modifier) {
    BackgroundCard(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 12.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        val tabs = remember {
            listOf(
                GuideTab(R.string.frp_guide_tab_prepare),
                GuideTab(R.string.frp_guide_tab_host),
                GuideTab(R.string.frp_guide_tab_faq),
            )
        }

        val pagerState = rememberPagerState(pageCount = { tabs.size })
        var selectedTabIndex by remember { mutableIntStateOf(0) }

        LaunchedEffect(selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }

        SecondaryTabRow(
            containerColor = influencedByBackgroundColor(
                color = cardTitleColor(),
                influencedAlpha = 0.5f * (AllSettings.launcherBackgroundOpacity.state.toFloat() / 100f)
            ),
            selectedTabIndex = selectedTabIndex
        ) {
            tabs.forEachIndexed { index, item ->
                Tab(
                    selected = index == selectedTabIndex,
                    onClick = { selectedTabIndex = index },
                    text = { MarqueeText(text = stringResource(item.text)) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> GuideColumn(
                    title = stringResource(R.string.frp_guide_prepare_title),
                    steps = listOf(
                        R.string.frp_guide_prepare_1,
                        R.string.frp_guide_prepare_2,
                        R.string.frp_guide_prepare_3,
                        R.string.frp_guide_prepare_4,
                    ),
                    note = R.string.frp_guide_prepare_note
                )
                1 -> GuideColumn(
                    title = stringResource(R.string.frp_guide_host_title),
                    steps = listOf(
                        R.string.frp_guide_host_1,
                        R.string.frp_guide_host_2,
                        R.string.frp_guide_host_3,
                        R.string.frp_guide_host_4,
                        R.string.frp_guide_host_5,
                        R.string.frp_guide_host_6,
                        R.string.frp_guide_host_7,
                        R.string.frp_guide_host_8,
                    ),
                    note = R.string.frp_guide_host_note
                )
                else -> GuideColumn(
                    title = stringResource(R.string.frp_guide_faq_title),
                    steps = listOf(
                        R.string.frp_guide_faq_1,
                        R.string.frp_guide_faq_2,
                        R.string.frp_guide_faq_3,
                        R.string.frp_guide_faq_4,
                    ),
                    note = R.string.frp_guide_faq_note
                )
            }
        }
    }
}

@Composable
private fun GuideColumn(
    title: String,
    steps: List<Int>,
    note: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        steps.forEachIndexed { index, res ->
            GuideStep(index + 1, stringResource(res))
        }
        GuideNote(stringResource(note))
    }
}

@Composable
private fun GuideStep(number: Int, text: String) {
    StepRow {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            modifier = Modifier.padding(start = 6.dp),
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun StepRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        content = content
    )
}

@Composable
private fun GuideNote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
