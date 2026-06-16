/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.appearance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.ui.component.SettingListItem
import com.fairphone.spring.launcher.ui.component.SettingSwitchItem
import com.fairphone.spring.launcher.ui.theme.Color_FP_Brand_Lime
import com.fairphone.spring.launcher.ui.theme.FairphoneTypography

@Composable
fun AppearanceSettingsScreen(
    onCustomizeWallpaperClick: () -> Unit,
    mediaControlsEnabled: Boolean = true,
    onMediaControlsToggle: (Boolean) -> Unit = {},
    onApplyMediaControlsToAll: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.setting_subtitle_appearance),
            style = FairphoneTypography.BodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp)
        )

        SettingListItem(
            title = stringResource(R.string.wallpaper),
            subtitle = null,
            onClick = onCustomizeWallpaperClick,
            modifier = settingItemModifier(),
        )

        // The "apply to all" action lives inside this card and only folds out once the
        // switch has actually been changed, so it's clearly tied to this one setting.
        var mediaControlsChanged by remember { mutableStateOf(false) }
        Column(modifier = settingItemModifier()) {
            SettingSwitchItem(
                state = mediaControlsEnabled,
                title = stringResource(R.string.media_controls_setting_title),
                subtitle = stringResource(R.string.media_controls_setting_subtitle),
                onClick = {
                    mediaControlsChanged = true
                    onMediaControlsToggle(it)
                },
                modifier = Modifier.fillMaxWidth(),
            )
            AnimatedVisibility(visible = mediaControlsChanged) {
                TextButton(
                    onClick = {
                        onApplyMediaControlsToAll()
                        mediaControlsChanged = false
                    },
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.media_controls_apply_all),
                        style = FairphoneTypography.BodySmall,
                        color = Color_FP_Brand_Lime,
                    )
                }
            }
        }
    }
}

@Composable
private fun settingItemModifier(): Modifier = Modifier
    .fillMaxWidth()
    .border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outline,
        shape = RoundedCornerShape(size = 12.dp)
    )
    .clip(RoundedCornerShape(size = 12.dp))
