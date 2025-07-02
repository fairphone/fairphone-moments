/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.repository

import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserManager
import android.util.Log
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.data.model.protos.LauncherProfileApp
import com.fairphone.spring.launcher.util.isManagedProfile

interface AppInfoRepository {
    fun getAllInstalledApps(context: Context): List<AppInfo>
    fun getAppInfo(context: Context, packageName: String): AppInfo?
    fun getAppInfosByPackageNames(context: Context, packageNames: List<String>): List<AppInfo>
    fun getAppInfosByProfileApps(context: Context, profileApps: List<LauncherProfileApp>): List<AppInfo>
}

class AppInfoRepositoryImpl : AppInfoRepository {

    override fun getAllInstalledApps(context: Context): List<AppInfo> {
        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

        val density = context.resources.displayMetrics.densityDpi
        return try {
            // Get all user profiles associated with the current user
            userManager.userProfiles.flatMap { profile -> // Iterate through each profile (UserHandle)
                // Get the list of launchable activities for the specific profile
                val activities = launcherApps.getActivityList(null, profile)
                    .sortedBy { it.label.toString().lowercase() }
                // Check if the profile is a work profile
                val isWorkProfile = profile.isManagedProfile(context)
                activities.mapNotNull { activityInfo ->
                    try {
                        AppInfo(
                            name = activityInfo.label.toString(),
                            mainActivityClassName = activityInfo.componentName.className,
                            packageName = activityInfo.componentName.packageName,
                            icon = activityInfo.getIcon(density),
                            userUuid = activityInfo.user.hashCode(),
                            isWorkApp = isWorkProfile
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load info for ${activityInfo.componentName.flattenToString()}", e)
                        null
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException accessing LauncherApps. Check permissions or device policy.", e)
            emptyList() // Return empty on permission errors
        } catch (e: Exception) {
            Log.e(TAG, "Error loading apps using LauncherApps", e)
            emptyList() // Return empty list on other errors
        }
    }

    override fun getAppInfo(context: Context, packageName: String): AppInfo? {
        return getInstalledAppsLauncherApps(context, listOf(packageName)).firstOrNull()
    }

    override fun getAppInfosByPackageNames(context: Context, packageNames: List<String>): List<AppInfo> {
        return getInstalledAppsLauncherApps(context, packageNames)
    }

    private fun getInstalledAppsLauncherApps(
        context: Context,
        packageNames: List<String>
    ): List<AppInfo>  {
        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val density = context.resources.displayMetrics.densityDpi


        return try {
            // TODO: App appears multiple times if installed in multiple profiles (personal + work)
            // Get all user profiles associated with the current user
            userManager.userProfiles.flatMap { profile -> // Iterate through each profile (UserHandle)
                // Get the list of launchable activities for the specific profile
                val activities = packageNames.flatMap { packageName ->
                    launcherApps.getActivityList(packageName, profile)
                }.sortedBy { it.label.toString().lowercase() }

                activities.mapNotNull { activityInfo ->
                    try {
                        AppInfo(
                            name = activityInfo.label.toString(),
                            mainActivityClassName = activityInfo.componentName.className,
                            packageName = activityInfo.componentName.packageName,
                            icon = activityInfo.getIcon(density),
                            userUuid = activityInfo.user.hashCode()
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load info for ${activityInfo.componentName.flattenToString()}", e)
                        null
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException accessing LauncherApps. Check permissions or device policy.", e)
            emptyList() // Return empty on permission errors
        } catch (e: Exception) {
            Log.e(TAG, "Error loading apps using LauncherApps", e)
            emptyList() // Return empty list on other errors
        }
    }

    /**
     * Retrieves a list of [AppInfo] objects based on a list of [LauncherProfileApp] objects.
     * @param context The application context.
     * @param profileApps A list of [LauncherProfileApp] objects specifying the apps to retrieve.
     * @return A list of [AppInfo] objects corresponding to the provided [LauncherProfileApp] objects.
     */
    override fun getAppInfosByProfileApps(
        context: Context,
        profileApps: List<LauncherProfileApp>
    ): List<AppInfo> {
        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val density = context.resources.displayMetrics.densityDpi

        return try {
            // Get all user profiles associated with the current user
            userManager.userProfiles.flatMap { profile -> // Iterate through each profile (UserHandle)
                // Get the list of launchable activities for the specific profile
                val isWorkProfile = profile.isManagedProfile(context)
                val activities = profileApps
                    // Keeps only those LauncherProfileApp objects whose isWorkApp property
                    // matches whether the current profile (UserHandle) being processed is a work profile
                    .filter { it.isWorkApp == isWorkProfile }
                    .flatMap { launcherProfileApp ->
                        launcherApps.getActivityList(launcherProfileApp.packageName, profile)
                    }

                activities.mapNotNull { activityInfo ->
                    try {
                        AppInfo(
                            name = activityInfo.label.toString(),
                            mainActivityClassName = activityInfo.componentName.className,
                            packageName = activityInfo.componentName.packageName,
                            icon = activityInfo.getIcon(density),
                            userUuid = activityInfo.user.hashCode(),
                            isWorkApp = isWorkProfile
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load info for ${activityInfo.componentName.flattenToString()}", e)
                        null
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException accessing LauncherApps. Check permissions or device policy.", e)
            emptyList() // Return empty on permission errors
        } catch (e: Exception) {
            Log.e(TAG, "Error loading apps using LauncherApps", e)
            emptyList() // Return empty list on other errors
        }
    }


    companion object {
        const val TAG = "AppInfoRepository"
    }
}
