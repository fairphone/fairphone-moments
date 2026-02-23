/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.domain.usecase.profile

import com.fairphone.spring.launcher.data.model.CreateLauncherProfile
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.model.toLauncherProfile
import com.fairphone.spring.launcher.data.repository.LauncherProfileRepository
import com.fairphone.spring.launcher.domain.usecase.base.UseCase
import com.fairphone.spring.launcher.util.ZenNotificationManager
import java.util.UUID

/**
 * [UseCase] to create a new launcher profile.
 */
class CreateLauncherProfileUseCase(
    private val launcherProfileRepository: LauncherProfileRepository,
    private val zenNotificationManager: ZenNotificationManager,
) : UseCase<CreateLauncherProfile, LauncherProfile>() {

    override suspend fun execute(params: CreateLauncherProfile): Result<LauncherProfile> {
        return try {
            val launcherProfile = params.toLauncherProfile()

            val createdZenRuleIdResult = zenNotificationManager.addAutomaticZenRule(launcherProfile)

            if (createdZenRuleIdResult.isFailure) {
                return  Result.failure(createdZenRuleIdResult.exceptionOrNull() ?: Exception("Failed to create zen rule"))
            }

            val profileWithZenRule = launcherProfile.toBuilder()
                .setZenRuleId(createdZenRuleIdResult.getOrThrow())
                .build()

            launcherProfileRepository.createProfile(profile = profileWithZenRule)

            Result.success(profileWithZenRule)
        } catch (e: IllegalStateException) {
            Result.failure(e)
        }
    }

    companion object {
        fun newId(): String =
            UUID.randomUUID().toString()
    }
}