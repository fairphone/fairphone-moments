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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.ui.screen.mode.creator.ChooseBackgroundScreen
import com.fairphone.spring.launcher.ui.screen.settings.appearance.AppearanceSettingsScreen
import com.fairphone.spring.launcher.ui.screen.settings.appearance.WallpaperSettingScreenState
import com.fairphone.spring.launcher.ui.screen.settings.appearance.WallpaperSettingsViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object WallpaperSettings

@Serializable
object AppearanceSettings

fun NavGraphBuilder.appearenceSettingsNavGraph(navController: NavHostController) {

    composable<AppearanceSettings> {
        val viewModel: WallpaperSettingsViewModel = koinViewModel()
        val activeProfile by viewModel.editedProfile.collectAsStateWithLifecycle()

        AppearanceSettingsScreen(
            onCustomizeWallpaperClick = {
                navController.navigate(WallpaperSettings)
            },
            mediaControlsEnabled = activeProfile?.mediaControlsDisabled != true,
            onMediaControlsToggle = viewModel::setMediaControlsEnabled,
            onApplyMediaControlsToAll = viewModel::applyMediaControlsToAll,
        )
    }

    composable<WallpaperSettings> {
        val viewModel: WallpaperSettingsViewModel = koinViewModel()
        val state by viewModel.updateWallpaperState.collectAsStateWithLifecycle()
        val activeProfile by viewModel.editedProfile.collectAsStateWithLifecycle()

        LaunchedEffect(state) {
            if (state is WallpaperSettingScreenState.Success) {
                navController.popBackStack()
            }
        }
        activeProfile?.let {
            ChooseBackgroundScreen(
                continueButtonName = stringResource(R.string.update_wallpaper),
                selectedColor = it.bgColor2,
                onBackgroundColorSelected = { colors ->
                    viewModel.updateProfileColors(colors)
                }
            )
        }
    }
}
