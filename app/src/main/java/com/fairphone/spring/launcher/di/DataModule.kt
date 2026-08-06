/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.fairphone.spring.launcher.analytics.AnalyticsService
import com.fairphone.spring.launcher.analytics.FirebaseAnalyticsService
import com.fairphone.spring.launcher.data.datasource.DeviceContactDataSource
import com.fairphone.spring.launcher.data.datasource.DeviceContactDataSourceImpl
import com.fairphone.spring.launcher.data.datasource.MIGRATION_LAUNCHER_PROFILE_APPS
import com.fairphone.spring.launcher.data.datasource.ProfileDataSource
import com.fairphone.spring.launcher.data.datasource.ProfileDataSourceImpl
import com.fairphone.spring.launcher.data.model.protos.LauncherProfiles
import com.fairphone.spring.launcher.data.prefs.AppPrefs
import com.fairphone.spring.launcher.data.prefs.AppPrefsImpl
import com.fairphone.spring.launcher.data.repository.AppInfoRepository
import com.fairphone.spring.launcher.data.repository.AppInfoRepositoryImpl
import com.fairphone.spring.launcher.data.repository.LauncherProfileRepository
import com.fairphone.spring.launcher.data.repository.LauncherProfileRepositoryImpl
import com.fairphone.spring.launcher.data.serializer.LauncherProfilesSerializer
import com.fairphone.spring.launcher.util.DeviceAppearanceManager
import com.fairphone.spring.launcher.util.DeviceAppearanceManagerImpl
import com.fairphone.spring.launcher.util.ZenNotificationManager
import com.fairphone.spring.launcher.util.ZenNotificationManagerImpl
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.analytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    singleOf(::AppInfoRepositoryImpl) { bind<AppInfoRepository>() }
    singleOf(::LauncherProfileRepositoryImpl) { bind<LauncherProfileRepository>() }
    singleOf(::DeviceContactDataSourceImpl) { bind<DeviceContactDataSource>() }
    single<ProfileDataSource> { ProfileDataSourceImpl(androidContext().profileDataStore) }
    single<AppPrefs> { AppPrefsImpl(androidContext().appPrefsDataStore) }
    single {
        FirebaseApp.initializeApp(get())
        Firebase.analytics }
    singleOf(::FirebaseAnalyticsService) { bind<AnalyticsService>() }
    factoryOf(::ZenNotificationManagerImpl) { bind<ZenNotificationManager>() }
    factoryOf(::DeviceAppearanceManagerImpl) { bind<DeviceAppearanceManager>() }
}

/**
 * DataStore used to store App Prefs
 */
val Context.appPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_prefs",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

/**
 * DataStore used to store LauncherProfiles
 */
val Context.profileDataStore: DataStore<LauncherProfiles> by dataStore(
    fileName = "profiles.pb",
    serializer = LauncherProfilesSerializer,
    produceMigrations = { context ->
        listOf(MIGRATION_LAUNCHER_PROFILE_APPS)
    },
    corruptionHandler = ReplaceFileCorruptionHandler {
        LauncherProfiles.getDefaultInstance()
    }
)
