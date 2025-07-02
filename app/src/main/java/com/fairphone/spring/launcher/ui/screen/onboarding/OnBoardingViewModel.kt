/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.onboarding

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.data.model.LAUNCHER_MAX_APP_COUNT
import com.fairphone.spring.launcher.data.model.LauncherColors
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.model.protos.LauncherProfileApp
import com.fairphone.spring.launcher.data.model.toLauncherProfileApp
import com.fairphone.spring.launcher.data.repository.AppInfoRepository
import com.fairphone.spring.launcher.domain.usecase.profile.GetActiveProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.UpdateLauncherProfileUseCase
import com.fairphone.spring.launcher.ui.icons.mode.ModeIcon
import com.fairphone.spring.launcher.ui.screen.settings.apps.selector.ScreenData
import com.fairphone.spring.launcher.ui.screen.settings.apps.selector.VisibleAppSelectorScreenState
import com.fairphone.spring.launcher.ui.screen.settings.apps.selector.updateAppSelectorState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnBoardingViewModel(
    private val appInfoRepository: AppInfoRepository,
    private val updateLauncherProfileUseCase: UpdateLauncherProfileUseCase,
    private val getActiveProfileUseCase: GetActiveProfileUseCase
) : ViewModel() {

    var profileName: String by mutableStateOf("")
        private set

    var profileIcon: ModeIcon by mutableStateOf(ModeIcon.customIcons().random())
        private set

    val launcherProfileApps: MutableList<LauncherProfileApp> = mutableStateListOf()

    var backgroundColors: LauncherColors by mutableStateOf(LauncherColors(0L, 0L))
        private set

    fun updateName(name: String) {
        this.profileName = name
    }

    fun updateIcon(icon: ModeIcon) {
        this.profileIcon = icon
    }

    fun updateLauncherProfileApps(visibleApps: List<AppInfo>) {
        this.launcherProfileApps.clear()
        this.launcherProfileApps.addAll(visibleApps.map { it.toLauncherProfileApp() })
    }

    fun updateBackgroundColors(colors: LauncherColors) {
        this.backgroundColors = colors
    }

    val activeProfile: StateFlow<LauncherProfile?> =
        getActiveProfileUseCase.execute(Unit)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = null,
            )

    /**
     * Used to update the current mode at the end of the onboarding process
     */
    fun updateProfile() = viewModelScope.launch {
        _updateModeState.update { UpdateModeState.Loading }
        val profile = getActiveProfileUseCase.execute(Unit).first()
        val updatedProfile = profile
            .toBuilder()
            .setName(profileName)
            .setBgColor1(backgroundColors.leftColor)
            .setBgColor2(backgroundColors.rightColor)
            .setIcon(profileIcon.name)
            .clearLauncherProfileApps()
            .addAllLauncherProfileApps(launcherProfileApps)
            .build()
        val result = updateLauncherProfileUseCase.execute(updatedProfile)

        if (result.isFailure) {
            _updateModeState.update { UpdateModeState.Error(result.exceptionOrNull()) }
        } else {
            _updateModeState.update { UpdateModeState.Success }
        }
    }


    // TODO: This should be refactored / cleaned up
    private val _appSelectorScreenState: MutableStateFlow<VisibleAppSelectorScreenState> =
        MutableStateFlow(VisibleAppSelectorScreenState.Loading)
    val appSelectorScreenState = _appSelectorScreenState.asStateFlow()

    private lateinit var screenData: ScreenData

    var installedApps = mutableStateListOf<AppInfo>()
    var visibleApps = mutableStateListOf<AppInfo>()

    fun loadApps(context: Context) = viewModelScope.launch {
        installedApps.addAll(appInfoRepository.getAllInstalledApps(context))
        // On Init we only initialize the installed apps on the device
        screenData = ScreenData(
            appList = installedApps,
            visibleApps = visibleApps,
            showConfirmButton = installedApps.isNotEmpty(),
            showAppCounter = visibleApps.size == LAUNCHER_MAX_APP_COUNT,
            showEmptyAppSelectedError = false,
            showMaxAppSelectedError = false,
            confirmButtonTextResource = R.string.bt_continue
        )

        _appSelectorScreenState.update {
            // Elements ae loaded, and the data will be ready when the updateSelectProfile
            // function will be called
            VisibleAppSelectorScreenState.Ready(data = screenData)
        }
    }

    fun onAppClick(appInfo: AppInfo) {
        if (appInfo in visibleApps) {
            removeVisibleApp(appInfo)
            return
        }
        if (visibleApps.size < LAUNCHER_MAX_APP_COUNT) {
            visibleApps.add(appInfo)
            _appSelectorScreenState.updateAppSelectorState {
                screenData.copy(
                    visibleApps = visibleApps,
                    showConfirmButton = true,
                    showAppCounter = visibleApps.size == LAUNCHER_MAX_APP_COUNT,
                    showEmptyAppSelectedError = false,
                    showMaxAppSelectedError = false,
                )
            }
        } else if (visibleApps.size == LAUNCHER_MAX_APP_COUNT) {
            viewModelScope.launch {
                _appSelectorScreenState.updateAppSelectorState {
                    screenData.copy(showMaxAppSelectedError = true)
                }
                delay(3000)
                _appSelectorScreenState.updateAppSelectorState {
                    screenData.copy(showMaxAppSelectedError = false)
                }
            }

        }
    }

    fun removeVisibleApp(appInfo: AppInfo) {
        visibleApps.remove(appInfo)
        _appSelectorScreenState.updateAppSelectorState {
            screenData.copy(
                visibleApps = visibleApps,
                showConfirmButton = visibleApps.isNotEmpty(),
                showAppCounter = false,
                showEmptyAppSelectedError = visibleApps.isEmpty(),
                showMaxAppSelectedError = false,
            )
        }
    }

    private val _updateModeState: MutableStateFlow<UpdateModeState> =
        MutableStateFlow(UpdateModeState.Loading)
    val updateModeState = _updateModeState.asStateFlow()
}

sealed class UpdateModeState {
    data object Loading : UpdateModeState()
    data object Success : UpdateModeState()
    data class Error(val exception: Throwable?) : UpdateModeState()
}