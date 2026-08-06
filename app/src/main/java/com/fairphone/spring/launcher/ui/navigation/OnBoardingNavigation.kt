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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.LauncherColors
import com.fairphone.spring.launcher.ui.icons.mode.ModeIcon
import com.fairphone.spring.launcher.ui.screen.mode.creator.ChooseAppsScreen
import com.fairphone.spring.launcher.ui.screen.mode.creator.ChooseBackgroundScreen
import com.fairphone.spring.launcher.ui.screen.mode.creator.NameYourMomentScreen
import com.fairphone.spring.launcher.ui.screen.onboarding.OnBoardingViewModel
import com.fairphone.spring.launcher.ui.screen.onboarding.UpdateModeState
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

/**
 * Use to initialize the onboarding.
 */
@Serializable
object OnBoardingInit

/**
 * Used when the user wants to customize the defaults apps
 */
@Serializable
data object OnBoardingChooseApps

/**
 * Used when the user wants to customize his/her background
 */
@Serializable
data object OnBoardingChooseBackground

@Composable
fun OnBoardingNavigation(
    navController: NavHostController,
    onBoardingClose: () -> Unit,
) {
    val viewModel: OnBoardingViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = OnBoardingInit
    ) {
        composable<OnBoardingInit> {
            val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

            if (activeProfile != null) {
                NameYourMomentScreen(
                    modeName = activeProfile!!.name,
                    modeIcon = ModeIcon.fromString(activeProfile!!.icon),
                    onContinue = { newName, icon ->
                        viewModel.updateName(newName)
                        viewModel.updateIcon(icon)

                        navController.navigate(OnBoardingChooseApps)
                    }
                )
            }
        }

        composable<OnBoardingChooseApps> { backStackEntry ->
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
                    viewModel.updateLauncherProfileApps(viewModel.visibleApps)
                    navController.navigate(OnBoardingChooseBackground)
                }
            )
        }

        composable<OnBoardingChooseBackground> { backStackEntry ->
            val updateProfileState by viewModel.updateModeState.collectAsStateWithLifecycle()

            ChooseBackgroundScreen(
                selectedColor = LauncherColors.Default.rightColor,
                continueButtonName = stringResource(R.string.bt_apply_changes),
                onBackgroundColorSelected = { colors ->
                    viewModel.updateBackgroundColors(colors)
                    viewModel.updateProfile()
                }
            )

            LaunchedEffect(updateProfileState) {
                if (updateProfileState is UpdateModeState.Success) {
                    onBoardingClose()
                }
            }
        }
    }
}
