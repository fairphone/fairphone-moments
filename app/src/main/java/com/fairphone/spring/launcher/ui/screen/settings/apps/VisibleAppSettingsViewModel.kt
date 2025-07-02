/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.apps

/*
 * Copyright (c) 2025. FairPhone B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VisibleAppSettingsViewModel(
    context: Context,
    private val appInfoRepository: AppInfoRepository,
    private val getEditedProfileUseCase: GetEditedProfileUseCase,
    private val updateLauncherProfileUseCase: UpdateLauncherProfileUseCase,
) : ViewModel() {

    private val _screenState: MutableStateFlow<VisibleAppSettingsScreenState> =
        MutableStateFlow(VisibleAppSettingsScreenState.Loading)
    val screenState = _screenState.asStateFlow()
    lateinit var profile: LauncherProfile

    init {
        viewModelScope.launch {
            _screenState.update {
                profile = getEditedProfileUseCase.execute(Unit).first()
                val visibleApps = getAppInfoList(context, profile.launcherProfileAppsList)
                VisibleAppSettingsScreenState.Ready(
                    visibleApps = visibleApps,
                )
            }
        }
    }

    private fun getAppInfoList(context: Context, appIds: List<LauncherProfileApp>): List<AppInfo> {
        return appInfoRepository.getAppInfosByProfileApps(context, appIds)
    }

    fun updateAppOrder(currentIndex: Int, targetIndex: Int) {
        viewModelScope.launch {
            if(currentIndex == targetIndex) {
                return@launch
            } else {
                _screenState.update { state ->
                    when (state) {
                        is VisibleAppSettingsScreenState.Loading -> state
                        is VisibleAppSettingsScreenState.Ready -> {
                            val updatedApps = state.visibleApps.permute(currentIndex, targetIndex)
                            val launcherProfileApps = updatedApps.map { it.toLauncherProfileApp() }
                            profile = profile
                                .toBuilder()
                                .clearLauncherProfileApps()
                                .addAllLauncherProfileApps(launcherProfileApps)
                                .build()
                            val  result = updateLauncherProfileUseCase.execute(profile)
                            if(result.isSuccess) {
                                state.copy(visibleApps = updatedApps)
                            } else {
                                state
                            }
                        }
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
