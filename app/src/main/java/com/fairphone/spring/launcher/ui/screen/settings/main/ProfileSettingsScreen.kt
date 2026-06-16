/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.main

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.data.model.Mock_Profile
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.ui.FP6Preview
import com.fairphone.spring.launcher.ui.FP6PreviewDark
import com.fairphone.spring.launcher.ui.component.ConfirmDialog
import com.fairphone.spring.launcher.ui.component.LauncherProfileSettingsTopBar
import com.fairphone.spring.launcher.ui.component.ProfileNameEditorDialog
import com.fairphone.spring.launcher.ui.component.SettingListItem
import com.fairphone.spring.launcher.ui.screen.settings.contacts.allowedContactSubtitle
import com.fairphone.spring.launcher.ui.theme.FairphoneTypography
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import com.fairphone.spring.launcher.ui.theme.errorColor

@Composable
fun ProfileSettingsScreen(
    screenState: ProfileSettingsScreenState,
    onEditProfileName: (String) -> Unit,
    onProfileIconClick: () -> Unit,
    onSwitchToThisProfileClick: () -> Unit,
    onNavigateToVisibleAppSettings: () -> Unit,
    onNavigateToAllowedContactSettings: () -> Unit,
    onNavigateToAllowedAppSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToAppearanceSettings: () -> Unit,
    onNavigateToLocationTriggerSettings: () -> Unit,
    onNavigateToTimeTriggerSettings: () -> Unit,
    onNavigateToMotionTriggerSettings: () -> Unit,
    onNavigateToSoundAndVibrationSettings: () -> Unit,
    onNavigateToPowerSavingSettings: () -> Unit,
    onModeDeletionClick: () -> Unit
) {
    when (screenState) {
        is ProfileSettingsScreenState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize()
            )
        }

        is ProfileSettingsScreenState.Success -> {
            ProfileSettingsScreen(
                profile = screenState.profile,
                visibleApps = screenState.visibleApps,
                isActiveProfile = screenState.isActiveProfile,
                hasMultipleProfiles = screenState.hasMultipleProfiles,
                onEditProfileName = onEditProfileName,
                onProfileIconClick = onProfileIconClick,
                onSwitchToThisProfileClick = onSwitchToThisProfileClick,
                onNavigateToVisibleAppSettings = onNavigateToVisibleAppSettings,
                onNavigateToAllowedContactSettings = onNavigateToAllowedContactSettings,
                onNavigateToAllowedAppSettings = onNavigateToAllowedAppSettings,
                onNavigateToNotificationSettings = onNavigateToNotificationSettings,
                onNavigateToAppearanceSettings = onNavigateToAppearanceSettings,
                onNavigateToLocationTriggerSettings = onNavigateToLocationTriggerSettings,
                onNavigateToTimeTriggerSettings = onNavigateToTimeTriggerSettings,
                onNavigateToMotionTriggerSettings = onNavigateToMotionTriggerSettings,
                onNavigateToSoundAndVibrationSettings = onNavigateToSoundAndVibrationSettings,
                onNavigateToPowerSavingSettings = onNavigateToPowerSavingSettings,
                onModeDeletionClick = onModeDeletionClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    profile: LauncherProfile,
    visibleApps: List<AppInfo>,
    isActiveProfile: Boolean,
    hasMultipleProfiles: Boolean,
    onProfileIconClick: () -> Unit,
    onEditProfileName: (String) -> Unit,
    onSwitchToThisProfileClick: () -> Unit,
    onNavigateToVisibleAppSettings: () -> Unit,
    onNavigateToAllowedContactSettings: () -> Unit,
    onNavigateToAllowedAppSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToAppearanceSettings: () -> Unit,
    onNavigateToLocationTriggerSettings: () -> Unit,
    onNavigateToTimeTriggerSettings: () -> Unit,
    onNavigateToMotionTriggerSettings: () -> Unit,
    onNavigateToSoundAndVibrationSettings: () -> Unit,
    onNavigateToPowerSavingSettings: () -> Unit,
    onModeDeletionClick: () -> Unit
) {
    var showProfileNameEditor by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            LauncherProfileSettingsTopBar(
                currentLauncherProfile = profile,
                isActiveProfile = isActiveProfile,
                onEditLauncherProfileName = {
                    showProfileNameEditor = true
                },
                onIconClick = onProfileIconClick,
                onSetActiveProfileClick = onSwitchToThisProfileClick,
                //modifier = Modifier.windowInsetsPadding(TopAppBarDefaults.windowInsets)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                    .clip(RoundedCornerShape(size = 12.dp))
            ) {
                SettingListItem(
                    title = stringResource(R.string.setting_title_visible_apps),
                    subtitle = visibleApps.joinToString(", ") { it.name },
                    onClick = onNavigateToVisibleAppSettings
                )
            }

            Text(
                text = stringResource(R.string.setting_title_customization_settings),
                style = FairphoneTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            )

            // Other settings
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                    .clip(RoundedCornerShape(size = 12.dp))
            ) {
                SettingListItem(
                    title = stringResource(R.string.setting_title_allowed_contacts),
                    subtitle = allowedContactSubtitle(profile.allowedContacts, profile.customContactsCount),
                    onClick = onNavigateToAllowedContactSettings
                )
                // TODO: Hidden for now until more testing on app notifications is done
//                SettingListItem(
//                    title = stringResource(R.string.setting_title_allowed_apps),
//                    subtitle = notificationSubtitle(profile.appNotificationsList.size),
//                    onClick = onNavigateToAllowedAppSettings
//                )
                SettingListItem(
                    title = stringResource(R.string.setting_title_notification),
                    subtitle = null,
                    //subtitle = notificationSubtitle(profile.appNotificationsList.size),
                    onClick = onNavigateToNotificationSettings
                )
                SettingListItem(
                    title = stringResource(R.string.setting_title_appearance),
                    subtitle = stringResource(R.string.setting_subtitle_appearance),
                    onClick = onNavigateToAppearanceSettings
                )
                SettingListItem(
                    title = stringResource(R.string.setting_title_location_trigger),
                    subtitle = if (profile.locationTrigger.enabled) {
                        stringResource(R.string.location_trigger_status_on)
                    } else {
                        stringResource(R.string.location_trigger_status_off)
                    },
                    onClick = onNavigateToLocationTriggerSettings
                )
                SettingListItem(
                    title = stringResource(R.string.setting_title_time_trigger),
                    subtitle = if (profile.dayTrigger.enabled || profile.timeTrigger.enabled) {
                        stringResource(R.string.location_trigger_status_on)
                    } else {
                        stringResource(R.string.location_trigger_status_off)
                    },
                    onClick = onNavigateToTimeTriggerSettings
                )
                SettingListItem(
                    title = stringResource(R.string.setting_title_motion_trigger),
                    subtitle = if (profile.motionTrigger.enabled) {
                        stringResource(R.string.location_trigger_status_on)
                    } else {
                        stringResource(R.string.location_trigger_status_off)
                    },
                    onClick = onNavigateToMotionTriggerSettings
                )
//                SettingListItem(
//                    enabled = false,
//                    title = stringResource(R.string.setting_title_appearance),
//                    subtitle = stringResource(
//                        R.string.setting_subtitle_appearance,
//                        contactType
//                    ),
//                    onClick = onNavigateToAppearanceSettings
//                )
//                SettingListItem(
//                    enabled = false,
//                    title = stringResource(R.string.setting_title_sound_and_vibration),
//                    subtitle = stringResource(
//                        R.string.setting_subtitle_sound_and_vibration,
//                        contactType
//                    ),
//                    onClick = onNavigateToSoundAndVibrationSettings
//                )
//                SettingListItem(
//                    enabled = false,
//                    title = stringResource(R.string.setting_title_power_saving),
//                    subtitle = stringResource(
//                        R.string.setting_subtitle_power_saving,
//                        contactType
//                    ),
//                    onClick = onNavigateToPowerSavingSettings
//                )
            }

            DeleteModeButton(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 24.dp),
                onModeDeletionClick = onModeDeletionClick,
                canDeleteMode = hasMultipleProfiles,
            )
        }

        ProfileNameEditorDialog(
            show = showProfileNameEditor,
            currentName = profile.name,
            onConfirm = {
                onEditProfileName(it)
                showProfileNameEditor = false
            },
            onDismiss = {
                showProfileNameEditor = false
            }
        )
    }
}

