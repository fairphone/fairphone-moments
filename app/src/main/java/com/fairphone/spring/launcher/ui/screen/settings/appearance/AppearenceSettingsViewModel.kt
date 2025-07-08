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
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.domain.usecase.profile.GetActiveProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.UpdateLauncherProfileUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppearenceSettingsViewModel(
    getActiveProfileUseCase: GetActiveProfileUseCase,
    private val updateLauncherProfileUseCase: UpdateLauncherProfileUseCase
) : ViewModel() {

    val editedProfile: StateFlow<LauncherProfile?> =
        getActiveProfileUseCase.execute(Unit)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun updateBlueLightFilter(filterValue: Boolean) {
        if (editedProfile.value != null) {
            viewModelScope.launch {
                updateLauncherProfileUseCase.execute(
                    editedProfile.value!!.toBuilder().setBlueLightFilterEnabled(filterValue)
                        .build()
                ).getOrNull()
            }
        }
    }

    fun updateGrayscaleIndicator(value: Boolean) {
        if (editedProfile.value != null) {
            viewModelScope.launch {
                updateLauncherProfileUseCase.execute(
                    editedProfile.value!!.toBuilder().setGrayScaleEnabled(value)
                        .build()
                ).getOrNull()
            }
        }
    }
}



