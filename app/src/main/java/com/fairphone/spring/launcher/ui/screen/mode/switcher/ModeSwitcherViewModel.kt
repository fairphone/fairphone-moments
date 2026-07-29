/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.mode.switcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.domain.usecase.profile.GetActiveProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.GetAllProfilesUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetActiveProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetEditedProfileUseCase
import com.fairphone.spring.launcher.util.Constants
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

class ModeSwitcherViewModel(
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val getAllProfilesUseCase: GetAllProfilesUseCase,
    private val setActiveProfileUseCase: SetActiveProfileUseCase,
    private val setEditedProfileUseCase: SetEditedProfileUseCase,
) : ViewModel() {

    val screenState: StateFlow<ModeSwitcherScreenState?> =
        getAllProfilesUseCase.execute(Unit)
            .zip(getActiveProfileUseCase.execute(Unit)) { profiles, active ->
                ModeSwitcherScreenState(
                    activeProfile = active,
                    profiles = profiles,
                    isMaxProfileCountReached = profiles.size >= Constants.MAX_PROFILE_COUNT,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = null,
            )

    /**
     * TODO: Add javadoc
     */
    suspend fun updateActiveProfile(profile: LauncherProfile) {
        setActiveProfileUseCase.execute(profile.id)
    }

    /**
     * TODO: Add javadoc
     */
    fun editActiveProfileSettings(profile: LauncherProfile) = viewModelScope.launch {
        setEditedProfileUseCase.execute(profile.id)
    }
}

data class ModeSwitcherScreenState(
    val activeProfile: LauncherProfile,
    val profiles: List<LauncherProfile>,
    val isMaxProfileCountReached: Boolean,
)
