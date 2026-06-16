/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.di

import com.fairphone.spring.launcher.activity.viewmodel.SwitchStateChangeViewModel
import com.fairphone.spring.launcher.ui.screen.home.HomeScreenViewModel
import com.fairphone.spring.launcher.ui.screen.home.MediaControlViewModel
import com.fairphone.spring.launcher.ui.screen.mode.creator.CreateModeViewModel
import com.fairphone.spring.launcher.ui.screen.mode.switcher.ModeSwitcherViewModel
import com.fairphone.spring.launcher.ui.screen.onboarding.OnBoardingStatusViewModel
import com.fairphone.spring.launcher.ui.screen.onboarding.OnBoardingViewModel
import com.fairphone.spring.launcher.ui.screen.settings.appearance.WallpaperSettingsViewModel
import com.fairphone.spring.launcher.ui.screen.settings.apps.VisibleAppSettingsViewModel
import com.fairphone.spring.launcher.ui.screen.settings.apps.selector.VisibleAppSelectorViewModel
import com.fairphone.spring.launcher.ui.screen.settings.contacts.AllowedContactSelectorViewModel
import com.fairphone.spring.launcher.ui.screen.settings.contacts.AllowedContactSettingsViewModel
import com.fairphone.spring.launcher.ui.screen.settings.main.ProfileSettingsViewModel
import com.fairphone.spring.launcher.ui.screen.settings.notifications.AllowedNotificationsAppsViewModel
import com.fairphone.spring.launcher.ui.screen.settings.notifications.NotificationsSettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val uiModule = module {
    viewModelOf(::HomeScreenViewModel)
    viewModelOf(::MediaControlViewModel)
    viewModelOf(::ModeSwitcherViewModel)
    viewModelOf(::ProfileSettingsViewModel)
    viewModelOf(::VisibleAppSettingsViewModel)
    viewModelOf(::VisibleAppSelectorViewModel)
    viewModelOf(::AllowedContactSettingsViewModel)
    viewModelOf(::NotificationsSettingsViewModel)
    viewModelOf(::AllowedNotificationsAppsViewModel)
    viewModelOf(::AllowedContactSelectorViewModel)
    viewModelOf(::SwitchStateChangeViewModel)
    viewModelOf(::CreateModeViewModel)
    viewModelOf(::OnBoardingViewModel)
    viewModelOf(::OnBoardingStatusViewModel)
    viewModelOf(::WallpaperSettingsViewModel)
}
