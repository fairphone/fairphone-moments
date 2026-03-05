/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class UsageMode {
    /**
     * When a user turns on the default Moment for the first time an extra button is shown
     * on the home and the user can update the settings.
     */
    ON_BOARDING,

    /**
     * When the onboarding is complete or when the user chooses to stop this process we use
     * this usage to display a tooltip on the home page
     */
    ON_BOARDING_COMPLETE,

    /**
     * This is the default mode when the user has decided to not display anymore the tooltip
     * on the home
     */
    DEFAULT
}

interface AppPrefs {
    suspend fun isFirstTimeUse(): Boolean
    suspend fun setFirstTimeUse(value: Boolean)
    suspend fun usageMode(): UsageMode
    suspend fun setUsageMode(usageMode: UsageMode)

    suspend fun setRingerMode(ringerMode: Int)
    suspend fun getRingerMode(): Int

    suspend fun getDeviceUiMode(): Int
    suspend fun setDeviceUiMode(mode: Int)

    suspend fun isBlueLightFilterEnabled(): Boolean
    suspend fun setBlueLightFilter(enabled: Boolean)
}

class AppPrefsImpl(private val dataStore: DataStore<Preferences>) : AppPrefs {
    companion object {
        val FIRST_TIME_USE = booleanPreferencesKey("first_time_use")
        val ONBOARDING_COMPLETION = stringPreferencesKey("onboarding_complete")
        val DEVICE_RINGER_MODE = intPreferencesKey("device_ringer_mode")
        val DEVICE_UI_MODE = intPreferencesKey("device_ui_mode")
        val BLUE_LIGHT_FILTER = booleanPreferencesKey("blue_light_filter")
    }

    override suspend fun isFirstTimeUse(): Boolean {
        return dataStore.data.map { prefs ->
            prefs[FIRST_TIME_USE] != false
        }.first()
    }

    override suspend fun setFirstTimeUse(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[FIRST_TIME_USE] = value
        }
    }

    override suspend fun usageMode(): UsageMode =
        dataStore.data
            .map { prefs ->
                try {
                    UsageMode.valueOf(prefs[ONBOARDING_COMPLETION] ?: UsageMode.ON_BOARDING.name)
                } catch (e: IllegalArgumentException) {
                    UsageMode.ON_BOARDING
                }
            }
            .first()

    override suspend fun setUsageMode(usageMode: UsageMode) {
        dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETION] = usageMode.name
        }
    }

    override suspend fun getRingerMode(): Int {
        return dataStore.data.map { prefs ->
            prefs[DEVICE_RINGER_MODE] ?: -1
        }.first()
    }

    override suspend fun setRingerMode(ringerMode: Int) {
        dataStore.edit { prefs ->
            prefs[DEVICE_RINGER_MODE] = ringerMode
        }
    }

    override suspend fun getDeviceUiMode(): Int {
        return dataStore.data.map { prefs ->
            prefs[DEVICE_UI_MODE] ?: -1
        }.first()
    }

    override suspend fun setDeviceUiMode(mode: Int) {
        dataStore.edit { prefs ->
            prefs[DEVICE_UI_MODE] = mode
        }
    }

    override suspend fun isBlueLightFilterEnabled(): Boolean {
        return dataStore.data.map { prefs ->
            prefs[BLUE_LIGHT_FILTER] ?: false
        }.first()
    }

    override suspend fun setBlueLightFilter(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[BLUE_LIGHT_FILTER] = enabled
        }
    }
}
