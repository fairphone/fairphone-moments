/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.motion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.protos.MotionMode
import com.fairphone.spring.launcher.data.model.protos.MotionTrigger
import com.fairphone.spring.launcher.ui.FP6Preview
import com.fairphone.spring.launcher.ui.FP6PreviewDark
import com.fairphone.spring.launcher.ui.component.SettingSwitchItem
import com.fairphone.spring.launcher.ui.theme.Color_FP_Brand_Lime
import com.fairphone.spring.launcher.ui.theme.FairphoneTypography
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme

/** The motion modes a user can bind a Moment to, in display order. */
private val SELECTABLE_MODES = listOf(
    MotionMode.MOTION_MODE_WALKING,
    MotionMode.MOTION_MODE_TRANSPORT,
)

@Composable
fun MotionTriggerSettingsScreen(
    screenState: MotionTriggerSettingsScreenState,
    onTriggerEnabledChanged: (Boolean) -> Unit,
    onModeToggled: (MotionMode) -> Unit,
) {
    when (screenState) {
        is MotionTriggerSettingsScreenState.Loading ->
            Box(modifier = Modifier.fillMaxSize())

        is MotionTriggerSettingsScreenState.Success ->
            MotionTriggerSettingsScreen(
                motionTrigger = screenState.motionTrigger,
                onTriggerEnabledChanged = onTriggerEnabledChanged,
                onModeToggled = onModeToggled,
            )
    }
}

@Composable
fun MotionTriggerSettingsScreen(
    motionTrigger: MotionTrigger,
    onTriggerEnabledChanged: (Boolean) -> Unit,
    onModeToggled: (MotionMode) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.motion_trigger_description),
            style = FairphoneTypography.BodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp)
        )

        SettingSwitchItem(
            state = motionTrigger.enabled,
            title = stringResource(R.string.motion_trigger_enable_title),
            subtitle = stringResource(R.string.motion_trigger_enable_subtitle),
            onClick = onTriggerEnabledChanged,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        )

        if (motionTrigger.enabled) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                SELECTABLE_MODES.forEach { mode ->
                    ModeChip(
                        label = stringResource(modeLabel(mode)),
                        selected = mode in motionTrigger.modesList,
                        onClick = { onModeToggled(mode) },
                    )
                }
            }
            Text(
                text = stringResource(R.string.motion_trigger_modes_hint),
                style = FairphoneTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color_FP_Brand_Lime else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) Color_FP_Brand_Lime else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = label,
            style = FairphoneTypography.BodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.scrim else MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun modeLabel(mode: MotionMode): Int = when (mode) {
    MotionMode.MOTION_MODE_WALKING -> R.string.motion_mode_walking
    MotionMode.MOTION_MODE_TRANSPORT -> R.string.motion_mode_transport
    else -> R.string.motion_mode_walking
}

@Composable
private fun MotionTriggerSettingsScreen_Preview() {
    SpringLauncherTheme {
        MotionTriggerSettingsScreen(
            screenState = MotionTriggerSettingsScreenState.Success(
                motionTrigger = MotionTrigger.newBuilder()
                    .setEnabled(true)
                    .addModes(MotionMode.MOTION_MODE_TRANSPORT)
                    .build(),
            ),
            onTriggerEnabledChanged = {},
            onModeToggled = {},
        )
    }
}

@Composable
@FP6Preview()
private fun MotionTriggerSettingsScreen_LightPreview() {
    MotionTriggerSettingsScreen_Preview()
}

@Composable
@FP6PreviewDark()
private fun MotionTriggerSettingsScreen_DarkPreview() {
    MotionTriggerSettingsScreen_Preview()
}
