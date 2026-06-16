/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.appearance

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.data.model.LauncherColors
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.domain.usecase.profile.GetActiveProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.UpdateLauncherProfileUseCase
import com.fairphone.spring.launcher.util.BackgroundImageStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WallpaperSettingsViewModel(
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val updateLauncherProfileUseCase: UpdateLauncherProfileUseCase,
    private val backgroundImageStore: BackgroundImageStore,
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

    /** Saves a picked photo as this Moment's background. */
    fun setBackgroundImage(uri: Uri) = viewModelScope.launch {
        val profile = editedProfile.value ?: return@launch
        val path = backgroundImageStore.save(profile.id, uri) ?: return@launch
        updateLauncherProfileUseCase.execute(
            profile.toBuilder().setBackgroundImagePath(path).build()
        )
    }

    /** Removes this Moment's background photo, falling back to the gradient. */
    fun clearBackgroundImage() = viewModelScope.launch {
        val profile = editedProfile.value ?: return@launch
        backgroundImageStore.delete(profile.id)
        updateLauncherProfileUseCase.execute(
            profile.toBuilder().clearBackgroundImagePath().build()
        )
    }
}

sealed class WallpaperSettingScreenState {
    data object Loading : WallpaperSettingScreenState()
    data object Success : WallpaperSettingScreenState()
    data class Error(val exception: Throwable?) : WallpaperSettingScreenState()
}
