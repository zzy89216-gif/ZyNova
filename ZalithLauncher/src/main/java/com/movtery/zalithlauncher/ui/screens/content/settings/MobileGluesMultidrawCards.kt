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

package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.renderer.mobileglues.MobileGluesMultidraw
import com.movtery.zalithlauncher.game.renderer.mobileglues.MultidrawAlgorithm
import com.movtery.zalithlauncher.game.renderer.mobileglues.MultidrawOverride
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard

/**
 * MobileGlues 2.0.1 的 multidraw 策略顺序。
 *
 * 默认全局顺序由 MobileGlues 决定，用户只在遇到「区块/实体绘制异常」时才需要动它；
 * 因此这里收在高级参数的最后，不占主设置页的位置。
 */
@Composable
fun MobileGluesMultidrawCards(modifier: Modifier = Modifier) {
    var editing by remember { mutableStateOf<EditTarget?>(null) }

    SettingsCardColumn(modifier = modifier) {
        SettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Top,
            title = stringResource(R.string.settings_mg_multidraw_title),
            summary = stringResource(R.string.settings_mg_multidraw_summary),
            onClick = { editing = EditTarget(null) }
        )

        MobileGluesMultidraw.OVERRIDES.forEachIndexed { index, overrideDef ->
            val checked = enabledOf(index)
            SwitchSettingsCard(
                modifier = Modifier.fillMaxWidth(),
                position = if (index == MobileGluesMultidraw.OVERRIDES.lastIndex) {
                    CardPosition.Bottom
                } else {
                    CardPosition.Middle
                },
                checked = checked,
                onCheckedChange = { value ->
                    setEnabled(index, value)
                    if (value) {
                        saveOrder(
                            index,
                            MobileGluesMultidraw.inheritOrder(
                                AllSettings.mobileGluesMultidrawOrder.state,
                                overrideDef
                            )
                        )
                    }
                },
                title = overrideDef.title,
                summary = stringResource(
                    if (checked) R.string.settings_mg_multidraw_override_enabled
                    else R.string.settings_mg_multidraw_override_disabled
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { editing = EditTarget(overrideDef) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings_filled),
                            contentDescription = stringResource(R.string.settings_mg_multidraw_edit)
                        )
                    }
                }
            )
        }
    }

    editing?.let { target ->
        MultidrawOrderDialog(
            overrideDef = target.overrideDef,
            onDismiss = { editing = null },
            onSave = { enabled, order ->
                val index = target.overrideDef?.let { MobileGluesMultidraw.OVERRIDES.indexOf(it) }
                if (index == null) {
                    AllSettings.mobileGluesMultidrawOrder.save(
                        MobileGluesMultidraw.serialize(order)
                    )
                } else {
                    setEnabled(index, enabled)
                    saveOrder(index, order)
                }
                editing = null
            }
        )
    }
}

private data class EditTarget(val overrideDef: MultidrawOverride?)

