/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fairphone.spring.launcher.ui.component.ScreenViewTracker
import com.fairphone.spring.launcher.ui.screen.settings.main.ProfileSettingsScreen
import com.fairphone.spring.launcher.ui.screen.settings.main.ProfileSettingsViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

const val SLIDE_ANIMATION_DURATION_MILLIS = 300

@Serializable
object ProfileSettings


@Composable
fun SettingsNavigation(
    navController: NavHostController,
    onCloseSettings: () -> Unit
) {
    ScreenViewTracker(navController = navController)

    NavHost(
        navController = navController,
        startDestination = ProfileSettings,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(SLIDE_ANIMATION_DURATION_MILLIS)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(SLIDE_ANIMATION_DURATION_MILLIS)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(SLIDE_ANIMATION_DURATION_MILLIS)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(SLIDE_ANIMATION_DURATION_MILLIS)
            )
        }
    ) {
        // Moment Settings Screen
        composable<ProfileSettings> {
            val viewModel: ProfileSettingsViewModel = koinViewModel()
            val screenState by viewModel.screenState.collectAsStateWithLifecycle()

            ProfileSettingsScreen(
                screenState = screenState,
                onEditProfileName = viewModel::updateProfileName,
                onProfileIconClick = viewModel::updateProfileIcon,
                onSwitchToThisProfileClick = viewModel::setEditedProfileAsActive,
                onNavigateToVisibleAppSettings = {
                    navController.navigate(VisibleAppSettings)
                },
                onNavigateToAllowedContactSettings = {
                    navController.navigate(AllowedContactSettings)
                },
                onNavigateToAllowedAppSettings = {
                    navController.navigate(AllowedAppSettings)
                },
                onNavigateToNotificationSettings = {
                    navController.navigate(NotificationSettings)
                },
                onNavigateToAppearanceSettings = {
                    navController.navigate(AppearanceSettings)
                },
                onNavigateToLocationTriggerSettings = {
                    navController.navigate(LocationTriggerSettings)
                },
                onNavigateToTimeTriggerSettings = {
                    navController.navigate(TimeTriggerSettings)
                },
                onNavigateToMotionTriggerSettings = {
                    navController.navigate(MotionTriggerSettings)
                },
                onNavigateToSoundAndVibrationSettings = {},
                onNavigateToPowerSavingSettings = {},
                onModeDeletionClick = {
                    viewModel.deleteProfile()
                    onCloseSettings()
                }
            )
        }

        // Visible App Settings
        visibleAppSettingsNavGraph(navController)

        // Allowed Contact Settings
        allowedContactSettingsNavGraph(navController)

        // Allowed App Settings
        allowedAppSettingsNavGraph(navController)

        // Allowed notification Settings
        notificationSettingsNavGraph(navController)

        // Appearance Settings
        appearenceSettingsNavGraph(navController)

        // Location Trigger Settings
        locationTriggerSettingsNavGraph(navController)

        // Time & Day Trigger Settings
        timeTriggerSettingsNavGraph(navController)

        // Motion (transport-mode) Trigger Settings
        motionTriggerSettingsNavGraph(navController)
    }
}
