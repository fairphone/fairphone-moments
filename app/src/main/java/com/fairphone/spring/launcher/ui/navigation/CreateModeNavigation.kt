/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fairphone.spring.launcher.data.model.Preset
import com.fairphone.spring.launcher.ui.screen.mode.creator.ChooseAppsScreen
import com.fairphone.spring.launcher.ui.screen.mode.creator.ChooseBackgroundScreen
import com.fairphone.spring.launcher.ui.screen.mode.creator.CreateModeState
import com.fairphone.spring.launcher.ui.screen.mode.creator.CreateModeViewModel
import com.fairphone.spring.launcher.ui.screen.mode.creator.NameYourMomentScreen
import com.fairphone.spring.launcher.ui.screen.mode.creator.SelectModeScreen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object SelectMode

@Serializable
object NameYourMoment

@Serializable
object ChooseApps

@Serializable
data object ChooseBackground

@Composable
fun CreateModeNavigation(
    navController: NavHostController,
    onModeCreated: () -> Unit,
) {
    val viewModel: CreateModeViewModel = koinViewModel()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = SelectMode
    ) {
        // Select Mode Screen
        composable<SelectMode> {
            SelectModeScreen(
                profiles = Preset.entries,
                onPresetSelected = { preset ->
                    viewModel.onPresetSelected(context, preset)

                    if (preset == Preset.Custom) {
                        navController.navigate(NameYourMoment)
                    } else {
                        navController.navigate(ChooseApps)
                    }
                }
            )
        }

        // Name Your Moment Screen
        composable<NameYourMoment> { backStackEntry ->
            NameYourMomentScreen(
                modeName = viewModel.profileName,
                modeIcon = viewModel.profileIcon,
                onContinue = { newName, newIcon ->
                    viewModel.updateName(newName)
                    viewModel.updateIcon(newIcon)

                    navController.navigate(ChooseApps)
                }
            )
        }

        // Choose Apps Screen
        composable<ChooseApps> { backStackEntry ->
            val screenState by viewModel.appSelectorScreenState.collectAsStateWithLifecycle()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                viewModel.loadApps(context)
            }

            ChooseAppsScreen(
                screenState = screenState,
                onAppClick = viewModel::onAppClick,
                onAppDeselected = viewModel::removeVisibleApp,
                onContinue = {
                    viewModel.updateLauncherVisibleApps(viewModel.visibleApps)
                    navController.navigate(ChooseBackground)
                }
            )
        }

        // Choose Background Screen
        composable<ChooseBackground> { backStackEntry ->
            val createModeState by viewModel.createModeState.collectAsStateWithLifecycle()

            ChooseBackgroundScreen(
                selectedColor = viewModel.selectedPreset.colors.rightColor,
                onBackgroundColorSelected = { colors ->
                    viewModel.updateBackgroundColors(colors)
                    viewModel.save()
                }
            )

            LaunchedEffect(createModeState) {
                if (createModeState is CreateModeState.Success) {
                    onModeCreated()
                }
            }
        }
    }
}
