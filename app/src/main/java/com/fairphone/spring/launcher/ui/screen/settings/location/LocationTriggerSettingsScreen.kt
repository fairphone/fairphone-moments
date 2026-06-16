/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.location

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.protos.LocationPoint
import com.fairphone.spring.launcher.ui.FP6Preview
import com.fairphone.spring.launcher.ui.FP6PreviewDark
import com.fairphone.spring.launcher.ui.component.SettingListItem
import com.fairphone.spring.launcher.ui.component.SettingSwitchItem
import com.fairphone.spring.launcher.ui.theme.Color_FP_Brand_Lime
import com.fairphone.spring.launcher.ui.theme.FairphoneTypography
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import com.fairphone.spring.launcher.ui.theme.errorColor
import java.util.Locale

val TRIGGER_RADIUS_OPTIONS_METERS = listOf(100f, 250f, 500f, 1000f)

@Composable
fun LocationTriggerSettingsScreen(
    screenState: LocationTriggerSettingsScreenState,
    onTriggerEnabledChanged: (Boolean) -> Unit,
    onAddCurrentLocation: () -> Unit,
    onRemoveLocation: (Int) -> Unit,
    onRadiusSelected: (Int, Float) -> Unit,
    onRequestLocationPermission: () -> Unit,
) {
    when (screenState) {
        is LocationTriggerSettingsScreenState.Loading ->
            Box(modifier = Modifier.fillMaxSize())

        is LocationTriggerSettingsScreenState.Success ->
            LocationTriggerSettingsScreen(
                enabled = screenState.enabled,
                locations = screenState.locations,
                hasLocationPermission = screenState.hasLocationPermission,
                isFetchingLocation = screenState.isFetchingLocation,
                fetchLocationFailed = screenState.fetchLocationFailed,
                onTriggerEnabledChanged = onTriggerEnabledChanged,
                onAddCurrentLocation = onAddCurrentLocation,
                onRemoveLocation = onRemoveLocation,
                onRadiusSelected = onRadiusSelected,
                onRequestLocationPermission = onRequestLocationPermission,
            )
    }
}

@Composable
fun LocationTriggerSettingsScreen(
    enabled: Boolean,
    locations: List<LocationPoint>,
    hasLocationPermission: Boolean,
    isFetchingLocation: Boolean,
    fetchLocationFailed: Boolean,
    onTriggerEnabledChanged: (Boolean) -> Unit,
    onAddCurrentLocation: () -> Unit,
    onRemoveLocation: (Int) -> Unit,
    onRadiusSelected: (Int, Float) -> Unit,
    onRequestLocationPermission: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.location_trigger_description),
            style = FairphoneTypography.BodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp)
        )

        if (!hasLocationPermission) {
            Text(
                text = stringResource(R.string.location_trigger_permission_header),
                style = FairphoneTypography.H4,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Text(
                text = stringResource(R.string.location_trigger_permission_text),
                style = FairphoneTypography.BodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PermissionListItem(
                title = stringResource(R.string.permission_toggle_title_location),
                subtitle = stringResource(R.string.permission_toggle_subtitle_location),
                onClick = onRequestLocationPermission,
            )
            return@Column
        }

        SettingSwitchItem(
            state = enabled,
            title = stringResource(R.string.location_trigger_enable_title),
            subtitle = if (isFetchingLocation) {
                stringResource(R.string.location_trigger_fetching)
            } else {
                stringResource(R.string.location_trigger_enable_subtitle)
            },
            onClick = onTriggerEnabledChanged,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        )

        if (locations.isEmpty()) {
            Text(
                text = stringResource(R.string.location_trigger_no_location),
                style = FairphoneTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        locations.forEachIndexed { index, point ->
            LocationCard(
                index = index,
                point = point,
                onRemove = { onRemoveLocation(index) },
                onRadiusSelected = { radius -> onRadiusSelected(index, radius) },
            )
        }

        SettingListItem(
            title = stringResource(R.string.location_trigger_add),
            subtitle = if (isFetchingLocation) {
                stringResource(R.string.location_trigger_fetching)
            } else {
                stringResource(R.string.location_trigger_current_location)
            },
            onClick = onAddCurrentLocation,
            icon = {
                Icon(
                    imageVector = if (locations.isEmpty()) Icons.Outlined.MyLocation else Icons.Outlined.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        )

        if (fetchLocationFailed) {
            Text(
                text = stringResource(R.string.location_trigger_fetch_failed),
                style = FairphoneTypography.BodySmall,
                textAlign = TextAlign.Center,
                color = errorColor,
            )
        }
    }
}

@Composable
private fun LocationCard(
    index: Int,
    point: LocationPoint,
    onRemove: () -> Unit,
    onRadiusSelected: (Float) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.location_trigger_location_label, index + 1),
                    style = FairphoneTypography.ButtonDefault,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formatCoords(point),
                    style = FairphoneTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.location_trigger_remove),
                    tint = errorColor,
                )
            }
        }

        Text(
            text = stringResource(R.string.location_trigger_radius_header),
            style = FairphoneTypography.BodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TRIGGER_RADIUS_OPTIONS_METERS.forEach { radius ->
                RadiusChip(
                    label = formatDistance(radius),
                    selected = point.radiusMeters == radius,
                    onClick = { onRadiusSelected(radius) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RadiusChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color_FP_Brand_Lime else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) Color_FP_Brand_Lime else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
    ) {
        Text(
            text = label,
            style = FairphoneTypography.BodySmall,
            fontWeight = FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.scrim else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun PermissionListItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    SettingListItem(
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
    )
}

private fun formatCoords(point: LocationPoint): String =
    String.format(Locale.getDefault(), "%.5f, %.5f", point.latitude, point.longitude)

private fun formatDistance(meters: Float): String =
    if (meters >= 1000f) {
        String.format(Locale.getDefault(), "%.0f km", meters / 1000f)
    } else {
        String.format(Locale.getDefault(), "%.0f m", meters)
    }

@Composable
private fun LocationTriggerSettingsScreen_Preview() {
    SpringLauncherTheme {
        LocationTriggerSettingsScreen(
            screenState = LocationTriggerSettingsScreenState.Success(
                enabled = true,
                locations = listOf(
                    LocationPoint.newBuilder()
                        .setLatitude(52.36757).setLongitude(4.90400).setRadiusMeters(250f).build(),
                    LocationPoint.newBuilder()
                        .setLatitude(51.92250).setLongitude(4.47917).setRadiusMeters(1000f).build(),
                ),
                hasLocationPermission = true,
                isFetchingLocation = false,
                fetchLocationFailed = false,
            ),
            onTriggerEnabledChanged = {},
            onAddCurrentLocation = {},
            onRemoveLocation = {},
            onRadiusSelected = { _, _ -> },
            onRequestLocationPermission = {},
        )
    }
}

@Composable
@FP6Preview()
private fun LocationTriggerSettingsScreen_LightPreview() {
    LocationTriggerSettingsScreen_Preview()
}

@Composable
@FP6PreviewDark()
private fun LocationTriggerSettingsScreen_DarkPreview() {
    LocationTriggerSettingsScreen_Preview()
}
