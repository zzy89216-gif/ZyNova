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

package com.movtery.zalithlauncher.ai.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ai.audit.AIAuditLog
import com.movtery.zalithlauncher.ai.audit.AIAuditRecord
import com.movtery.zalithlauncher.utils.file.shareFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.movtery.zalithlauncher.ai.AIModelRepository
import com.movtery.zalithlauncher.ai.AIPermissionMode
import com.movtery.zalithlauncher.ai.AISettings
import com.movtery.zalithlauncher.ai.model.AIModelInfo
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.SmallOutlinedEditField
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.toAndroidString
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.launch

/**
 * AI 设置的内容体（议题 #37 / #41）
 *
 * 27.7.0 起不再有独立的「AI 配置页」：这些卡片直接渲染在
 * 「设置 → AI 设置」分类里，由 [com.movtery.zalithlauncher.ui.screens.content.settings.AISettingsScreen] 承载。
 *
 * ⚠️ 只换渲染位置：配置仍然全部存在 [AISettings]（MMKV 键名一个都没改），
 * 所以老用户填过的 API Key、模型、权限模式都还在。
 */
@Composable
fun AIConfigContent(
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 27.5.1 起固定使用 DeepSeek，不再有服务商选择
    var apiKey by remember { mutableStateOf(AISettings.getKey()) }
    //保存「用户真正填过的值」，而不是回退后的默认值：
    //这样输入框默认是空的，默认地址只作为灰字提示出现，聚焦时再淡出
    var baseUrl by remember { mutableStateOf(AISettings.baseUrl.getValue()) }
    var showKey by remember { mutableStateOf(false) }

    var models by remember { mutableStateOf<List<AIModelInfo>>(emptyList()) }
    var modelFilter by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val selectedModel = AISettings.model.state
    val permissionMode = AISettings.permissionMode.state

    // #12：写操作审计
    var auditCount by remember { mutableStateOf(0) }
    var auditRecords by remember { mutableStateOf<List<AIAuditRecord>>(emptyList()) }
    var showAudit by remember { mutableStateOf(false) }
    var auditMessage by remember { mutableStateOf<String?>(null) }

    // #10：首次打开时说明「内容会发往你配置的服务商」，确认后不再打扰
    var showPrivacyNotice by remember {
        mutableStateOf(!AISettings.privacyNoticeShown.getValue())
    }

    fun dismissPrivacyNotice() {
        AISettings.privacyNoticeShown.save(true)
        showPrivacyNotice = false
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(all = 12.dp)
    ) {
            // ── 2. API Key ────────────────────────────────────────
            ConfigCard {
                SectionTitle(stringResource(R.string.ai_config_api_key))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = apiKey,
                    onValueChange = {
                        apiKey = it
                        AISettings.saveKey(it)
                    },
                    label = { Text(stringResource(R.string.ai_config_api_key)) },
                    placeholder = { Text(stringResource(R.string.ai_config_api_key_hint)) },
                    singleLine = true,
                    //默认把 Key 遮住：截图 / 旁人瞄一眼都不会泄露
                    visualTransformation = if (showKey) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                painter = painterResource(
                                    if (showKey) R.drawable.ic_visibility_off_outlined
                                    else R.drawable.ic_visibility_outlined
                                ),
                                contentDescription = stringResource(
                                    if (showKey) R.string.ai_config_key_hide
                                    else R.string.ai_config_key_show
                                )
                            )
                        }
                    },
                )

                HintText(stringResource(R.string.ai_config_api_key_note))
            }

            Spacer(Modifier.height(8.dp))

            // ── 3. Base URL ───────────────────────────────────────
            ConfigCard {
                SectionTitle(stringResource(R.string.ai_config_base_url))
                OwnOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = baseUrl,
                    onValueChange = {
                        baseUrl = it
                        AISettings.saveBaseUrl(it)
                    },
                    placeholder = { Text(AISettings.DEFAULT_BASE_URL) },
                    singleLine = true,
                    hidePlaceholderOnFocus = true,
                    shape = MaterialTheme.shapes.large
                )
                HintText(stringResource(R.string.settings_ai_base_url_hint))
            }

            Spacer(Modifier.height(8.dp))

            // ── 4. Model（动态获取，不硬编码）──────────────────────
            ConfigCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(
                        text = stringResource(R.string.ai_config_model),
                        modifier = Modifier.weight(1f)
                    )
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    FilledTonalButton(
                        enabled = !loading && apiKey.isNotBlank(),
                        onClick = {
                            loading = true
                            message = null
                            scope.launch {
                                runCatching {
                                    AIModelRepository.fetchAndEnsureSelection()
                                }.onSuccess { list ->
                                    models = list
                                    message = if (list.isEmpty()) {
                                        context.getString(R.string.ai_config_models_empty_from_api)
                                    } else null
                                }.onFailure { e ->
                                    models = emptyList()
                                    message = e.message
                                        ?: context.getString(R.string.ai_config_models_fetch_failed)
                                }
                                loading = false
                            }
                        }
                    ) {
                        Text(stringResource(R.string.ai_config_fetch_models))
                    }
                }

                message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (models.isEmpty()) {
                    HintText(stringResource(R.string.ai_config_models_empty))
                } else {
                    // 模型可能很多，给一个过滤框；列表本身限高，避免把整页撑得很长
                    SmallOutlinedEditField(
                        modifier = Modifier.fillMaxWidth(),
                        value = modelFilter,
                        onValueChange = { modelFilter = it },
                        placeholder = { Text(stringResource(R.string.ai_config_model_filter)) },
                        singleLine = true,
                    )
                    Spacer(Modifier.height(6.dp))

                    val shown = remember(models, modelFilter) {
                        if (modelFilter.isBlank()) models
                        else models.filter { it.id.contains(modelFilter, ignoreCase = true) }
                    }

                    if (shown.isEmpty()) {
                        HintText(stringResource(R.string.ai_config_model_filter_empty))
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f))
                        ) {
                            AiChoiceFlow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(8.dp)
                            ) {
                                shown.forEach { model ->
                                    AiChoicePill(
                                        text = model.displayName,
                                        selected = selectedModel == model.id,
                                        onClick = { AISettings.model.save(model.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── 5. Agent 权限模式 ────────────────────────────────
            ConfigCard {
                SectionTitle(stringResource(R.string.ai_config_permission))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AIPermissionMode.entries.forEach { mode ->
                        PermissionOption(
                            mode = mode,
                            selected = permissionMode == mode,
                            onClick = { AISettings.permissionMode.save(mode) }
                        )
                    }
                }
                HintText(
                    stringResource(
                        if (permissionMode == AIPermissionMode.FULL_CONTROL)
                            R.string.ai_config_permission_full_note
                        else
                            R.string.ai_config_permission_confirm_note
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── 网络搜索（议题 #45）──────────────────────────────
            ConfigCard {
                SectionTitle(stringResource(R.string.ai_web_title))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.ai_web_search_title),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(R.string.ai_web_search_desc),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.labelSmall.fontSize * 1.35f
                        )
                    }
                    Switch(
                        checked = AISettings.webSearch.state,
                        onCheckedChange = { AISettings.webSearch.save(it) }
                    )
                }
                HintText(stringResource(R.string.ai_web_search_note))

                //搜索引擎选择 + 「创建 API」直链
                val engine = AISettings.searchEngine.state
                SectionTitle(stringResource(R.string.ai_web_engine_title))
                AiChoiceFlow {
                    listOf(
                        "duckduckgo" to R.string.ai_web_engine_ddg,
                        "baidu" to R.string.ai_web_engine_baidu,
                        "bing" to R.string.ai_web_engine_bing,
                        "custom" to R.string.ai_web_engine_custom
                    ).forEach { entry ->
                        AiChoicePill(
                            text = stringResource(entry.second),
                            selected = engine == entry.first,
                            onClick = { AISettings.searchEngine.save(entry.first) }
                        )
                    }
                }

                if (engine != "duckduckgo") {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        value = AISettings.searchApiKey.state,
                        onValueChange = { AISettings.searchApiKey.save(it) },
                        label = { Text(stringResource(R.string.ai_web_api_key)) },
                        placeholder = { Text(stringResource(R.string.ai_web_api_key_hint)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(onClick = {
                                val url = when (engine) {
                                    "baidu" -> "https://console.bce.baidu.com/qianfan/ais/console/apiKey"
                                    "bing" -> "https://portal.azure.com/#create/Microsoft.CognitiveServicesBingSearch-v7"
                                    else -> "https://github.com/"
                                }
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                            }) {
                                Text(stringResource(R.string.ai_web_create_api))
                            }
                        }
                    )
                    if (engine == "custom") {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            value = AISettings.searchBaseUrl.state,
                            onValueChange = { AISettings.searchBaseUrl.save(it) },
                            label = { Text(stringResource(R.string.ai_web_base_url)) },
                            placeholder = { Text("https://your-search-api.example.com/search") },
                            singleLine = true
                        )
                    }
                    HintText(stringResource(R.string.ai_web_key_note))
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── #12 操作审计 ──────────────────────────────────────
            ConfigCard {
                SectionTitle(stringResource(R.string.ai_audit_title))
                HintText(stringResource(R.string.ai_audit_note))

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = {
                        auditRecords = AIAuditLog.readAll()
                        showAudit = true
                    }) {
                        Text(stringResource(R.string.ai_audit_view, auditCount))
                    }
                    TextButton(onClick = {
                        runCatching { AIAuditLog.exportToFile() }
                            .onSuccess { file ->
                                auditMessage = null
                                shareFile(
                                    context = context,
                                    file = file,
                                    cantProcess = {
                                        auditMessage = context.getString(
                                            R.string.ai_audit_share_failed
                                        )
                                    }
                                )
                            }
                            .onFailure { e ->
                                auditMessage = e.message
                            }
                    }) {
                        Text(stringResource(R.string.ai_audit_export))
                    }
                }

                auditMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
    }

    // 审计记录变化时刷新条数（放到 IO 上，别卡主线程）
    LaunchedEffect(showAudit) {
        auditCount = withContext(Dispatchers.IO) { AIAuditLog.count() }
    }

    // ── #12 审计记录查看 ─────────────────────────────────────────
    if (showAudit) {
        AlertDialog(
            onDismissRequest = { showAudit = false },
            title = { Text(stringResource(R.string.ai_audit_title)) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (auditRecords.isEmpty()) {
                            stringResource(R.string.ai_audit_empty)
                        } else {
                            auditRecords.joinToString("\n") { it.toText() }
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    AIAuditLog.clear()
                    auditRecords = emptyList()
                    auditCount = 0
                    showAudit = false
                }) {
                    Text(
                        text = stringResource(R.string.ai_audit_clear),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showAudit = false }) {
                    Text(stringResource(R.string.generic_cancel))
                }
            }
        )
    }

    // ── #10 数据流向说明（只在首次打开时弹一次）──────────────────
    if (showPrivacyNotice) {
        AlertDialog(
            onDismissRequest = { dismissPrivacyNotice() },
            title = { Text(stringResource(R.string.ai_privacy_title)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = stringResource(R.string.ai_privacy_message),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { dismissPrivacyNotice() }) {
                    Text(stringResource(R.string.ai_privacy_confirm))
                }
            }
        )
    }
}

