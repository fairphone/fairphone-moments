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
import com.fairphone.spring.launcher.data.model.SwitchState
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.domain.usecase.ToggleDndUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.GetActiveProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.InitializeSpringLauncherUseCase
import com.fairphone.spring.launcher.util.isDoNotDisturbAccessGranted
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SwitchStateChangeViewModel(
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val toggleDndUseCase: ToggleDndUseCase,
    private val initializeSpringLauncherUseCase: InitializeSpringLauncherUseCase,
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
}
