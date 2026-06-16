/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.util

import com.fairphone.spring.launcher.data.model.protos.LocationPoint
import com.fairphone.spring.launcher.data.model.protos.LocationTrigger

const val DEFAULT_TRIGGER_RADIUS_METERS = 250f

/**
 * The places this trigger covers. Reads the [LocationTrigger.getLocationsList], falling back to
 * the legacy single lat/lng/radius fields so profiles saved before multi-location keep working.
 */
fun LocationTrigger.effectivePoints(): List<LocationPoint> = when {
    locationsList.isNotEmpty() -> locationsList
    latitude != 0.0 || longitude != 0.0 -> listOf(
        LocationPoint.newBuilder()
            .setLatitude(latitude)
            .setLongitude(longitude)
            .setRadiusMeters(if (radiusMeters > 0f) radiusMeters else DEFAULT_TRIGGER_RADIUS_METERS)
            .build()
    )
    else -> emptyList()
}
