/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.time

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.data.model.protos.DayTrigger
import com.fairphone.spring.launcher.data.model.protos.TimeTrigger
import com.fairphone.spring.launcher.data.model.protos.copy
import com.fairphone.spring.launcher.domain.usecase.profile.GetEditedProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetDayTriggerParams
import com.fairphone.spring.launcher.domain.usecase.profile.SetDayTriggerUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetTimeTriggerParams
import com.fairphone.spring.launcher.domain.usecase.profile.SetTimeTriggerUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val DEFAULT_START_MINUTE = 9 * 60   // 09:00
const val DEFAULT_END_MINUTE = 17 * 60    // 17:00

class TimeTriggerSettingsViewModel(
    private val getEditedProfileUseCase: GetEditedProfileUseCase,
    private val setTimeTriggerUseCase: SetTimeTriggerUseCase,
    private val setDayTriggerUseCase: SetDayTriggerUseCase,
) : ViewModel() {

    val screenState: StateFlow<TimeTriggerSettingsScreenState> =
        getEditedProfileUseCase.execute(Unit)
            .map { profile ->
                TimeTriggerSettingsScreenState.Success(
                    dayTrigger = profile.dayTrigger,
                    timeTrigger = profile.timeTrigger,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = TimeTriggerSettingsScreenState.Loading,
            )

    // --- Day axis ---

    fun onDayEnabledChanged(enabled: Boolean) = viewModelScope.launch {
        updateDay(currentDay().copy { this.enabled = enabled })
    }

    fun onDayToggled(isoDay: Int) = viewModelScope.launch {
        val trigger = currentDay()
        val days = trigger.daysOfWeekList.toMutableList()
        if (!days.remove(isoDay)) days.add(isoDay)
        updateDay(
            trigger.copy {
                daysOfWeek.clear()
                daysOfWeek.addAll(days.sorted())
            }
        )
    }

    // --- Time axis ---

    fun onTimeEnabledChanged(enabled: Boolean) = viewModelScope.launch {
        val trigger = currentTime()
        // On first enable, seed a sensible default window if none was set.
        val seeded = if (enabled && trigger.startMinuteOfDay == 0 && trigger.endMinuteOfDay == 0) {
            trigger.copy {
                startMinuteOfDay = DEFAULT_START_MINUTE
                endMinuteOfDay = DEFAULT_END_MINUTE
            }
        } else {
            trigger
        }
        updateTime(seeded.copy { this.enabled = enabled })
    }

    fun onStartTimeChanged(minuteOfDay: Int) = viewModelScope.launch {
        updateTime(currentTime().copy { startMinuteOfDay = minuteOfDay })
    }

    fun onEndTimeChanged(minuteOfDay: Int) = viewModelScope.launch {
        updateTime(currentTime().copy { endMinuteOfDay = minuteOfDay })
    }

    // --- helpers ---

    private suspend fun currentDay(): DayTrigger =
        getEditedProfileUseCase.execute(Unit).first().dayTrigger

    private suspend fun currentTime(): TimeTrigger =
        getEditedProfileUseCase.execute(Unit).first().timeTrigger

    private suspend fun updateDay(trigger: DayTrigger) {
        val profile = getEditedProfileUseCase.execute(Unit).first()
        setDayTriggerUseCase.execute(
            SetDayTriggerParams(profileId = profile.id, dayTrigger = trigger)
        )
    }

    private suspend fun updateTime(trigger: TimeTrigger) {
        val profile = getEditedProfileUseCase.execute(Unit).first()
        setTimeTriggerUseCase.execute(
            SetTimeTriggerParams(profileId = profile.id, timeTrigger = trigger)
        )
    }
}

sealed interface TimeTriggerSettingsScreenState {
    object Loading : TimeTriggerSettingsScreenState
    data class Success(
        val dayTrigger: DayTrigger,
        val timeTrigger: TimeTrigger,
    ) : TimeTriggerSettingsScreenState
}