@Composable
private fun MultidrawOrderDialog(
    overrideDef: MultidrawOverride?,
    onDismiss: () -> Unit,
    onSave: (Boolean, List<MultidrawAlgorithm>) -> Unit,
) {
    val options = overrideDef?.options ?: MultidrawAlgorithm.entries
    val globalOrder = AllSettings.mobileGluesMultidrawOrder.state
    var enabled by remember(overrideDef) {
        mutableStateOf(overrideDef?.let { enabledOf(MobileGluesMultidraw.OVERRIDES.indexOf(it)) } ?: true)
    }
    var order by remember(overrideDef) {
        mutableStateOf(
            if (overrideDef == null) {
                MobileGluesMultidraw.parse(globalOrder)
            } else {
                MobileGluesMultidraw.parse(
                    raw = orderOf(MobileGluesMultidraw.OVERRIDES.indexOf(overrideDef)),
                    options = overrideDef.options,
                    fallback = overrideDef.defaultOrder
                )
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = overrideDef?.title ?: stringResource(R.string.settings_mg_multidraw_title))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (overrideDef != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.settings_mg_multidraw_override_toggle),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = enabled,
                            onCheckedChange = { value ->
                                enabled = value
                                if (value) {
                                    order = MobileGluesMultidraw.inheritOrder(globalOrder, overrideDef)
                                }
                            }
                        )
                    }
                }

                options.forEachIndexed { index, algorithm ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = algorithm.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(
                            enabled = (overrideDef == null || enabled) && index > 0,
                            onClick = {
                                order = order.toMutableList().also {
                                    val tmp = it[index - 1]
                                    it[index - 1] = it[index]
                                    it[index] = tmp
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_upward),
                                contentDescription = null
                            )
                        }
                        IconButton(
                            enabled = (overrideDef == null || enabled) && index < options.lastIndex,
                            onClick = {
                                order = order.toMutableList().also {
                                    val tmp = it[index + 1]
                                    it[index + 1] = it[index]
                                    it[index] = tmp
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_downward),
                                contentDescription = null
                            )
                        }
                    }
                }

                Spacer(Modifier.width(1.dp))
                TextButton(
                    onClick = {
                        order = if (overrideDef == null) {
                            MobileGluesMultidraw.DEFAULT_ORDER
                        } else if (enabled) {
                            MobileGluesMultidraw.inheritOrder(globalOrder, overrideDef)
                        } else {
                            overrideDef.defaultOrder
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.generic_reset))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val normalized = MobileGluesMultidraw.parse(
                        raw = MobileGluesMultidraw.serialize(order),
                        options = options,
                        fallback = overrideDef?.defaultOrder ?: MobileGluesMultidraw.DEFAULT_ORDER
                    )
                    onSave(enabled, normalized)
                }
            ) {
                Text(text = stringResource(R.string.generic_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.generic_cancel))
            }
        }
    )
}

private fun enabledOf(index: Int): Boolean = when (index) {
    0 -> AllSettings.mobileGluesMultidrawArraysEnabled.state
    1 -> AllSettings.mobileGluesMultidrawElementsEnabled.state
    2 -> AllSettings.mobileGluesMultidrawElementsBaseVertexEnabled.state
    3 -> AllSettings.mobileGluesMultidrawArraysIndirectEnabled.state
    else -> AllSettings.mobileGluesMultidrawElementsIndirectEnabled.state
}

private fun setEnabled(index: Int, value: Boolean) {
    when (index) {
        0 -> AllSettings.mobileGluesMultidrawArraysEnabled.save(value)
        1 -> AllSettings.mobileGluesMultidrawElementsEnabled.save(value)
        2 -> AllSettings.mobileGluesMultidrawElementsBaseVertexEnabled.save(value)
        3 -> AllSettings.mobileGluesMultidrawArraysIndirectEnabled.save(value)
        else -> AllSettings.mobileGluesMultidrawElementsIndirectEnabled.save(value)
    }
}

private fun orderOf(index: Int): String = when (index) {
    0 -> AllSettings.mobileGluesMultidrawArraysOrder.state
    1 -> AllSettings.mobileGluesMultidrawElementsOrder.state
    2 -> AllSettings.mobileGluesMultidrawElementsBaseVertexOrder.state
    3 -> AllSettings.mobileGluesMultidrawArraysIndirectOrder.state
    else -> AllSettings.mobileGluesMultidrawElementsIndirectOrder.state
}

private fun saveOrder(index: Int, order: List<MultidrawAlgorithm>) {
    val value = MobileGluesMultidraw.serialize(order)
    when (index) {
        0 -> AllSettings.mobileGluesMultidrawArraysOrder.save(value)
        1 -> AllSettings.mobileGluesMultidrawElementsOrder.save(value)
        2 -> AllSettings.mobileGluesMultidrawElementsBaseVertexOrder.save(value)
        3 -> AllSettings.mobileGluesMultidrawArraysIndirectOrder.save(value)
        else -> AllSettings.mobileGluesMultidrawElementsIndirectOrder.save(value)
    }
}
