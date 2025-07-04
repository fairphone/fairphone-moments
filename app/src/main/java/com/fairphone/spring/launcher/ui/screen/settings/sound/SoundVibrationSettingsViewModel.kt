/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.sound

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.data.model.SoundSettingVO
import com.fairphone.spring.launcher.data.model.protos.SoundSetting
import com.fairphone.spring.launcher.data.model.toVO
import com.fairphone.spring.launcher.domain.usecase.profile.GetEditedProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.UpdateLauncherProfileUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SoundVibrationSettingsViewModel(
    private val getEditedProfileUseCase: GetEditedProfileUseCase,
    private val updateLauncherProfileUseCase: UpdateLauncherProfileUseCase,
) : ViewModel() {

    val screenState: StateFlow<SoundVibrationSettingsScreenState> =
        getEditedProfileUseCase.execute(Unit).map { profile ->
            SoundVibrationSettingsScreenState.Success(
                activeProfileName = profile.name,
                activeSoundSetting = profile.soundSetting,
                allowedSoundSettings = ALLOWED_SOUND_SETTINGS.map { it.toVO() }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = SoundVibrationSettingsScreenState.Loading
        )

    fun onSoundSettingsSelected(soundSetting: SoundSetting) =
        viewModelScope.launch {
            val profile = getEditedProfileUseCase.execute(Unit).first()
            val updatedProfile = profile.toBuilder().setSoundSetting(soundSetting).build()
            updateLauncherProfileUseCase.execute(updatedProfile)
        }

    companion object {
        val ALLOWED_SOUND_SETTINGS = listOf(
            SoundSetting.SOUND_SETTING_FOLLOW_DEVICE_SETTINGS,
            SoundSetting.SOUND_SETTING_LOUD,
            SoundSetting.SOUND_SETTING_VIBRATE,
            SoundSetting.SOUND_SETTING_SILENT
        )
    }
}

sealed class SoundVibrationSettingsScreenState {
    data object Loading : SoundVibrationSettingsScreenState()
    data class Success(
        val activeProfileName: String,
        val activeSoundSetting: SoundSetting,
        val allowedSoundSettings: List<SoundSettingVO>,
    ) : SoundVibrationSettingsScreenState()
}
