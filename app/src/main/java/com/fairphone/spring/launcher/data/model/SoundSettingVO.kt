/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.model

import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.protos.SoundSetting

data class SoundSettingVO(
    val soundSetting: SoundSetting,
    val titleResource: Int,
    val subtitleResource: Int
)

fun SoundSetting.toVO() = SoundSettingVO(
    soundSetting = this,
    titleResource = when (this) {
        SoundSetting.SOUND_SETTING_FOLLOW_DEVICE_SETTINGS -> R.string.sound_and_vibration_match_title
        SoundSetting.SOUND_SETTING_LOUD -> R.string.sound_and_vibration_loud_title
        SoundSetting.SOUND_SETTING_VIBRATE -> R.string.sound_and_vibration_vibrate_title
        SoundSetting.SOUND_SETTING_SILENT -> R.string.sound_and_vibration_silent_title
        else -> R.string.sound_and_vibration_match_title
    },
    subtitleResource = when (this) {
        SoundSetting.SOUND_SETTING_FOLLOW_DEVICE_SETTINGS -> R.string.sound_and_vibration_match_subtitle
        SoundSetting.SOUND_SETTING_LOUD -> R.string.sound_and_vibration_loud_subtitle
        SoundSetting.SOUND_SETTING_VIBRATE -> R.string.sound_and_vibration_vibrate_subtitle
        SoundSetting.SOUND_SETTING_SILENT -> R.string.sound_and_vibration_silent_subtitle
        else -> R.string.sound_and_vibration_match_subtitle

    }
)