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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ai.AIModelRepository
import com.movtery.zalithlauncher.ai.AIPermissionMode
import com.movtery.zalithlauncher.ai.AISettings
import com.movtery.zalithlauncher.ai.model.AIModelInfo
import com.movtery.zalithlauncher.ai.provider.AIProviders
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.RadioCard
import com.movtery.zalithlauncher.ui.components.SmallOutlinedEditField
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.toAndroidString
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.launch

/**
 * AI 配置界面
 *
 * ⚠️ 刻意**独立于启动器通用设置**：它由聊天界面右上角的入口进入，
 * 配置项存在 [AISettings] 里，不会出现在「设置」页面中。
 */
@Composable
fun AIConfigScreen(
    key: NormalNavKey.AIConfig,
    backStackViewModel: ScreenBackStackViewModel,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 当前 Provider（可观察）
    val provider = AISettings.provider.state

    var apiKey by remember(provider) { mutableStateOf(AISettings.getKey(provider)) }
    var baseUrl by remember(provider) { mutableStateOf(AISettings.getBaseUrl(provider)) }
    var showKey by remember { mutableStateOf(false) }

    var models by remember(provider) { mutableStateOf<List<AIModelInfo>>(emptyList()) }
    var modelFilter by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val selectedModel = AISettings.model.state
    val permissionMode = AISettings.permissionMode.state

    // 切换 Provider 时清掉上一家的模型列表，避免列表与 Provider 对不上
    LaunchedEffect(provider) {
        models = emptyList()
        modelFilter = ""
        message = null
    }

    BaseScreen(
        screenKey = key,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ── 1. AI Provider ────────────────────────────────────
            BackgroundCard {
                SectionTitle(stringResource(R.string.ai_config_provider))
                AIProviders.available.forEach { type ->
                    RadioCard(
                        selected = provider == type,
                        text = type.displayName,
                        onClick = {
                            if (provider != type) {
                                // 先保存当前输入的 Key / Base URL，再切走
                                AISettings.saveKey(provider, apiKey)
                                AISettings.saveBaseUrl(provider, baseUrl)
                                AISettings.switchProvider(type)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── 2. API Key ────────────────────────────────────────
            BackgroundCard {
                SectionTitle(stringResource(R.string.ai_config_api_key))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = apiKey,
                    onValueChange = {
                        apiKey = it
                        AISettings.saveKey(provider, it)
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

            Spacer(Modifier.height(12.dp))

            // ── 3. Base URL ───────────────────────────────────────
            BackgroundCard {
                SectionTitle(stringResource(R.string.ai_config_base_url))
                SmallOutlinedEditField(
                    modifier = Modifier.fillMaxWidth(),
                    value = baseUrl,
                    onValueChange = {
                        baseUrl = it
                        AISettings.saveBaseUrl(provider, it)
                    },
                    label = { Text(stringResource(R.string.ai_config_base_url)) },
                    placeholder = { Text(provider.defaultBaseUrl) },
                    singleLine = true,
                )
                HintText(stringResource(R.string.ai_config_base_url_note))
            }

            Spacer(Modifier.height(12.dp))

            // ── 4. Model（动态获取，不硬编码）──────────────────────
            BackgroundCard {
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
                    TextButton(
                        enabled = !loading && apiKey.isNotBlank(),
                        onClick = {
                            loading = true
                            message = null
                            scope.launch {
                                runCatching {
                                    AIModelRepository.fetchAndEnsureSelection(provider)
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
                        label = { Text(stringResource(R.string.ai_config_model_filter)) },
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
                                .heightIn(max = 320.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                shown.forEach { model ->
                                    RadioCard(
                                        selected = selectedModel == model.id,
                                        text = model.displayName,
                                        onClick = { AISettings.model.save(model.id) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── 5. Agent 权限模式 ────────────────────────────────
            BackgroundCard {
                SectionTitle(stringResource(R.string.ai_config_permission))
                AIPermissionMode.entries.forEach { mode ->
                    RadioCard(
                        selected = permissionMode == mode,
                        text = mode.displayName.toAndroidString(context),
                        onClick = { AISettings.permissionMode.save(mode) },
                        modifier = Modifier.fillMaxWidth()
                    )
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

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(bottom = 6.dp),
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun HintText(text: String) {
    Text(
        modifier = Modifier.padding(top = 4.dp),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
