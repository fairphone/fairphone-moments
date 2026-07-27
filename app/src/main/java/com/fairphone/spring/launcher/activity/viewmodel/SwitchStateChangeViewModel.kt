/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.activity.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.analytics.AnalyticsEvent
import com.fairphone.spring.launcher.analytics.AnalyticsService
import com.fairphone.spring.launcher.data.model.SwitchState
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.domain.usecase.ToggleDndUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.GetActiveProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.InitializeSpringLauncherUseCase
import com.fairphone.spring.launcher.util.isDoNotDisturbAccessGranted
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SwitchStateChangeViewModel(
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val toggleDndUseCase: ToggleDndUseCase,
    private val initializeSpringLauncherUseCase: InitializeSpringLauncherUseCase,
    private val analyticsService: AnalyticsService,
) : ViewModel() {

    val activeProfile: StateFlow<LauncherProfile?> =
        getActiveProfileUseCase.execute(Unit)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    init {
        initializeSpringLauncher()
    }

    fun initializeSpringLauncher() {
        viewModelScope.launch {
            initializeSpringLauncherUseCase.execute(Unit)
        }
    }

    fun handleDnd(context: Context, switchState: SwitchState) = viewModelScope.launch {
        trackSwitchStateChangedEvent(switchState)

        val enableDnd = when (switchState) {
            SwitchState.ENABLED -> true
            SwitchState.DISABLED -> false
        }
        toggleDndUseCase.execute(enableDnd)
    }

    fun handleLockscreenWallpaper(context: Context, switchState: SwitchState) {
        val enableDnd = when (switchState) {
            SwitchState.ENABLED -> true
            SwitchState.DISABLED -> false
        }
        // TODO: Enable lockscreen wallpaper switch when fixed
        // LockscreenWallpaperSwitcherWorker.enqueueWallpaperWork(context, enableDnd)
    }

    fun isDndPermissionGranted(context: Context): Boolean {
        return context.isDoNotDisturbAccessGranted()
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun trackSwitchStateChangedEvent(switchState: SwitchState) {
        val profile = getActiveProfileUseCase.execute(Unit).first()
        val event = when (switchState) {
            SwitchState.ENABLED -> {
                AnalyticsEvent.SwitchOnEvent(
                    modeName = profile.name,
                    modeId = profile.id,
                    visibleApps = profile.launcherProfileAppsList.map { it.packageName },
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )
            }

            SwitchState.DISABLED -> {
                AnalyticsEvent.SwitchOffEvent(
                    modeName = profile.name,
                    modeId = profile.id,
                    visibleApps = profile.launcherProfileAppsList.map { it.packageName },
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )
            }
        }

        analyticsService.trackEvent(event)
    }
}
