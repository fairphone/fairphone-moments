/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.data.model.LAUNCHER_MAX_APP_COUNT
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.prefs.AppPrefs
import com.fairphone.spring.launcher.data.prefs.UsageMode
import com.fairphone.spring.launcher.data.repository.AppInfoRepository
import com.fairphone.spring.launcher.domain.usecase.profile.GetActiveProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.InitializeSpringLauncherUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetApplicationUsageModeUseCase
import com.fairphone.spring.launcher.util.isDeviceInRetailDemoMode
import com.fairphone.spring.launcher.util.launchApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class HomeScreenViewModel(
    context: Context,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    private val setApplicationUsageModeUseCase: SetApplicationUsageModeUseCase,
    private val appPrefs: AppPrefs,
    private val appInfoRepository: AppInfoRepository,
    private val initializeSpringLauncherUseCase: InitializeSpringLauncherUseCase,
) : ViewModel() {

    private val _dateTime: MutableStateFlow<LocalDateTime> =
        MutableStateFlow(getCurrentDateTime())
    val dateTime: StateFlow<LocalDateTime> = _dateTime.asStateFlow()

    val screenState: StateFlow<HomeScreenState?> =
        getActiveProfileUseCase.execute(Unit).map { profile ->
            val visibleApps = appInfoRepository
                .getAppInfosByProfileApps(context, profile.launcherProfileAppsList)
                .take(LAUNCHER_MAX_APP_COUNT) // We only show LAUNCHER_MAX_APP_COUNT apps

            HomeScreenState(
                activeProfile = profile,
                visibleApps = visibleApps,
                isRetailDemoMode = context.isDeviceInRetailDemoMode(),
                appUsageMode = appPrefs.usageMode()
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = null,
            )

    init {
        refreshTime()
        initializeSpringLauncher()
    }

    private fun refreshTime() = viewModelScope.launch {
        while (isActive) {
            _dateTime.update { getCurrentDateTime() }
            delay(1000)
        }
    }

    fun onAppClick(context: Context, appInfo: AppInfo) {
        viewModelScope.launch {
            context.launchApp(appInfo)
        }
    }

    fun initializeSpringLauncher() {
        viewModelScope.launch {
            initializeSpringLauncherUseCase.execute(Unit)
        }
    }

    /**
     * When user has clicked on ‘X’ on the onboarding tooltip, or interact with another component
     * on the screen (i.e., the Mode button or an app) the onboarding is complete and the usage
     * mode is the default one
     */
    fun finishOnBoarding() {
        viewModelScope.launch {
            if (screenState.value?.appUsageMode == UsageMode.ON_BOARDING_COMPLETE) {
                setApplicationUsageModeUseCase.execute(UsageMode.DEFAULT)
            }
        }
    }

    /**
     * @return the current [LocalDateTime]
     */
    private fun getCurrentDateTime(): LocalDateTime {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }
}

data class HomeScreenState(
    val activeProfile: LauncherProfile,
    val visibleApps: List<AppInfo> = emptyList(),
    val isRetailDemoMode: Boolean = false,
    val appUsageMode: UsageMode = UsageMode.ON_BOARDING
)
