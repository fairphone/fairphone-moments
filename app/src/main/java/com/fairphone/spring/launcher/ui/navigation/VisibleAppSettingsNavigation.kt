/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.fairphone.spring.launcher.ui.screen.settings.apps.VisibleAppSettingsScreen
import com.fairphone.spring.launcher.ui.screen.settings.apps.VisibleAppSettingsViewModel
import com.fairphone.spring.launcher.ui.screen.settings.apps.selector.VisibleAppSelectorScreen
import com.fairphone.spring.launcher.ui.screen.settings.apps.selector.VisibleAppSelectorScreenState
import com.fairphone.spring.launcher.ui.screen.settings.apps.selector.VisibleAppSelectorViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object VisibleAppSettings

@Serializable
object VisibleAppSelector


fun NavGraphBuilder.visibleAppSettingsNavGraph(navController: NavHostController) {
    // Visible App Settings Screen
    composable<VisibleAppSettings> {
        val viewModel: VisibleAppSettingsViewModel = koinViewModel()
        val screenState by viewModel.screenState.collectAsStateWithLifecycle()

        VisibleAppSettingsScreen(
            screenState = screenState,
            onChangeAppsClick = {
                navController.navigate(VisibleAppSelector)
            },
            onChangeAppOrder = { currentIndex, targetIndex ->
                viewModel.updateAppOrder(currentIndex, targetIndex)
            }
        )
    }

    // Visible App Selector Screen
    composable<VisibleAppSelector> {
        val viewModel: VisibleAppSelectorViewModel = koinViewModel()
        val screenState by viewModel.screenState.collectAsStateWithLifecycle()

        LaunchedEffect(screenState) {
            if (screenState is VisibleAppSelectorScreenState.UpdateAppSelectionSuccess) {
                navController.popBackStack()
            }
        }

        VisibleAppSelectorScreen(
            screenState = screenState,
            onAppClick = viewModel::onAppClick,
            onAppDeselected = viewModel::removeVisibleApp,
            onConfirmAppSelection = {
                viewModel.confirmAppSelection()
            }
        )
    }
}
