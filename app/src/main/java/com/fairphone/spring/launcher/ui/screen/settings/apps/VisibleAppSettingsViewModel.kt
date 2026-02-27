/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.apps

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.model.protos.LauncherProfileApp
import com.fairphone.spring.launcher.data.model.toLauncherProfileApp
import com.fairphone.spring.launcher.data.repository.AppInfoRepository
import com.fairphone.spring.launcher.domain.usecase.profile.GetEditedProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.UpdateLauncherProfileUseCase
import com.fairphone.spring.launcher.util.permute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VisibleAppSettingsViewModel(
    context: Context,
    private val appInfoRepository: AppInfoRepository,
    private val getEditedProfileUseCase: GetEditedProfileUseCase,
    private val updateLauncherProfileUseCase: UpdateLauncherProfileUseCase,
) : ViewModel() {

    lateinit var profile: LauncherProfile

    val screenState: StateFlow<VisibleAppSettingsScreenState> =
        getEditedProfileUseCase.execute(Unit).map { profile ->
            this.profile = profile
            val visibleApps = getAppInfoList(context, profile.launcherProfileAppsList)
            VisibleAppSettingsScreenState.Ready(
                visibleApps = visibleApps,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = VisibleAppSettingsScreenState.Loading,
        )

    private fun getAppInfoList(context: Context, appIds: List<LauncherProfileApp>): List<AppInfo> {
        return appInfoRepository.getAppInfosByProfileApps(context, appIds)
    }

    fun updateAppOrder(currentIndex: Int, targetIndex: Int) {
        viewModelScope.launch {
            if(currentIndex == targetIndex) {
                return@launch
            } else {
                screenState.value.let { state ->
                    if (state is VisibleAppSettingsScreenState.Ready) {
                        val updatedApps = state.visibleApps.permute(currentIndex, targetIndex)
                        val launcherProfileApps = updatedApps.map { it.toLauncherProfileApp() }
                        profile = profile
                            .toBuilder()
                            .clearLauncherProfileApps()
                            .addAllLauncherProfileApps(launcherProfileApps)
                            .build()
                        updateLauncherProfileUseCase.execute(profile)
                    }
                }
            }
        }
    }
}

sealed interface VisibleAppSettingsScreenState {
    object Loading : VisibleAppSettingsScreenState
    data class Ready(val visibleApps: List<AppInfo>) :
        VisibleAppSettingsScreenState
}
