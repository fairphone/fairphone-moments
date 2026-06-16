/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.navigation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.fairphone.spring.launcher.ui.screen.settings.location.LocationTriggerSettingsScreen
import com.fairphone.spring.launcher.ui.screen.settings.location.LocationTriggerSettingsViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object LocationTriggerSettings

fun NavGraphBuilder.locationTriggerSettingsNavGraph(navController: NavHostController) {
    composable<LocationTriggerSettings> {
        val viewModel: LocationTriggerSettingsViewModel = koinViewModel()
        val screenState by viewModel.screenState.collectAsStateWithLifecycle()

        val locationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { viewModel.refreshPermissionState() }
        )

        LocationTriggerSettingsScreen(
            screenState = screenState,
            onTriggerEnabledChanged = viewModel::onTriggerEnabledChanged,
            onAddCurrentLocation = viewModel::onAddCurrentLocation,
            onRemoveLocation = viewModel::onRemoveLocation,
            onRadiusSelected = viewModel::onRadiusSelected,
            onRequestLocationPermission = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            },
        )
    }
}
