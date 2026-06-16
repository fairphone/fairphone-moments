/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.motion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.data.model.protos.MotionMode
import com.fairphone.spring.launcher.data.model.protos.MotionTrigger
import com.fairphone.spring.launcher.data.model.protos.copy
import com.fairphone.spring.launcher.domain.usecase.profile.GetEditedProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetMotionTriggerParams
import com.fairphone.spring.launcher.domain.usecase.profile.SetMotionTriggerUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MotionTriggerSettingsViewModel(
    private val getEditedProfileUseCase: GetEditedProfileUseCase,
    private val setMotionTriggerUseCase: SetMotionTriggerUseCase,
) : ViewModel() {

    val screenState: StateFlow<MotionTriggerSettingsScreenState> =
        getEditedProfileUseCase.execute(Unit)
            .map { profile -> MotionTriggerSettingsScreenState.Success(profile.motionTrigger) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = MotionTriggerSettingsScreenState.Loading,
            )

    fun onTriggerEnabledChanged(enabled: Boolean) = viewModelScope.launch {
        update(current().copy { this.enabled = enabled })
    }

    fun onModeToggled(mode: MotionMode) = viewModelScope.launch {
        val trigger = current()
        val modes = trigger.modesList.toMutableList()
        if (!modes.remove(mode)) modes.add(mode)
        update(
            trigger.copy {
                this.modes.clear()
                this.modes.addAll(modes)
            }
        )
    }

    private suspend fun current(): MotionTrigger =
        getEditedProfileUseCase.execute(Unit).first().motionTrigger

    private suspend fun update(trigger: MotionTrigger) {
        val profile = getEditedProfileUseCase.execute(Unit).first()
        setMotionTriggerUseCase.execute(
            SetMotionTriggerParams(profileId = profile.id, motionTrigger = trigger)
        )
    }
}

sealed interface MotionTriggerSettingsScreenState {
    object Loading : MotionTriggerSettingsScreenState
    data class Success(val motionTrigger: MotionTrigger) : MotionTriggerSettingsScreenState
}
