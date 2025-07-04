/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.sound

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.Preset
import com.fairphone.spring.launcher.data.model.SoundSettingVO
import com.fairphone.spring.launcher.data.model.protos.SoundSetting
import com.fairphone.spring.launcher.data.model.toVO
import com.fairphone.spring.launcher.ui.FP6Preview
import com.fairphone.spring.launcher.ui.FP6PreviewDark
import com.fairphone.spring.launcher.ui.component.RadioButtonListItem
import com.fairphone.spring.launcher.ui.theme.FairphoneTypography
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme

@Composable
fun SoundVibrationSettingsScreen(
    screenState: SoundVibrationSettingsScreenState,
    onSoundSettingsSelected: (SoundSetting) -> Unit
) {
    when (screenState) {
        is SoundVibrationSettingsScreenState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize()
            )
        }

        is SoundVibrationSettingsScreenState.Success -> {
            SoundVibrationSettingsScreen(
                activeSoundSetting = screenState.activeSoundSetting,
                allowedSoundSettings = screenState.allowedSoundSettings,
                onSoundSettingsSelected = onSoundSettingsSelected
            )
        }
    }
}

@Composable
fun SoundVibrationSettingsScreen(
    activeSoundSetting: SoundSetting,
    allowedSoundSettings: List<SoundSettingVO>,
    onSoundSettingsSelected: (SoundSetting) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {

        Text(
            text = stringResource(R.string.sound_and_vibration_description),
            style = FairphoneTypography.BodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
        ) {
            allowedSoundSettings.forEach { setting ->
                val isSelected = setting.soundSetting == activeSoundSetting
                RadioButtonListItem(
                    title = stringResource(setting.titleResource),
                    subtitle = stringResource(setting.subtitleResource),
                    isSelected = isSelected,
                    onClick = { onSoundSettingsSelected(setting.soundSetting) }
                )
            }
        }
    }
}

@Composable
fun SoundVibrationSettingsScreen_Preview() {
    SpringLauncherTheme {
        SoundVibrationSettingsScreen(
            screenState = SoundVibrationSettingsScreenState.Success(
                activeProfileName = Preset.QualityTime.name,
                activeSoundSetting = SoundSetting.SOUND_SETTING_SILENT,
                allowedSoundSettings = SoundVibrationSettingsViewModel.ALLOWED_SOUND_SETTINGS.map { it.toVO() }
            ),
            onSoundSettingsSelected = {}
        )
    }
}

@Composable
@FP6Preview()
fun SoundVibrationSettingsScreen_PreviewLight() {
    SoundVibrationSettingsScreen_Preview()
}

@Composable
@FP6PreviewDark()
fun SoundVibrationSettingsScreen_PreviewDark() {
    SoundVibrationSettingsScreen_Preview()
}