@Composable
fun DeleteModeButton(
    modifier: Modifier = Modifier,
    canDeleteMode: Boolean = true,
    onModeDeletionClick: () -> Unit
) {
    var showDeletionConfirmDialog by remember { mutableStateOf(false) }

    if (canDeleteMode) {
        Button(
            modifier = modifier,
            onClick = { showDeletionConfirmDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = errorColor,
                )
                Text(
                    text = stringResource(R.string.setting_button_delete_mode),
                    color = errorColor,
                    style = FairphoneTypography.BodySmall
                )
            }
        }
    } else {
        Text(
            text = stringResource(R.string.setting_button_delete_mode_not_allowed),
            style = FairphoneTypography.BodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
    }

    ConfirmDialog(
        show = showDeletionConfirmDialog,
        title = stringResource(R.string.setting_button_delete_mode),
        message = stringResource(R.string.setting_delete_mode_confirm),
        onConfirm = {
            showDeletionConfirmDialog = false
            onModeDeletionClick()
        },
        onDismiss = { showDeletionConfirmDialog = false }
    )
}

@Composable
fun ProfileSettings_Preview() {
    SpringLauncherTheme {
        ProfileSettingsScreen(
            profile = Mock_Profile,
            visibleApps = emptyList(),
            isActiveProfile = true,
            hasMultipleProfiles = true,
            onEditProfileName = {},
            onProfileIconClick = {},
            onNavigateToAllowedAppSettings = {},
            onSwitchToThisProfileClick = {},
            onNavigateToVisibleAppSettings = {},
            onNavigateToAllowedContactSettings = {},
            onNavigateToNotificationSettings = {},
            onNavigateToAppearanceSettings = {},
            onNavigateToLocationTriggerSettings = {},
            onNavigateToTimeTriggerSettings = {},
            onNavigateToMotionTriggerSettings = {},
            onNavigateToSoundAndVibrationSettings = {},
            onNavigateToPowerSavingSettings = {},
            onModeDeletionClick = {}
        )
    }
}

@Composable
@FP6Preview()
fun ProfileSettings_Preview_Light() {
    ProfileSettings_Preview()
}

@Composable
@FP6PreviewDark()
fun ProfileSettings_Preview_Dark() {
    ProfileSettings_Preview()
}