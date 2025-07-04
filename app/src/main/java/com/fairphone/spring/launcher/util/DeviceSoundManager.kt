/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.util

import android.content.Context
import android.media.AudioManager
import com.fairphone.spring.launcher.data.model.protos.SoundSetting
import com.fairphone.spring.launcher.data.prefs.AppPrefs

class DeviceSoundManager(
    private val context: Context,
    private val appPrefs: AppPrefs,
) {

    private val audioManager: AudioManager
        get() = context.audioManager()

    /**
     * Enables the specified sound setting on the device.
     *
     * @param soundSetting The sound setting to enable.
     * @throws IllegalStateException if Do Not Disturb permission is not granted.
     */
    @Throws(IllegalStateException::class)
    suspend fun enableDeviceSoundSetting(soundSetting: SoundSetting) {
        // Check if Do Not Disturb permission is granted
        check(context.isDoNotDisturbAccessGranted())

        // Save current mode
        saveDeviceRingerMode()

        when (soundSetting) {
            SoundSetting.SOUND_SETTING_LOUD -> {
                // Set ringer mode to normal
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }

            SoundSetting.SOUND_SETTING_VIBRATE -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            }

            SoundSetting.SOUND_SETTING_SILENT -> {
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            }

            SoundSetting.SOUND_SETTING_FOLLOW_DEVICE_SETTINGS,
            SoundSetting.UNRECOGNIZED -> {} // Ignore
        }
    }

    /**
     * Disables the current sound setting and restores the device's original sound settings.
     *
     * @throws IllegalStateException if Do Not Disturb permission is not granted.
     */
    @Throws(IllegalStateException::class)
    suspend fun disableDeviceSoundSetting() {
        // Check if Do Not Disturb permission is granted
        check(context.isDoNotDisturbAccessGranted())

        restoreDeviceRingerMode()
    }

    /**
     * Saves the current device ringer mode, notification volume, and ring volume to AppPrefs.
     * This is used to restore the settings later.
     */
    private suspend fun saveDeviceRingerMode() {
        val ringerMode = audioManager.ringerMode
        appPrefs.setRingerMode(ringerMode)
    }

    /**
     * Restores the device ringer mode, notification volume, and ring volume from AppPrefs.
     * This is used to revert to the original settings when a sound setting is disabled.
     */
    private suspend fun restoreDeviceRingerMode() {
        val savedRingerMode = appPrefs.getRingerMode()
        audioManager.ringerMode = savedRingerMode
    }
}
