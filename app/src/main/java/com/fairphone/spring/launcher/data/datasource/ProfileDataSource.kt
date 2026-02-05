/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.datasource

import android.util.Log
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.model.protos.LauncherProfileApp
import com.fairphone.spring.launcher.data.model.protos.LauncherProfiles
import com.fairphone.spring.launcher.data.model.protos.launcherProfiles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import java.io.IOException

interface ProfileDataSource {
    fun getActiveProfile(): Flow<LauncherProfile>
    fun getEditedProfile(): Flow<LauncherProfile>
    fun getProfiles(): Flow<List<LauncherProfile>>

    suspend fun setActiveProfile(profileId: String)
    suspend fun setEditedProfile(profileId: String)
    suspend fun createLauncherProfile(profile: LauncherProfile)
    suspend fun deleteLauncherProfile(profile: LauncherProfile)
    suspend fun updateLauncherProfile(profile: LauncherProfile)
    suspend fun updateVisibleApps(profileId: String, visibleApps: List<LauncherProfileApp>)
}

class ProfileDataSourceImpl(private val dataStore: DataStore<LauncherProfiles>) :
    ProfileDataSource {

    override fun getActiveProfile(): Flow<LauncherProfile> =
        getLauncherProfiles().transform { profile ->
            if (profile.profilesList.isNotEmpty()) {
                val activeProfile = profile.profilesList.firstOrNull { it.id == profile.active }
                    ?: profile.profilesList.first()

                emit(activeProfile)
            }
        }

    override fun getEditedProfile(): Flow<LauncherProfile> =
        getLauncherProfiles().transform { profile ->
            val profileList = profile.profilesList
            if (profileList.isNotEmpty()) {
                val editedProfile = profile.profilesList.firstOrNull { it.id == profile.edited }
                    ?: profile.profilesList.firstOrNull { it.id == profile.active }
                    ?: profile.profilesList.first()
                emit(editedProfile)
            }
        }

    override fun getProfiles(): Flow<List<LauncherProfile>> =
        getLauncherProfiles().map { it.profilesList }

    private fun getLauncherProfiles(): Flow<LauncherProfiles> {
        return dataStore.data.catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e("ProfileDataSource", "Error reading sort order preferences.", exception)
                emit(LauncherProfiles.getDefaultInstance())
            } else {
                throw exception
            }
        }
    }

    override suspend fun setActiveProfile(profileId: String) {
        dataStore.updateData { profiles ->
            profiles
                .toBuilder()
                .setActive(profileId)
                .build()
        }
    }

    override suspend fun setEditedProfile(profileId: String) {
        dataStore.updateData { profiles ->
            profiles
                .toBuilder()
                .setEdited(profileId)
                .build()
        }
    }

    override suspend fun createLauncherProfile(profile: LauncherProfile) {
        val existing = getLauncherProfiles().firstOrNull()
        if (existing == null || existing.profilesList.isEmpty()) {
            dataStore.updateData {
                launcherProfiles {
                    profiles.add(profile)
                    active = profile.id
                }
            }
        } else {
            if (existing.profilesList.map { it.id }.contains(profile.id)) {
                // In a perfect world we must never create a profile with the same id. If we
                // try we update the existing profile
                updateLauncherProfile(profile)
            } else {
                dataStore.updateData { profiles ->
                    profiles
                        .toBuilder()
                        .clearProfiles()
                        .addAllProfiles((existing.profilesList + listOf(profile)).toMutableList())
                        .build()
                }
            }
        }
    }

    override suspend fun deleteLauncherProfile(profile: LauncherProfile) {
        dataStore.updateData { profiles ->
            val newProfiles = getProfiles().first().filter { it.id != profile.id }.toMutableList()
            profiles
                .toBuilder()
                .clearProfiles()
                .addAllProfiles(newProfiles)
                .build()
        }
    }

    override suspend fun updateLauncherProfile(profile: LauncherProfile) {
        dataStore.updateData { profiles ->
            profiles
                .toBuilder()
                .clearProfiles()
                .addAllProfiles(updateLauncherProfileInList(profile.id) {
                    profile
                })
                .build()
        }
    }

    override suspend fun updateVisibleApps(
        profileId: String,
        visibleApps: List<LauncherProfileApp>
    ) {
        dataStore.updateData { profiles ->
            profiles
                .toBuilder()
                .clearProfiles()
                .addAllProfiles(updateLauncherProfileInList(profileId) {
                    it.toBuilder()
                        .clearLauncherProfileApps()
                        .addAllLauncherProfileApps(visibleApps)
                        .build()
                })
                .build()
        }
    }

    /**
     * In this case we have at least one element in the list and the list must contain the
     * given profile id
     */
    private suspend fun updateLauncherProfileInList(
        profileId: String,
        profileUpdateAction: (LauncherProfile) -> LauncherProfile
    ): List<LauncherProfile> =
        getProfiles().first().toMutableList().also { profiles ->
            profiles.replaceAll { profile ->
                // We update only the given profile id in the list
                if (profileId == profile.id) profileUpdateAction(profile) else profile
            }
        }

}