/**
 * 配置卡片统一外观。
 *
 * ⚠️ 两个细节缺一不可：
 * 1. [Modifier.fillMaxWidth]：卡片默认按内容宽度收缩，会让「操作审计」这类
 *    文字较少的卡片明显比别人窄，页面看起来参差不齐；
 * 2. 内边距：Card 会把内容裁到圆角内，没有内边距时标题贴边，
 *    圆角处就会把文字切掉一半（议题 #25 的「文字被垂直裁切」）。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiChoiceFlow(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.FlowRowScope.() -> Unit,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

/**
 * 与「设置 → 基础设置 → 颜色主题」同一种选择样式：
 * 紧凑的圆形单选 + 文字，不再用整行大卡片堆在一起。
 */
@Composable
private fun AiChoicePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
    }
}

/**
 * 权限模式选项卡（议题：设置 UI 美化）。
 *
 * 以前是两个小药丸并排，**看不出「完全放开」到底有多危险**，也没有当前状态的强调。
 * 现在改成整行可选卡片：左图标 + 标题 + 一句人话解释 + 右侧单选圆点，
 * 「完全放开」统一用错误色，避免用户随手点过去。
 */
@Composable
private fun PermissionOption(
    mode: AIPermissionMode,
    selected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val dangerous = mode == AIPermissionMode.FULL_CONTROL
    val accent = if (dangerous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val description = stringResource(
        if (dangerous) R.string.ai_permission_full_control_desc else R.string.ai_permission_confirm_desc
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) accent.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) accent.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(
                    if (dangerous) R.drawable.ic_warning_outlined else R.drawable.ic_check
                ),
                contentDescription = null,
                tint = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mode.displayName.toAndroidString(context),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) accent else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.labelSmall.fontSize * 1.35f
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = accent)
            )
        }
    }
}

@Composable
private fun ConfigCard(content: @Composable ColumnScope.() -> Unit) {
    BackgroundCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), content = content)
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(top = 2.dp, bottom = 6.dp),
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun HintText(text: String) {
    Text(
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.4f
    )
}
