/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.domain.usecase.profile

import android.content.Context
import android.util.Log
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.CreateLauncherProfile
import com.fairphone.spring.launcher.data.model.Defaults
import com.fairphone.spring.launcher.data.model.LauncherColors
import com.fairphone.spring.launcher.data.model.protos.launcherProfileApp
import com.fairphone.spring.launcher.domain.usecase.base.UseCase
import com.fairphone.spring.launcher.util.isDoNotDisturbAccessGranted
import kotlinx.coroutines.flow.first

class InitializeSpringLauncherUseCase(
    private val context: Context,
    private val createLauncherProfileUseCase: CreateLauncherProfileUseCase,
    private val getAllProfilesUseCase: GetAllProfilesUseCase,
    private val setActiveProfileUseCase: SetActiveProfileUseCase,
) : UseCase<Unit, Unit>() {

    override suspend fun execute(params: Unit): Result<Unit> {
        val profiles = getAllProfilesUseCase.execute(Unit).first()
        if (profiles.isNotEmpty()) {
            return Result.failure(IllegalStateException("App already initialized"))
        }

        if (context.isDoNotDisturbAccessGranted()) {
            val result = createDefaultProfile(context)

            return result
        } else {
            return Result.failure(IllegalStateException("DND permission not granted"))
        }
    }

    private suspend fun createDefaultProfile(context: Context): Result<Unit> {
        val essentials = CreateLauncherProfile(
            id = CreateLauncherProfileUseCase.newId(),
            name = context.getString(R.string.default_profile_name),
            icon = Defaults.DEFAULT_ICON,
            bgColor1 = LauncherColors.Default.leftColor,
            bgColor2 = LauncherColors.Default.rightColor,
            launcherProfileApps = Defaults.DEFAULT_VISIBLE_APPS.map { app ->
                app.allApps.firstNotNullOf {
                    launcherProfileApp {
                        packageName = it.getPackageName(context)
                        isWorkApp = it.isWorkApp
                    }
                }
            },
            allowedContacts = Defaults.DEFAULT_ALLOWED_CONTACTS,
            repeatCallEnabled = Defaults.DEFAULT_REPEAT_CALL_ENABLED,
            wallpaperId = Defaults.DEFAULT_WALLPAPER_ID,
            uiMode = Defaults.DEFAULT_DARK_MODE_SETTING,
            blueLightFilterEnabled = Defaults.DEFAULT_BLUE_LIGHT_FILTER_ENABLED,
            grayScaleEnabled = Defaults.DEFAULT_GRAY_SCALE_ENABLED,
            soundSetting = Defaults.DEFAULT_SOUND_SETTING,
            batterySaverEnabled = Defaults.BATTERY_SAVER_ENABLED,
            reduceBrightnessEnabled = Defaults.REDUCE_BRIGHTNESS_ENABLED,
        )
        val result = createLauncherProfileUseCase.execute(essentials)

        return when  {
            result.isFailure -> {
                Log.e("InitializeSpringLauncher", "createDefaultProfile: ${result.exceptionOrNull()}", result.exceptionOrNull())
                Result.failure(result.exceptionOrNull() ?: Exception())
            }
            result.isSuccess -> {
                Log.d("InitializeSpringLauncher", "createDefaultProfile: Success")
                result.getOrNull()?.let { setActiveProfileUseCase.execute(it.id) }

                Result.success(Unit)
            }
            else -> Result.success(Unit)
        }


    }
}