/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2026. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.domain.usecase.profile

import android.content.Context
import android.util.Log
import com.fairphone.spring.launcher.data.repository.LauncherProfileRepository
import com.fairphone.spring.launcher.domain.usecase.base.UseCase
import com.fairphone.spring.launcher.util.ZenNotificationManager
import com.fairphone.spring.launcher.util.isDoNotDisturbAccessGranted
import kotlinx.coroutines.flow.first

/**
 * Reconciles the set of Android [android.app.AutomaticZenRule]s owned by this package with the
 * persisted launcher profiles. Handles two orphan cases:
 *
 *  - Forward orphan: a system zen rule with no matching profile (stale rule from a prior install
 *    or from cleared app data) -> remove it.
 *  - Reverse orphan: a profile whose `zenRuleId` no longer exists in the system (partial-failure
 *    scenario) -> recreate the rule and persist the new id on the profile.
 */
class SweepOrphanZenRulesUseCase(
    private val context: Context,
    private val getAllProfilesUseCase: GetAllProfilesUseCase,
    private val zenNotificationManager: ZenNotificationManager,
    private val launcherProfileRepository: LauncherProfileRepository,
) : UseCase<Unit, Unit>() {

    companion object {
        const val LOG_TAG = "SweepOrphanZenRules"
    }

    override suspend fun execute(params: Unit): Result<Unit> {
        if (!context.isDoNotDisturbAccessGranted()) {
            return Result.success(Unit)
        }

        val profiles = getAllProfilesUseCase.execute(Unit).first()
        val ruleIds = zenNotificationManager.getRuleIds()
        val profileRuleIds = profiles.map { it.zenRuleId }.toSet()
        val orphanRuleIds = ruleIds - profileRuleIds

        // Remove orphan zel rules
        orphanRuleIds.forEach { orphanRuleId ->
            zenNotificationManager.removeAutomaticZenRule(orphanRuleId)
                .onFailure { Log.e(LOG_TAG, "Failed to remove orphan rule $orphanRuleId", it) }
        }

        // Add missing rules for existing profiles
        profiles.filter { it.zenRuleId !in ruleIds }.forEach { profile ->
            val addResult = zenNotificationManager.addAutomaticZenRule(profile)
            addResult.fold(
                onSuccess = { newId ->
                    val updated = profile.toBuilder().setZenRuleId(newId).build()
                    runCatching { launcherProfileRepository.updateProfile(updated) }
                        .onFailure {
                            Log.e(LOG_TAG, "Failed to persist new zenRuleId for ${profile.name}", it)
                        }
                },
                onFailure = {
                    Log.e(LOG_TAG, "Failed to recreate zen rule for ${profile.name}", it)
                }
            )
        }

        return Result.success(Unit)
    }
}
