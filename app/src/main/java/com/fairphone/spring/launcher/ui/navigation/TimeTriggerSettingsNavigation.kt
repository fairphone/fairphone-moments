/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.fairphone.spring.launcher.ui.screen.settings.time.TimeTriggerSettingsScreen
import com.fairphone.spring.launcher.ui.screen.settings.time.TimeTriggerSettingsViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object TimeTriggerSettings

fun NavGraphBuilder.timeTriggerSettingsNavGraph(navController: NavHostController) {
    composable<TimeTriggerSettings> {
        val viewModel: TimeTriggerSettingsViewModel = koinViewModel()
        val screenState by viewModel.screenState.collectAsStateWithLifecycle()

        TimeTriggerSettingsScreen(
            screenState = screenState,
            onDayEnabledChanged = viewModel::onDayEnabledChanged,
            onDayToggled = viewModel::onDayToggled,
            onTimeEnabledChanged = viewModel::onTimeEnabledChanged,
            onStartTimeChanged = viewModel::onStartTimeChanged,
            onEndTimeChanged = viewModel::onEndTimeChanged,
        )
    }
}
