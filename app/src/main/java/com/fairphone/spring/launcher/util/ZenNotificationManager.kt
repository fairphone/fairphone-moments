/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.util

import android.app.AutomaticZenRule
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.service.notification.Condition
import android.service.notification.ZenDeviceEffects
import android.service.notification.ZenPolicy
import android.util.Log
import androidx.core.net.toUri
import com.fairphone.spring.launcher.activity.LauncherSettingsActivity
import com.fairphone.spring.launcher.data.model.protos.ContactType
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.model.protos.UiMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val ZEN_RULE_CONDITION_ID = "com.fairphone.moments"

interface ZenNotificationManager {
    suspend fun enableDnd(profile: LauncherProfile): Result<String>
    suspend fun disableDnd(profile: LauncherProfile): Result<String>
    suspend fun disableAllDnd(): Result<Unit>
    fun addAutomaticZenRule(profile: LauncherProfile): Result<String>
    suspend fun updateAutomaticZenRule(profile: LauncherProfile): Result<String>
    fun removeAutomaticZenRule(zenRuleId: String): Result<Unit>
}

/**
 * Manages Do Not Disturb (DND) settings and automatic Zen rules for launcher profiles.
 *
 * @param context The application context.
 * @param deviceSoundManager The manager for the device sound settings.
 * @param deviceAppearanceManager The manager for the device appearance settings.
 */
