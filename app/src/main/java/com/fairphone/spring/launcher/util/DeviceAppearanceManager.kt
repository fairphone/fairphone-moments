/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.util

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.fairphone.spring.launcher.data.prefs.AppPrefs

interface DeviceAppearanceManager {
    suspend fun enableBlueLightFilter(enable: Boolean)
    suspend fun disableBlueLightFilter()
}

/**
 * Manages device appearance settings, specifically the blue light filter.
 */
class DeviceAppearanceManagerImpl(
    private val context: Context,
    private val appPrefs: AppPrefs,
) : DeviceAppearanceManager {
    companion object {
        const val LOG_TAG = "DeviceAppearanceManager"
        const val SETTING_BLUE_FILTER = "night_display_activated"
    }

    /**
     * Enables or disables the blue light filter.
     * Saves the current state before applying the new state.
     * @param enable True to enable, false to disable.
     */
    override suspend fun enableBlueLightFilter(enable: Boolean) {
        saveBlueLightFilterSettingState()
        setBlueLightFilterEnabled(enable)
    }

    /**
     * Disables the blue light filter by restoring its state to what it was before it was enabled.
     */
    override suspend fun disableBlueLightFilter() {
        restoreBlueLightFilterSettingsState()
    }

    /**
     * Sets the blue light filter enabled state.
     * This method directly interacts with the system settings.
     *
     * @param enable True to enable the blue light filter, false to disable it.
     * @throws SecurityException if the app does not have the WRITE_SECURE_SETTINGS permission.
     */
    private fun setBlueLightFilterEnabled(enable: Boolean) {
        try {
            Settings.Secure.putInt(
                context.contentResolver,
                SETTING_BLUE_FILTER,
                if (enable) 1 else 0
            )
        } catch (e: SecurityException) {
            Log.e(
                LOG_TAG,
                "Failed to set blue light filter. App may need WRITE_SECURE_SETTINGS permission.",
                e
            )
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Unexpected error setting blue light filter", e)
        }
    }

    /**
     * Saves the current blue light filter state to preferences.
     */
    private suspend fun saveBlueLightFilterSettingState() {
        try {
            val isEnabled = Settings.Secure.getInt(
                context.contentResolver,
                SETTING_BLUE_FILTER,
            ) == 1
            appPrefs.setBlueLightFilter(isEnabled)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to save blue light filter state", e)
        }
    }

    /**
     * Restores the blue light filter state from preferences.
     */
    private suspend fun restoreBlueLightFilterSettingsState() {
        val originalState = appPrefs.isBlueLightFilterEnabled()
        setBlueLightFilterEnabled(originalState)
    }
}
