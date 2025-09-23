/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.model

import android.content.Context
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.protos.ContactType
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.model.protos.launcherProfile

const val PROFILE_ID_CUSTOM: String = "custom"

enum class Preset(
    val id: String,
    val title: Int,
    val subtitle: Int,
    val icon: String,
    val colors: LauncherColors,
    val defaultApps: List<AppPreset>
) {
    Custom(
        id = PROFILE_ID_CUSTOM,
        title = R.string.mode_title_custom,
        subtitle = R.string.mode_subtitle_custom,
        icon = "Extra6",
        colors = LauncherColors.Custom,
        defaultApps = emptyList()
    ),
    DeepFocus(
        id = "deep_focus",
        title = R.string.mode_title_deep_focus,
        subtitle = R.string.mode_subtitle_deep_focus,
        icon = "DeepFocus",
        colors = LauncherColors.DeepFocus,
        defaultApps = listOf(
            AppPreset(GoogleGmail),
            AppPreset(GoogleCalendar),
            AppPreset(GoogleDrive),
            AppPreset(GoogleMeet),
            AppPreset(GoogleKeepNotes),
        )
    ),
    Journey(
        id = "journey",
        title = R.string.mode_title_journey,
        subtitle = R.string.mode_subtitle_journey,
        icon = "Journey",
        colors = LauncherColors.Journey,
        defaultApps = listOf(
            AppPreset(Waze, alternatives = listOf(Navigation)),
            AppPreset(Spotify, alternatives = listOf(Tidal, Deezer, GoogleYoutubeMusic)),
            AppPreset(Phone)
        )
    ),
    Recharge(
        id = "recharge",
        title = R.string.mode_title_recharge,
        subtitle = R.string.mode_subtitle_recharge,
        icon = "Recharge",
        colors = LauncherColors.Recharge,
        defaultApps = listOf(
            AppPreset(Spotify, alternatives = listOf(Tidal, Deezer, GoogleYoutubeMusic)),
            AppPreset(Headspace, alternatives = listOf(Calm, Clock)),
        )
    ),
    QualityTime(
        id = "qualitytime",
        title = R.string.mode_title_quality_time,
        subtitle = R.string.mode_subtitle_quality_time,
        icon = "QualityTime",
        colors = LauncherColors.QualityTime,
        defaultApps = listOf(
            AppPreset(Camera),
            AppPreset(GooglePhoto),
            AppPreset(Whatsapp, alternatives = listOf(Messages)),
        )
    );

    /**
     * Returns a list of [AppInfo] that are visible in the launcher based on the installed apps.
     * @param context The context.
     * @param installedApps The list of installed apps.
     * @return The list of visible [AppInfo].
     */
    fun getVisibleAppInfos(
        context: Context,
        installedApps: List<AppInfo> = emptyList()
    ): List<AppInfo> {
        return defaultApps.flatMap { defaultApp ->
            val defaultAppPackageNames = defaultApp.allApps.map { it.getPackageName(context) }
            defaultAppPackageNames.mapNotNull { packageName ->
                installedApps.firstOrNull { it.packageName == packageName }
            }
        }
    }
}

/**
 * Mock LauncherProfile used for previews
 */
val Mock_Profile = launcherProfile {
    id = "spring"
    name = "Spring"
    icon = "Spring"
    bgColor1 = 0xFF14241E
    bgColor2 = 0xFF0B1410
    allowedContacts = ContactType.CONTACT_TYPE_CUSTOM
    customContacts.addAll(listOf("Contact 1", "Contact 2", "Contact 3"))
}

fun LauncherProfile.colors() = LauncherColors(
    rightColor = bgColor2,
    leftColor = bgColor1,
)

fun CreateLauncherProfile.colors() = LauncherColors(
    rightColor = bgColor2,
    leftColor = bgColor1,
)