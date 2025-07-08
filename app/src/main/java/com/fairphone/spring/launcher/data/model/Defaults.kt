/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */


package com.fairphone.spring.launcher.data.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.fairphone.spring.launcher.data.model.protos.ContactType
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.model.protos.SoundSetting
import com.fairphone.spring.launcher.data.model.protos.UiMode
import com.fairphone.spring.launcher.ui.icons.mode.fromString

fun LauncherProfile.getIconVector(): ImageVector =
    ImageVector.fromString(icon)


object Defaults {
    const val DEFAULT_ICON = "Spring"
    val DEFAULT_VISIBLE_APPS = listOf(
        AppPreset(default = Camera),
        AppPreset(default = Browser),
        AppPreset(default = Navigation),
        AppPreset(default = Messages),
        AppPreset(default = Phone),
    )
    val DEFAULT_ALLOWED_CONTACTS = ContactType.CONTACT_TYPE_STARRED
    const val DEFAULT_REPEAT_CALL_ENABLED = true
    const val DEFAULT_WALLPAPER_ID = 0
    val DEFAULT_DARK_MODE_SETTING = UiMode.UI_MODE_SYSTEM
    const val DEFAULT_BLUE_LIGHT_FILTER_ENABLED = false
    const val DEFAULT_GRAY_SCALE_ENABLED = false
    val DEFAULT_SOUND_SETTING = SoundSetting.SOUND_SETTING_FOLLOW_DEVICE_SETTINGS
    const val BATTERY_SAVER_ENABLED = false
    const val REDUCE_BRIGHTNESS_ENABLED = false
}
