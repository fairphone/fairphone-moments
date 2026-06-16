/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Provides the device's current position on demand — when the user flips the
 * hardware switch — rather than monitoring geofences in the background. The
 * trigger matching itself lives in SwitchToTriggeredProfileUseCase.
 */
class LocationTriggerManager(
    private val context: Context,
) {
    fun hasLocationPermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** Metres between a fix and a latitude/longitude. */
    fun distanceMeters(from: Location, latitude: Double, longitude: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(from.latitude, from.longitude, latitude, longitude, results)
        return results[0]
    }

    /**
     * The most recent location the system already has cached, returned immediately so the
     * Moment can switch the instant the switch is flipped — no waiting on a fresh GPS fix.
     * Picks the newest fix across providers. Returns null when nothing is cached or the
     * permission is missing.
     */
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null
        val locationManager = context.getSystemService(LocationManager::class.java) ?: return null
        return listOf(
            LocationManager.FUSED_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
        )
            .filter { locationManager.allProviders.contains(it) }
            .mapNotNull { runCatching { locationManager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    /**
     * One-shot fix of the current position. Returns null when no fix could be
     * obtained or the permission is missing.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) return null
        val locationManager = context.getSystemService(LocationManager::class.java) ?: return null
        val provider = when {
            locationManager.allProviders.contains(LocationManager.FUSED_PROVIDER) ->
                LocationManager.FUSED_PROVIDER
            locationManager.allProviders.contains(LocationManager.GPS_PROVIDER) ->
                LocationManager.GPS_PROVIDER
            else -> return null
        }

        return suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()
            continuation.invokeOnCancellation { cancellationSignal.cancel() }
            locationManager.getCurrentLocation(
                provider,
                cancellationSignal,
                context.mainExecutor,
            ) { location ->
                continuation.resume(location)
            }
        }
    }

    private fun distanceTo(from: Location, latitude: Double, longitude: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(from.latitude, from.longitude, latitude, longitude, results)
        return results[0]
    }
}
