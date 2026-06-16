/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.di

import com.fairphone.spring.launcher.domain.usecase.EnableDndUseCase
import com.fairphone.spring.launcher.domain.usecase.contacts.GetAllContactsUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.CreateLauncherProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.DeleteLauncherProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.GetActiveProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.GetAllProfilesUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.GetApplicationUsageModeUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.GetEditedProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.InitializeSpringLauncherUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetActiveProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetApplicationUsageModeUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetDayTriggerUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetEditedProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetLocationTriggerUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetMotionTriggerUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SetTimeTriggerUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.SwitchToTriggeredProfileUseCase
import com.fairphone.spring.launcher.domain.usecase.profile.UpdateLauncherProfileUseCase
import com.fairphone.spring.launcher.util.LocationTriggerManager
import com.fairphone.spring.launcher.util.MotionClassifier
import com.fairphone.spring.launcher.util.ZenNotificationManager
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::EnableDndUseCase)
    factoryOf(::CreateLauncherProfileUseCase)
    factoryOf(::UpdateLauncherProfileUseCase)
    factoryOf(::GetActiveProfileUseCase)
    factoryOf(::GetAllProfilesUseCase)
    factoryOf(::GetEditedProfileUseCase)
    factoryOf(::SetActiveProfileUseCase)
    factoryOf(::SetEditedProfileUseCase)
    factoryOf(::InitializeSpringLauncherUseCase)
    factoryOf(::GetAllContactsUseCase)
    factoryOf(::DeleteLauncherProfileUseCase)
    factoryOf(::GetApplicationUsageModeUseCase)
    factoryOf(::SetApplicationUsageModeUseCase)
    factoryOf(::SetLocationTriggerUseCase)
    factoryOf(::SetTimeTriggerUseCase)
    factoryOf(::SetDayTriggerUseCase)
    factoryOf(::SetMotionTriggerUseCase)
    factoryOf(::SwitchToTriggeredProfileUseCase)

    factoryOf(::ZenNotificationManager)
    singleOf(::LocationTriggerManager)
    singleOf(::MotionClassifier)
}
