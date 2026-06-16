/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.data.model.LauncherColors
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.domain.usecase.profile.GetActiveProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.GetAllProfilesUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.UpdateLauncherProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WallpaperSettingsViewModel(
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val getAllProfilesUseCase: GetAllProfilesUseCase,
    private val updateLauncherProfileUseCase: UpdateLauncherProfileUseCase,
) : ViewModel() {

    val editedProfile: StateFlow<LauncherProfile?> =
        getActiveProfileUseCase.execute(Unit)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private val _updateWallpaperState: MutableStateFlow<WallpaperSettingScreenState> =
        MutableStateFlow(WallpaperSettingScreenState.Loading)
    val updateWallpaperState = _updateWallpaperState.asStateFlow()


    fun updateProfileColors(colors: LauncherColors) = viewModelScope.launch {
        _updateWallpaperState.update { WallpaperSettingScreenState.Loading }
        val currentActiveProfile = editedProfile.value

        if (currentActiveProfile == null) {
            _updateWallpaperState.update {
                WallpaperSettingScreenState.Error(IllegalStateException("Cannot update: No active profile."))
            }
            return@launch
        }
        val updatedProfile = currentActiveProfile
            .toBuilder()
            .setBgColor1(colors.leftColor)
            .setBgColor2(colors.rightColor)
            .build()
        val result = updateLauncherProfileUseCase.execute(updatedProfile)

        if (result.isFailure) {
            _updateWallpaperState.update { WallpaperSettingScreenState.Error(result.exceptionOrNull()) }
        } else {
            _updateWallpaperState.update { WallpaperSettingScreenState.Success }
        }
    }

    /** Shows or hides the media-controls card for this Moment. */
    fun setMediaControlsEnabled(enabled: Boolean) = viewModelScope.launch {
        val profile = editedProfile.value ?: return@launch
        updateLauncherProfileUseCase.execute(
            profile.toBuilder().setMediaControlsDisabled(!enabled).build()
        )
    }

    /** Applies this Moment's media-controls choice to every Moment. */
    fun applyMediaControlsToAll() = viewModelScope.launch {
        val disabled = editedProfile.value?.mediaControlsDisabled ?: return@launch
        getAllProfilesUseCase.execute(Unit).first().forEach { profile ->
            if (profile.mediaControlsDisabled != disabled) {
                updateLauncherProfileUseCase.execute(
                    profile.toBuilder().setMediaControlsDisabled(disabled).build()
                )
            }
        }
    }
}

sealed class WallpaperSettingScreenState {
    data object Loading : WallpaperSettingScreenState()
    data object Success : WallpaperSettingScreenState()
    data class Error(val exception: Throwable?) : WallpaperSettingScreenState()
}
