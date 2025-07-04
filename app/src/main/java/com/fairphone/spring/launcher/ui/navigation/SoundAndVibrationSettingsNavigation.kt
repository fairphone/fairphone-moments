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
import androidx.navigation.compose.composable
import com.fairphone.spring.launcher.ui.screen.settings.sound.SoundVibrationSettingsScreen
import com.fairphone.spring.launcher.ui.screen.settings.sound.SoundVibrationSettingsViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object SoundAndVibrationSettings

fun NavGraphBuilder.soundAndVibrationSettingsNavGraph() {

    composable<SoundAndVibrationSettings> {
        val viewModel: SoundVibrationSettingsViewModel = koinViewModel()
        val screenState by viewModel.screenState.collectAsStateWithLifecycle()

        SoundVibrationSettingsScreen(
            screenState = screenState,
            onSoundSettingsSelected = {
                viewModel.onSoundSettingsSelected(it)
            }
        )
    }

}
