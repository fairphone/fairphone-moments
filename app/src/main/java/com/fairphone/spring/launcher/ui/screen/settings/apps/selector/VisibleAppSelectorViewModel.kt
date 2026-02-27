/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.apps.selector

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.data.model.LAUNCHER_MAX_APP_COUNT
import com.fairphone.spring.launcher.data.model.toLauncherProfileApp
import com.fairphone.spring.launcher.data.repository.AppInfoRepository
import com.fairphone.spring.launcher.domain.usecase.profile.GetEditedProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.UpdateLauncherProfileUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VisibleAppSelectorViewModel(
    context: Application,
    private val appInfoRepository: AppInfoRepository,
    private val getEditedProfileUseCase: GetEditedProfileUseCase,
    private val updateLauncherProfileUseCase: UpdateLauncherProfileUseCase,
) : ViewModel() {

    private val _screenState: MutableStateFlow<VisibleAppSelectorScreenState> =
        MutableStateFlow(VisibleAppSelectorScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    private var maxAppErrorJob: Job? = null

    init {
        initScreenData(context)
    }

    private fun initScreenData(context: Context) = viewModelScope.launch {
        val installedApps = appInfoRepository.getAllInstalledApps(context)
        val currentProfile = getEditedProfileUseCase.execute(Unit).first()
        val visibleApps = appInfoRepository.getAppInfosByProfileApps(
            context,
            currentProfile.launcherProfileAppsList
        )

        val screenData = ScreenData(
            appList = installedApps,
            visibleApps = visibleApps,
            showConfirmButton = false,
            showAppCounter = visibleApps.size == LAUNCHER_MAX_APP_COUNT,
            showEmptyAppSelectedError = false,
            showMaxAppSelectedError = false,
            confirmButtonTextResource = R.string.bt_confirm
        )

        _screenState.update {
            VisibleAppSelectorScreenState.Ready(screenData)
        }
    }

    fun onAppClick(appInfo: AppInfo) {
        val state = _screenState.value as? VisibleAppSelectorScreenState.Ready ?: return
        val currentData = state.data

        when {
            appInfo in currentData.visibleApps -> removeVisibleApp(appInfo)
            currentData.visibleApps.size < LAUNCHER_MAX_APP_COUNT -> addVisibleApp(appInfo)
            else -> showMaxAppError()
        }
    }

    fun addVisibleApp(appInfo: AppInfo) {
        val state = _screenState.value as? VisibleAppSelectorScreenState.Ready ?: return
        val currentData = state.data
        val newVisibleApps = currentData.visibleApps + appInfo

        _screenState.updateScreenData {
            copy(
                visibleApps = newVisibleApps,
                showConfirmButton = true,
                showAppCounter = true,
                showEmptyAppSelectedError = false,
                showMaxAppSelectedError = false,
            )
        }
    }

    fun removeVisibleApp(appInfo: AppInfo) {
        val state = _screenState.value as? VisibleAppSelectorScreenState.Ready ?: return
        val currentData = state.data
        val newVisibleApps = currentData.visibleApps - appInfo

        _screenState.updateScreenData {
            copy(
                visibleApps = newVisibleApps,
                showConfirmButton = newVisibleApps.isNotEmpty(),
                showAppCounter = true,
                showEmptyAppSelectedError = newVisibleApps.isEmpty(),
                showMaxAppSelectedError = false,
            )
        }
    }

    private fun showMaxAppError() {
        _screenState.updateScreenData { copy(showMaxAppSelectedError = true) }

        maxAppErrorJob?.cancel()
        maxAppErrorJob = viewModelScope.launch {
            delay(3000)
            _screenState.updateScreenData { copy(showMaxAppSelectedError = false) }
        }
    }

    fun confirmAppSelection() = viewModelScope.launch {
        val state = _screenState.value as? VisibleAppSelectorScreenState.Ready ?: return@launch
        val visibleApps = state.data.visibleApps
        val editedProfile = getEditedProfileUseCase.execute(Unit).first()
        val profileApps = visibleApps.map { it.toLauncherProfileApp() }

        val newProfile = editedProfile.toBuilder()
            .clearLauncherProfileApps()
            .addAllLauncherProfileApps(profileApps)
            .build()

        val result = updateLauncherProfileUseCase.execute(newProfile)
        _screenState.update {
            if (result.isSuccess) {
                VisibleAppSelectorScreenState.UpdateAppSelectionSuccess
            } else {
                VisibleAppSelectorScreenState.UpdateAppSelectionFailure
            }
        }
    }
}

fun MutableStateFlow<VisibleAppSelectorScreenState>.updateScreenData(
    modifier: ScreenData.() -> ScreenData
) {
    update { state ->
        if (state is VisibleAppSelectorScreenState.Ready) {
            VisibleAppSelectorScreenState.Ready(state.data.modifier())
        } else {
            state
        }
    }
}

sealed class VisibleAppSelectorScreenState {
    data object Loading : VisibleAppSelectorScreenState()
    data class Ready(val data: ScreenData) : VisibleAppSelectorScreenState()
    data object UpdateAppSelectionSuccess : VisibleAppSelectorScreenState()
    data object UpdateAppSelectionFailure : VisibleAppSelectorScreenState()
}

data class ScreenData(
    val appList: List<AppInfo>,
    val visibleApps: List<AppInfo>,
    val showConfirmButton: Boolean,
    val confirmButtonTextResource: Int,
    val showAppCounter: Boolean,
    val showEmptyAppSelectedError: Boolean,
    val showMaxAppSelectedError: Boolean,
    val maxItemCount: Int = LAUNCHER_MAX_APP_COUNT,
)
