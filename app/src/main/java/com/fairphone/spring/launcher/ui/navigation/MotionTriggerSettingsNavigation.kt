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
import com.fairphone.spring.launcher.ui.screen.settings.motion.MotionTriggerSettingsScreen
import com.fairphone.spring.launcher.ui.screen.settings.motion.MotionTriggerSettingsViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object MotionTriggerSettings

fun NavGraphBuilder.motionTriggerSettingsNavGraph(navController: NavHostController) {
    composable<MotionTriggerSettings> {
        val viewModel: MotionTriggerSettingsViewModel = koinViewModel()
        val screenState by viewModel.screenState.collectAsStateWithLifecycle()

        MotionTriggerSettingsScreen(
            screenState = screenState,
            onTriggerEnabledChanged = viewModel::onTriggerEnabledChanged,
            onModeToggled = viewModel::onModeToggled,
        )
    }
}
