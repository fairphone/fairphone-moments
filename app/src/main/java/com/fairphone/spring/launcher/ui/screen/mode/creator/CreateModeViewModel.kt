/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.mode.creator

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.data.model.CreateLauncherProfile
import com.fairphone.spring.launcher.data.model.Defaults
import com.fairphone.spring.launcher.data.model.LAUNCHER_MAX_APP_COUNT
import com.fairphone.spring.launcher.data.model.LauncherColors
import com.fairphone.spring.launcher.data.model.Preset
import com.fairphone.spring.launcher.data.model.protos.LauncherProfileApp
import com.fairphone.spring.launcher.data.model.toLauncherProfileApp
import com.fairphone.spring.launcher.data.repository.AppInfoRepository
import com.fairphone.spring.launcher.domain.usecase.profile.CreateLauncherProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetEditedProfileUseCase
import com.fairphone.spring.launcher.ui.icons.mode.ModeIcon
import com.fairphone.spring.launcher.ui.screen.settings.apps.selector.ScreenData
import com.fairphone.spring.launcher.ui.screen.settings.apps.selector.VisibleAppSelectorScreenState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel used for the mode creation wizard.
 * It handles the creation of a new launcher profile.
 */
class CreateModeViewModel(
    private val appInfoRepository: AppInfoRepository,
    private val createLauncherProfileUseCase: CreateLauncherProfileUseCase,
    private val setEditedProfileUseCase: SetEditedProfileUseCase,
) : ViewModel() {

    var selectedPreset: Preset by mutableStateOf(Preset.Custom)
        private set

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

    fun onPresetSelected(context: Context, preset: Preset) {
        this.selectedPreset = preset

        if (preset != Preset.Custom) {
            updateName(context.getString(preset.title))
        } else {
            updateName("")
        }
        updateIcon(ModeIcon.fromString(preset.icon))
        loadApps(context)
    }

    fun updateLauncherVisibleApps(visibleApps: List<AppInfo>) {
        this.launcherProfileApps.clear()
        this.launcherProfileApps.addAll(visibleApps.map { it.toLauncherProfileApp() })
    }

    fun updateBackgroundColors(colors: LauncherColors) {
        this.backgroundColors = colors
    }

    private val _appSelectorScreenState: MutableStateFlow<VisibleAppSelectorScreenState> =
        MutableStateFlow(VisibleAppSelectorScreenState.Loading)
    val appSelectorScreenState = _appSelectorScreenState.asStateFlow()

    private lateinit var screenData: ScreenData

    private val installedApps = mutableStateListOf<AppInfo>()
    val visibleApps = mutableStateListOf<AppInfo>()

    fun loadApps(context: Context) = viewModelScope.launch {
        installedApps.clear()
        visibleApps.clear()

        installedApps.addAll(appInfoRepository.getAllInstalledApps(context))
        val apps = selectedPreset.getVisibleAppInfos(context, installedApps)
        visibleApps.addAll(apps)
        
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

    fun save() = viewModelScope.launch {
        _createModeState.update { CreateModeState.Loading }
        val params = CreateLauncherProfile(
            id = CreateLauncherProfileUseCase.newId(),
            name = profileName,
            icon = profileIcon.name,
            bgColor1 = backgroundColors.leftColor,
            bgColor2 = backgroundColors.rightColor,
            launcherProfileApps = launcherProfileApps,
            allowedContacts = Defaults.DEFAULT_ALLOWED_CONTACTS,
            repeatCallEnabled = Defaults.DEFAULT_REPEAT_CALL_ENABLED,
            wallpaperId = Defaults.DEFAULT_WALLPAPER_ID,
            uiMode = Defaults.DEFAULT_DARK_MODE_SETTING,
            grayScaleEnabled = Defaults.DEFAULT_GRAY_SCALE_ENABLED,
            blueLightFilterEnabled = Defaults.DEFAULT_BLUE_LIGHT_FILTER_ENABLED,
            soundSetting = Defaults.DEFAULT_SOUND_SETTING,
            batterySaverEnabled = Defaults.BATTERY_SAVER_ENABLED,
            reduceBrightnessEnabled = Defaults.REDUCE_BRIGHTNESS_ENABLED,
        )
        val result = createLauncherProfileUseCase.execute(params)
        setEditedProfileUseCase.execute(params.id)

        // TODO do we need more ?
        if (result.isFailure) {
            Log.e("App", "Failed to create initial profile", result.exceptionOrNull())
            _createModeState.update { CreateModeState.Error(result.exceptionOrNull()) }
        } else {
            _createModeState.update { CreateModeState.Success }
        }
    }

    private val _createModeState: MutableStateFlow<CreateModeState> =
        MutableStateFlow(CreateModeState.Loading)
    val createModeState = _createModeState.asStateFlow()
}

fun MutableStateFlow<VisibleAppSelectorScreenState>.updateAppSelectorState(
    screenDataModifier: () -> ScreenData
) {
    update {
        VisibleAppSelectorScreenState.Ready(
            screenDataModifier.invoke()
        )
    }
}

sealed class CreateModeState {
    data object Loading : CreateModeState()
    data object Success : CreateModeState()
    data class Error(val exception: Throwable?) : CreateModeState()
}
