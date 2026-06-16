/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairphone.spring.launcher.data.model.protos.LocationPoint
import com.fairphone.spring.launcher.data.model.protos.LocationTrigger
import com.fairphone.spring.launcher.domain.usecase.profile.GetEditedProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetLocationTriggerParams
import com.fairphone.spring.launcher.domain.usecase.profile.SetLocationTriggerUseCase
import com.fairphone.spring.launcher.util.DEFAULT_TRIGGER_RADIUS_METERS
import com.fairphone.spring.launcher.util.LocationTriggerManager
import com.fairphone.spring.launcher.util.effectivePoints
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocationTriggerSettingsViewModel(
    private val getEditedProfileUseCase: GetEditedProfileUseCase,
    private val setLocationTriggerUseCase: SetLocationTriggerUseCase,
    private val locationTriggerManager: LocationTriggerManager,
) : ViewModel() {

    private val hasLocationPermission = MutableStateFlow(locationTriggerManager.hasLocationPermission())
    private val isFetchingLocation = MutableStateFlow(false)
    private val fetchLocationFailed = MutableStateFlow(false)

    val screenState: StateFlow<LocationTriggerSettingsScreenState> =
        combine(
            getEditedProfileUseCase.execute(Unit),
            hasLocationPermission,
            isFetchingLocation,
            fetchLocationFailed,
        ) { profile, locationGranted, fetching, fetchFailed ->
            LocationTriggerSettingsScreenState.Success(
                enabled = profile.locationTrigger.enabled,
                locations = profile.locationTrigger.effectivePoints(),
                hasLocationPermission = locationGranted,
                isFetchingLocation = fetching,
                fetchLocationFailed = fetchFailed,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = LocationTriggerSettingsScreenState.Loading
        )

    /**
     * Called when returning from a permission request, since permission changes
     * do not emit through any flow.
     */
    fun refreshPermissionState() {
        hasLocationPermission.value = locationTriggerManager.hasLocationPermission()
    }

    fun onTriggerEnabledChanged(enabled: Boolean) = viewModelScope.launch {
        val trigger = currentTrigger()
        if (enabled && trigger.effectivePoints().isEmpty()) {
            // First activation with no place yet: capture the current position.
            fetchAndAddCurrentLocation(enable = true)
        } else {
            save(trigger, trigger.effectivePoints(), enabled = enabled)
        }
    }

    fun onAddCurrentLocation() = viewModelScope.launch {
        fetchAndAddCurrentLocation(enable = null)
    }

    fun onRemoveLocation(index: Int) = viewModelScope.launch {
        val trigger = currentTrigger()
        val points = trigger.effectivePoints().toMutableList()
        if (index in points.indices) {
            points.removeAt(index)
            save(trigger, points, enabled = null)
        }
    }

    fun onRadiusSelected(index: Int, radiusMeters: Float) = viewModelScope.launch {
        val trigger = currentTrigger()
        val points = trigger.effectivePoints().toMutableList()
        if (index in points.indices) {
            points[index] = points[index].toBuilder().setRadiusMeters(radiusMeters).build()
            save(trigger, points, enabled = null)
        }
    }

    private suspend fun fetchAndAddCurrentLocation(enable: Boolean?) {
        fetchLocationFailed.value = false
        isFetchingLocation.value = true
        try {
            val location = locationTriggerManager.getCurrentLocation()
            if (location == null) {
                fetchLocationFailed.value = true
                return
            }
            val trigger = currentTrigger()
            val points = trigger.effectivePoints().toMutableList()
            points.add(
                LocationPoint.newBuilder()
                    .setLatitude(location.latitude)
                    .setLongitude(location.longitude)
                    .setRadiusMeters(DEFAULT_TRIGGER_RADIUS_METERS)
                    .build()
            )
            save(trigger, points, enabled = enable)
        } finally {
            isFetchingLocation.value = false
        }
    }

    private suspend fun currentTrigger(): LocationTrigger =
        getEditedProfileUseCase.execute(Unit).first().locationTrigger

    /** Writes the points into `locations` and drops the legacy single-location fields. */
    private suspend fun save(trigger: LocationTrigger, points: List<LocationPoint>, enabled: Boolean?) {
        val profile = getEditedProfileUseCase.execute(Unit).first()
        val builder = trigger.toBuilder()
            .clearLatitude()
            .clearLongitude()
            .clearRadiusMeters()
            .clearLocations()
            .addAllLocations(points)
        enabled?.let { builder.enabled = it }
        setLocationTriggerUseCase.execute(
            SetLocationTriggerParams(
                profileId = profile.id,
                locationTrigger = builder.build(),
            )
        )
    }
}

sealed interface LocationTriggerSettingsScreenState {
    object Loading : LocationTriggerSettingsScreenState
    data class Success(
        val enabled: Boolean,
        val locations: List<LocationPoint>,
        val hasLocationPermission: Boolean,
        val isFetchingLocation: Boolean,
        val fetchLocationFailed: Boolean,
    ) : LocationTriggerSettingsScreenState
}