class ZenNotificationManagerImpl(
    private val context: Context,
    private val deviceSoundManager: DeviceSoundManager,
    private val deviceAppearanceManager: DeviceAppearanceManager,
) : ZenNotificationManager {

    companion object {
        const val LOG_TAG = "ZenNotificationManager"
    }

    /**
     * Enables Do Not Disturb mode for the given profile.
     * If the zen rule for the profile does not exist, it will be created.
     *
     * @param profile The launcher profile to enable DND for.
     * @return a [Result] containing the new zen rule id if it was created, otherwise null.
     */
    override suspend fun enableDnd(profile: LauncherProfile): Result<String> {
        Log.d(LOG_TAG, "Enabling Do Not Disturb mode for rule ${profile.name}")
        // Check if Do Not Disturb permission is granted
        if (!context.isDoNotDisturbAccessGranted()) {
            return Result.failure(IllegalStateException("Do Not Disturb access is not granted"))
        }

        deviceSoundManager.enableDeviceSoundSetting(profile.soundSetting)
        deviceAppearanceManager.enableBlueLightFilter(profile.blueLightFilterEnabled)

        val verifyZenRuleExistsResult = verifyZenRuleExists(profile)
        if (verifyZenRuleExistsResult.isFailure) {
            return verifyZenRuleExistsResult
        }

        val zenRuleId = verifyZenRuleExistsResult.getOrThrow()
        return withContext(Dispatchers.Main) {
            enableDndInternal(zenRuleId = zenRuleId, name = profile.name)
        }
    }

    /**
     * Disables Do Not Disturb mode for the given profile.
     *
     * @param profile The launcher profile to disable DND for.
     * @return a [Result] indicating success or failure.
     */
    override suspend fun disableDnd(profile: LauncherProfile): Result<String> {
        Log.d(LOG_TAG, "Disabling Do Not Disturb mode for rule ${profile.name}")
        // Check if Do Not Disturb permission is granted
        if (!context.isDoNotDisturbAccessGranted()) {
            return Result.failure(IllegalStateException("Do Not Disturb access is not granted"))
        }
        deviceSoundManager.disableDeviceSoundSetting()
        deviceAppearanceManager.disableBlueLightFilter()

        val verifyZenRuleExistsResult = verifyZenRuleExists(profile)
        if (verifyZenRuleExistsResult.isFailure) {
            return Result.failure(
                verifyZenRuleExistsResult.exceptionOrNull()
                    ?: IllegalStateException("Failed to verify Zen rule")
            )
        }

        val zenRuleId = verifyZenRuleExistsResult.getOrThrow()
        return withContext(Dispatchers.Main) {
            disableDndInternal(zenRuleId = zenRuleId, name = profile.name)
        }
    }

    /**
     * Disables Do Not Disturb mode for all automatic zen rules created by this app.
     *
     * @return a [Result] indicating success or failure.
     */
    override suspend fun disableAllDnd(): Result<Unit> {
        Log.d(LOG_TAG, "Disabling Do Not Disturb mode for all automatic zen rules")
        // Check if Do Not Disturb permission is granted
        if (!context.isDoNotDisturbAccessGranted()) {
            return Result.failure(IllegalStateException("Do Not Disturb access is not granted"))
        }
        deviceSoundManager.disableDeviceSoundSetting()
        deviceAppearanceManager.disableBlueLightFilter()

        val results = mutableListOf<Result<String>>()
        try {
            withContext(Dispatchers.Main) {
                context.notificationManager().automaticZenRules.forEach { (ruleId, rule) ->
                    results += disableDndInternal(zenRuleId = ruleId, name = rule.name)
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to fetch or disable zen rules", e)
            return Result.failure(e)
        }
        return if (results.all { it.isSuccess }) {
            Result.success(Unit)
        } else {
            val failedCount = results.count { it.isFailure }
            Result.failure(Exception("Failed to disable $failedCount automatic zen rules"))
        }
    }

    /**
     * Adds an automatic zen rule for the given profile. It will fail if a rule with the same name already exists.
     *
     * @param profile The launcher profile to create the rule for.
     * @return The ID of the created rule, or null on failure.
     */
    override fun addAutomaticZenRule(profile: LauncherProfile): Result<String> {
        // Check if Do Not Disturb permission is granted
        if (!context.isDoNotDisturbAccessGranted()) {
            return Result.failure(IllegalStateException("Do Not Disturb access is not granted"))
        }

        // Check if a rule with the same name already exists
        return try {
            val existingRule = context.notificationManager().automaticZenRules.entries.firstOrNull {
                it.value.name == profile.name
            }
            if (existingRule != null) {
                val errorMsg = "A Zen rule with the name ${profile.name} already exists."
                Log.e(LOG_TAG, errorMsg)
                return Result.failure(Exception(errorMsg))
            }
            val zenRule = buildAutomaticZenRule(profile = profile)
            Log.d(LOG_TAG, "Creating new Zen rule: ${zenRule.name}")
            val zenRuleId = context.notificationManager().addAutomaticZenRule(zenRule)
            Result.success(zenRuleId)

        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to create new Zen rule for profile: ${profile.name}", e)
            Result.failure(e)
        }
    }

    /**
     * Updates an existing automatic zen rule for the given profile.
     * If the app is the default launcher, it will temporarily disable DND during the update.
     *
     * @param profile The launcher profile with updated information.
     * @return a [Result] containing the updated [AutomaticZenRule] on success.
     */
    override suspend fun updateAutomaticZenRule(profile: LauncherProfile): Result<String> =
        withContext(Dispatchers.IO) {
            return@withContext updateAutomaticZenRuleInternal(profile = profile)
        }

    /**
     * Internal method to update an existing automatic zen rule.
     *
     * @param profile The launcher profile with updated information.
     * @return a [Result] containing the updated [AutomaticZenRule] on success.
     */
    private fun updateAutomaticZenRuleInternal(profile: LauncherProfile): Result<String> {
        // Check if Do Not Disturb permission is granted
        if (!context.isDoNotDisturbAccessGranted()) {
            return Result.failure(IllegalStateException("Do Not Disturb access is not granted"))
        }

        return try {
            // Update rule
            val updatedZenRule = buildAutomaticZenRule(profile = profile)
            val result = context.notificationManager().updateAutomaticZenRule(
                profile.zenRuleId,
                updatedZenRule
            )

            if (result) {
                Result.success(profile.zenRuleId)
            } else {
                Result.failure(Exception("Failed to update automatic zen rule"))
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to update automatic zen rule for id: ${profile.zenRuleId}", e)
            Result.failure(e)
        }
    }

    /**
     * Removes an existing automatic zen rule.
     *
     * @param zenRuleId The ID of the zen rule to remove.
     * @return a [Result] indicating success or failure.
     */
    override fun removeAutomaticZenRule(zenRuleId: String): Result<Unit> {
        // Check if Do Not Disturb permission is granted
        if (!context.isDoNotDisturbAccessGranted()) {
            return Result.failure(IllegalStateException("Do Not Disturb access is not granted"))
        }

        return try {
            if (!isZenRulePresent(zenRuleId)) {
                Result.failure(Exception("Failed to find zen rule with id $zenRuleId"))
            } else {
                val result = context.notificationManager().removeAutomaticZenRule(zenRuleId)
                if (result) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to remove automatic zen rule"))
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to remove automatic zen rule for id: $zenRuleId", e)
            Result.failure(e)
        }
    }

    /**
     * Enables Do Not Disturb mode for the given rule ID.
     *
     * @param zenRuleId The ID of the zen rule to enable.
     * @param name The name of the zen rule.
     * @return a [Result] indicating success or failure.
     */
    private fun enableDndInternal(zenRuleId: String, name: String): Result<String> {
        if (!isZenRulePresent(zenRuleId)) {
            return Result.failure(Exception("Failed to find zen rule with id $zenRuleId"))
        }

        return setAutomaticZenRuleState(
            zenRuleId = zenRuleId,
            name = name,
            state = Condition.STATE_TRUE
        )
    }

    /**
     * Disables Do Not Disturb mode for the given rule ID.
     *
     * @param zenRuleId The ID of the zen rule to disable.
     * @param name The name of the zen rule.
     * @return a [Result] indicating success or failure.
     */
    private fun disableDndInternal(zenRuleId: String, name: String): Result<String> {
        if (!isZenRulePresent(zenRuleId)) {
            return Result.failure(Exception("Failed to find zen rule with id $zenRuleId"))
        }

        return setAutomaticZenRuleState(
            zenRuleId = zenRuleId,
            name = name,
            state = Condition.STATE_FALSE
        )
    }

    /**
     * Sets the state of an automatic zen rule.
     *
     * @param zenRuleId The ID of the rule.
     * @param name The name of the rule.
     * @param state The new state for the rule (e.g., [Condition.STATE_TRUE]).
     * @return a [Result] indicating success or failure.
     */
    private fun setAutomaticZenRuleState(
        zenRuleId: String,
        name: String,
        state: Int
    ): Result<String> = try {
        val conditionId = getConditionId()
        val source = Condition.SOURCE_USER_ACTION
        val condition = Condition(conditionId, name, state, source)
        context.notificationManager().setAutomaticZenRuleState(zenRuleId, condition)
        Result.success(zenRuleId)
    } catch (e: Exception) {
        Log.e(LOG_TAG, "Error state $state for ZenRule $zenRuleId - $name")
        Result.failure(e)
    }

    /**
     * Builds an [AutomaticZenRule] from a [LauncherProfile].
     *
     * @param profile The launcher profile.
     * @return The constructed [AutomaticZenRule].
     */
    private fun buildAutomaticZenRule(profile: LauncherProfile): AutomaticZenRule {
        return buildAutomaticZenRule(
            name = profile.name,
            allowedContacts = profile.allowedContacts,
            uiMode = profile.uiMode,
            isGrayScaleEnabled = profile.grayScaleEnabled,
            repeatCallEnabled = profile.repeatCallEnabled
        )
    }

    /**
     * Builds an [AutomaticZenRule] with the specified parameters.
     *
     * @param name The name of the rule.
     * @param allowedContacts The contact type allowed.
     * @param uiMode The UI mode (dark or light).
     * @param isGrayScaleEnabled Whether grayscale is enabled.
     * @param repeatCallEnabled Whether to allow repeat callers.
     * @return The constructed [AutomaticZenRule].
     */
    private fun buildAutomaticZenRule(
        name: String,
        allowedContacts: ContactType,
        uiMode: UiMode,
        isGrayScaleEnabled: Boolean,
        repeatCallEnabled: Boolean,
    ): AutomaticZenRule {
        val conditionId = getConditionId()
        val configActivity = getConfigurationActivity(context)
        val zenPolicy = buildZenPolicy(allowedContacts, repeatCallEnabled)
        val zenDeviceEffects = buildZenDeviceEffects(
            uiMode = uiMode,
            isGrayScaleEnabled = isGrayScaleEnabled
        )

        return AutomaticZenRule.Builder(name, conditionId)
            .setConfigurationActivity(configActivity)
            .setOwner(configActivity)
            .setDeviceEffects(zenDeviceEffects)
            .setZenPolicy(zenPolicy)
            .build()
    }

    /**
     * Gets the component name for the configuration activity.
     *
     * @param context The application context.
     * @return The [ComponentName] for the settings activity.
     */
    private fun getConfigurationActivity(context: Context): ComponentName {
        return ComponentName(context, LauncherSettingsActivity::class.java)
    }

    /**
     * @return The [Uri] for the condition ID.
     */
    private fun getConditionId(): Uri = ZEN_RULE_CONDITION_ID.toUri()

    /**
     * Builds a [ZenPolicy] based on the allowed contacts and repeat caller settings.
     *
     * @param allowedContacts The type of contacts to allow.
     * @param allowRepeatCallers Whether to allow repeat callers.
     * @return The constructed [ZenPolicy].
     */
    private fun buildZenPolicy(
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

    /**
     * Builds a [ZenDeviceEffects] object.
     *
     * @param uiMode The UI mode to apply.
     * @param isGrayScaleEnabled Whether to enable grayscale display mode.
     * @return The constructed [ZenDeviceEffects].
     */
    private fun buildZenDeviceEffects(
        uiMode: UiMode,
        isGrayScaleEnabled: Boolean
    ): ZenDeviceEffects {
        return ZenDeviceEffects.Builder()
            .setShouldUseNightMode(uiMode == UiMode.UI_MODE_DARK)
            .setShouldDisplayGrayscale(isGrayScaleEnabled)
            .build()
    }

    /**
     * Verifies if a Zen rule exists for the given profile. If it doesn't exist, it attempts to add it.
     *
     * @param profile The launcher profile to verify the Zen rule for.
     * @return A [Result] containing the Zen rule ID if it exists or was successfully added,
     *         or a failure [Result] if an error occurred during creation.
     */
    private fun verifyZenRuleExists(profile: LauncherProfile): Result<String> {
        return try {
            val zenRule = context.notificationManager().getAutomaticZenRule(profile.zenRuleId)
            if (zenRule == null) {
                addAutomaticZenRule(profile)
            } else {
                Result.success(profile.zenRuleId)
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to get automatic zen rule for id: ${profile.zenRuleId}", e)
            Result.failure(e)
        }
    }

    /**
     * Checks if a Zen rule with the given ID is present.
     *
     * @param zenRuleId The ID of the Zen rule to check.
     * @return `true` if the Zen rule is present, `false` otherwise.
     */
    private fun isZenRulePresent(zenRuleId: String): Boolean {
        return try {
            context.notificationManager().getAutomaticZenRule(zenRuleId) != null
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to check if zen rule is present for id: $zenRuleId", e)
            false
        }
    }
}
