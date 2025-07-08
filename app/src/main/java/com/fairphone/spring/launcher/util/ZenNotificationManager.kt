/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.util

import android.app.AutomaticZenRule
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.Condition
import android.service.notification.ZenDeviceEffects
import android.service.notification.ZenPolicy
import android.util.Log
import androidx.core.net.toUri
import com.fairphone.spring.launcher.activity.LauncherSettingsActivity
import com.fairphone.spring.launcher.data.model.CreateLauncherProfile
import com.fairphone.spring.launcher.data.model.protos.ContactType
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.model.protos.UiMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ZenNotificationManager(
    private val context: Context,
    private val deviceSoundManager: DeviceSoundManager,
) {

    companion object {
        val ZEN_RULE_CONDITION_ID = "com.fairphone.moments".toUri()
        const val SETTING_BLUE_FILTER = "night_display_activated";
    }

    /**
     * Enables Do Not Disturb mode for the given rule.
     *
     * @throws IllegalStateException if Do Not Disturb permission is not granted
     */
    @Throws(IllegalStateException::class)
    suspend fun enableDnd(profile: LauncherProfile) {
        Log.d("ZenNotificationManager", "Enabling Do Not Disturb mode for rule ${profile.name}")
        // Check if Do Not Disturb permission is granted
        check(context.isDoNotDisturbAccessGranted())
        deviceSoundManager.enableDeviceSoundSetting(profile.soundSetting)
        withContext(Dispatchers.Main) {
            enableDnd(zenRuleId = profile.zenRuleId, name = profile.name)
        }

        setBlueLightFilterEnabled(profile.blueLightFilterEnabled)
    }

    /**
     * Disables Do Not Disturb mode for the given rule.
     *
     * @throws IllegalStateException if Do Not Disturb permission is not granted
     */
    @Throws(IllegalStateException::class)
    suspend fun disableDnd(profile: LauncherProfile) {
        Log.d("ZenNotificationManager", "Disabling Do Not Disturb mode for rule ${profile.name}")
        // Check if Do Not Disturb permission is granted
        check(context.isDoNotDisturbAccessGranted())
        deviceSoundManager.disableDeviceSoundSetting()
        withContext(Dispatchers.Main) {
            disableDnd(zenRuleId = profile.zenRuleId, name = profile.name)
        }

        setBlueLightFilterEnabled(false)
    }

    /**
     * Disables Do Not Disturb mode for all automatic zen rules.
     */
    @Throws(IllegalStateException::class)
    suspend fun disableAllDnd() {
        Log.d("ZenNotificationManager", "Disabling Do Not Disturb mode for all automatic zen rules")
        // Check if Do Not Disturb permission is granted
        check(context.isDoNotDisturbAccessGranted())
        deviceSoundManager.disableDeviceSoundSetting()
        withContext(Dispatchers.Main) {
            context.notificationManager().automaticZenRules.forEach { (ruleId, rule) ->
                disableDnd(zenRuleId = ruleId, name = rule.name)
            }
        }

        setBlueLightFilterEnabled(false)
    }

    /**
     * Creates a new automatic zen rule using the given parameters and adds it to the notification manager.
     *
     * @throws IllegalStateException if Do Not Disturb permission is not granted
     */
    @Throws(IllegalStateException::class)
    fun createAutomaticZenRule(profile: CreateLauncherProfile): String {
        // Check if Do Not Disturb permission is granted
        check(context.isDoNotDisturbAccessGranted())

        val zenRule = createZenRule(
            name = profile.name,
            allowedContacts = profile.allowedContacts,
            uiMode = profile.uiMode,
            repeatCallEnabled = profile.repeatCallEnabled,
            isGrayScaleEnabled = profile.grayScaleEnabled,
        )
        return context.notificationManager().addAutomaticZenRule(zenRule)
    }

    /**
     * Updates an existing automatic zen rule using the given parameters.
     */
    suspend fun updateAutomaticZenRule(profile: LauncherProfile): Result<AutomaticZenRule> {
        if (context.isAppDefaultLauncher()) {
            // Disable DND first
            disableDnd(profile = profile)
        }

        val result = updateAutomaticZenRule(
            zenRuleId = profile.zenRuleId,
            name = profile.name,
            allowedContacts = profile.allowedContacts,
            uiMode = profile.uiMode,
            repeatCallEnabled = profile.repeatCallEnabled,
            isGrayScaleEnabled = profile.grayScaleEnabled,
        )

        if (context.isAppDefaultLauncher()) {
            // Enable DND again
            enableDnd(profile = profile)
        }

        return result
    }

    /**
     * Updates an existing automatic zen rule using the given parameters.
     */
    @Throws(IllegalStateException::class)
    private fun updateAutomaticZenRule(
        zenRuleId: String,
        name: String,
        allowedContacts: ContactType,
        uiMode: UiMode,
        repeatCallEnabled: Boolean,
        isGrayScaleEnabled: Boolean,
    ): Result<AutomaticZenRule> {
        // Check if Do Not Disturb permission is granted
        check(context.isDoNotDisturbAccessGranted())
        check(context.notificationManager().getAutomaticZenRule(zenRuleId) != null)

        // Update rule
        val updatedZenRule = createZenRule(
            name = name,
            allowedContacts = allowedContacts,
            uiMode = uiMode,
            repeatCallEnabled = repeatCallEnabled,
            isGrayScaleEnabled = isGrayScaleEnabled,
        )
        val result = context.notificationManager().updateAutomaticZenRule(zenRuleId, updatedZenRule)

        return if (result) {
            Result.success(context.notificationManager().getAutomaticZenRule(zenRuleId))
        } else {
            Result.failure(Exception("Failed to update automatic zen rule"))
        }
    }

    /**
     * Removes an existing automatic zen rule.
     */
    @Throws(IllegalStateException::class)
    fun removeAutomaticZenRule(zenRuleId: String): Result<Unit> {
        // Check if Do Not Disturb permission is granted
        check(context.isDoNotDisturbAccessGranted())
        check(context.notificationManager().getAutomaticZenRule(zenRuleId) != null)

        val result = context.notificationManager().removeAutomaticZenRule(zenRuleId)
        return if (result) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to remove automatic zen rule"))
        }
    }

    /**
     * Enables Do Not Disturb mode for the given rule.
     *
     * @throws IllegalStateException if Do Not Disturb permission is not granted
     */
    @Throws(IllegalStateException::class)
    private fun enableDnd(zenRuleId: String, name: String) {
        // Check if Do Not Disturb permission is granted
        check(context.isDoNotDisturbAccessGranted())

        setAutomaticZenRuleState(
            zenRuleId = zenRuleId,
            name = name,
            state = Condition.STATE_TRUE
        )
    }

    /**
     * Disables Do Not Disturb mode for the given rule.
     *
     * @throws IllegalStateException if Do Not Disturb permission is not granted
     */
    @Throws(IllegalStateException::class)
    private fun disableDnd(zenRuleId: String, name: String) {
        // Check if Do Not Disturb permission is granted
        check(context.isDoNotDisturbAccessGranted())

        setAutomaticZenRuleState(
            zenRuleId = zenRuleId,
            name = name,
            state = Condition.STATE_FALSE
        )
    }

    /**
     * Sets the state of the given automatic zen rule.
     */
    private fun setAutomaticZenRuleState(
        zenRuleId: String,
        name: String,
        state: Int
    ) {
        context.notificationManager()
            .setAutomaticZenRuleState(
                zenRuleId,
                Condition(
                    ZEN_RULE_CONDITION_ID,
                    name,
                    state,
                    Condition.SOURCE_USER_ACTION
                )
            )
    }

    /**
     * Creates an automatic zen rule using the given parameters.
     */
    private fun createZenRule(
        name: String,
        allowedContacts: ContactType,
        uiMode: UiMode,
        isGrayScaleEnabled: Boolean,
        repeatCallEnabled: Boolean,
    ): AutomaticZenRule {
        val configActivity = getConfigurationActivity(context)
        val zenPolicy = createZenPolicy(allowedContacts, repeatCallEnabled)
        val zenDeviceEffects = createZenDeviceEffects(
            uiMode = uiMode,
            isGrayScaleEnabled = isGrayScaleEnabled
        )

        return AutomaticZenRule.Builder(name, ZEN_RULE_CONDITION_ID)
            .setConfigurationActivity(configActivity)
            .setOwner(configActivity)
            .setDeviceEffects(zenDeviceEffects)
            .setZenPolicy(zenPolicy)
            .build()
    }

    private fun getConfigurationActivity(context: Context): ComponentName {
        return ComponentName(
            context.packageName,
            LauncherSettingsActivity::class.java.packageName + ".${LauncherSettingsActivity::class.java.simpleName}"
        )
    }

    private fun createZenPolicy(
        allowedContacts: ContactType,
        allowRepeatCallers: Boolean,
    ): ZenPolicy {
        val peopleType = when (allowedContacts) {
            ContactType.CONTACT_TYPE_EVERYONE -> ZenPolicy.PEOPLE_TYPE_ANYONE
            ContactType.CONTACT_TYPE_NONE -> ZenPolicy.PEOPLE_TYPE_NONE
            ContactType.CONTACT_TYPE_ALL_CONTACTS -> ZenPolicy.PEOPLE_TYPE_CONTACTS
            ContactType.CONTACT_TYPE_STARRED -> ZenPolicy.PEOPLE_TYPE_STARRED
            ContactType.CONTACT_TYPE_CUSTOM -> ZenPolicy.PEOPLE_TYPE_UNSET
            ContactType.UNRECOGNIZED -> ZenPolicy.PEOPLE_TYPE_NONE
        }

        val builder = ZenPolicy.Builder()
            .allowCalls(peopleType)
            .allowMessages(peopleType)
            .allowConversations(peopleType)
            .allowMedia(true)
            .allowRepeatCallers(allowRepeatCallers)
            .hideAllVisualEffects()

        return builder.build()
    }

    private fun createZenDeviceEffects(uiMode: UiMode, isGrayScaleEnabled: Boolean): ZenDeviceEffects {
        return ZenDeviceEffects.Builder()
            .setShouldUseNightMode(uiMode == UiMode.UI_MODE_DARK)
            .setShouldDisplayGrayscale(isGrayScaleEnabled)
            .build()
    }

    private fun setBlueLightFilterEnabled(enable: Boolean) {
        Settings.Secure.putInt(
            context.contentResolver,
            SETTING_BLUE_FILTER,
            if (enable) 1 else 0
        )
    }
}