/**
 * Data migration for LauncherProfiles.
 *
 * This migration handles the transition from storing visible apps as a list of package name strings
 * (`visible_apps`) to a list of `LauncherProfileApp` objects (`launcher_profile_apps`).
 *
 * The `LauncherProfileApp` object allows for more structured data associated with each app,
 * potentially including additional metadata in the future.
 *
 * Migration logic:
 * 1. **`shouldMigrate`**: Checks if any profile in the current data has a non-empty `visible_apps` list.
 *    If so, migration is needed.
 * 2. **`migrate`**:
 *    - Iterates through each `LauncherProfile`.
 *    - If a profile has `visible_apps`:
 *        - Converts each package name string in `visible_apps` into a `LauncherProfileApp` object.
 *        - Creates a new `LauncherProfile` with the `launcher_profile_apps` field populated and
 *          the old `visible_apps` field cleared.
 *    - If a profile does not need migration (i.e., `visible_apps` is empty), it's kept as is.
 *    - Reconstructs the `LauncherProfiles` object with the (potentially) migrated profiles.
 * 3. **`cleanUp`**: No specific cleanup actions are required after this migration.
 */
@Suppress("DEPRECATION")
val MIGRATION_LAUNCHER_PROFILE_APPS = object : DataMigration<LauncherProfiles> {
    /**
     * Determines if the migration should be run.
     * It checks if any profile has data in the old `visibleApps` field.
     */
    override suspend fun shouldMigrate(currentData: LauncherProfiles): Boolean {
        return currentData.profilesList.any { it.visibleAppsCount > 0 }
    }

    /**
     * Performs the data migration.
     * It converts the old `visibleApps` (list of strings) to the new `launcherProfileApps` (list of `LauncherProfileApp` objects).
     */
    override suspend fun migrate(currentData: LauncherProfiles): LauncherProfiles {
        // We start building a new LauncherProfiles object based on the old data.
        return currentData.toBuilder().apply {
            // We iterate through each profile and create a migrated version.
            val migratedProfiles = profilesList.map { profile ->
                // Check if this specific profile needs migration.
                if (profile.visibleAppsCount > 0) {
                    // Convert the old list of strings into the new list of LauncherProfileApp objects.
                    val newAppsList = profile.visibleAppsList.map { packageName ->
                        LauncherProfileApp.newBuilder().setPackageName(packageName).build()
                    }

                    // Build a new profile with the data moved to the new field
                    // and the old field cleared.
                    profile.toBuilder()
                        .clearVisibleApps() // Important: Clear the old field's data.
                        .addAllLauncherProfileApps(newAppsList) // Add all converted items to the new field.
                        .build()
                } else {
                    // If no migration is needed for this profile, return it as is.
                    profile
                }
            }
            // Finally, we clear the old profiles list from our builder and
            // add the newly updated list of profiles.
            clearProfiles()
            addAllProfiles(migratedProfiles)
        }.build()
    }

    /**
     * Called after the migration is complete. No cleanup is needed for this migration.
     */
    override suspend fun cleanUp() = Unit
}
