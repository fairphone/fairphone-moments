/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.util

import android.app.UiModeManager
import android.app.WallpaperManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.UserHandle
import android.os.UserManager
import android.provider.MediaStore
import android.provider.Telephony
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.data.repository.AppInfoRepositoryImpl.Companion.TAG

const val RETAIL_DEMO_APP_PACKAGE_NAME = "com.fairphone.retaildemo2"

/**
 * @return the package name of the default browser app.
 */
fun Context.getDefaultBrowserPackageName(fallback: String): String {
    val intent = Intent(Intent.ACTION_VIEW, "https://example.com".toUri())
    intent.addCategory(Intent.CATEGORY_BROWSABLE) // Specify that it's for browsing
    val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

    return resolveInfo?.activityInfo?.packageName ?: fallback
}

/**
 * @return the package name of the default phone/dialer app.
 */
fun Context.getDefaultPhonePackageName(fallback: String): String {
    val telecomManager = getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
    val defaultDialer = telecomManager?.defaultDialerPackage

    return defaultDialer ?: fallback
}

/**
 * @return the package name of the default messaging app.
 */
fun Context.getDefaultMessagingPackageName(fallback: String): String {
    return Telephony.Sms.getDefaultSmsPackage(this) ?: fallback
}

/**
 * @return the package name of the default navigation app.
 */
fun Context.getDefaultNavigationPackageName(fallback: String): String {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = "geo:0,0".toUri()
    }
    val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

    return resolveInfo?.activityInfo?.packageName ?: fallback
}

/**
 * @return the package name of the default camera app.
 */
fun Context.getDefaultCameraPackageName(fallback: String): String {
    val stillImageIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
    val stillInfo = packageManager.resolveActivity(stillImageIntent, PackageManager.MATCH_DEFAULT_ONLY)
    val resolvedPackageName = stillInfo?.activityInfo?.packageName

    if (resolvedPackageName != null && resolvedPackageName != "android" && !resolvedPackageName.contains("Resolver")) {
        return resolvedPackageName
    }

    val flags = PackageManager.MATCH_DEFAULT_ONLY
    val cameraApps = packageManager.queryIntentActivities(stillImageIntent, flags)

    if (cameraApps.isEmpty()) {
        return fallback
    }

    for (app in cameraApps) {
        val appInfo = app.activityInfo.applicationInfo
        // Check if it is a system app
        if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
            return app.activityInfo.packageName
        }
    }

    return cameraApps.firstOrNull()?.activityInfo?.packageName
        ?: fallback
}

/**
 * Launches an app.
 */
fun Context.launchApp(app: AppInfo) {
    val primaryUserHandle = android.os.Process.myUserHandle()
    val userHandle = getUserHandleFromId(app.userUuid) ?: primaryUserHandle
    val launcher = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val activityInfo = launcher.getActivityList(app.packageName, userHandle)
    val component = if (app.mainActivityClassName.isBlank()) {
        // activityClassName will be null for hidden apps.
        when (activityInfo.size) {
            0 -> {
                //context.showToast(context.getString(R.string.app_not_found))
                return
            }

            1 -> ComponentName(app.packageName, activityInfo[0].name)
            else -> ComponentName(packageName, activityInfo[activityInfo.size - 1].name)
        }
    } else {
        ComponentName(app.packageName, app.mainActivityClassName)
    }

    try {
        launcher.startMainActivity(component, userHandle, null, null)
    } catch (e: SecurityException) {
        Log.e(javaClass.name, e.message, e)
        try {
            launcher.startMainActivity(component, android.os.Process.myUserHandle(), null, null)
        } catch (e: Exception) {
            Log.e(javaClass.name, e.message, e)
            //context.showToast(appContext.getString(R.string.unable_to_open_app))
        }
    } catch (e: Exception) {
        Log.e(javaClass.name, e.message, e)
        //appContext.showToast(appContext.getString(R.string.unable_to_open_app))
    }
}

/**
 * The package name of the Clock app.
 */
const val PACKAGE_NAME_CLOCK_APP = "com.google.android.deskclock"

/**
 * Starts the Clock app.
 */
fun Context.launchClockApp() {
    try {
        startActivity(Intent(Intent.ACTION_MAIN).apply {
            setPackage(PACKAGE_NAME_CLOCK_APP)
            addCategory(Intent.CATEGORY_LAUNCHER)
        })
    } catch (e: Exception) {
        Log.e(javaClass.name, e.message, e)
    }
}

/**
 * Loads the app infos for the given package names.
 */
fun Context.loadAppInfosByPackageNames(
    packageNames: List<String>?
): List<AppInfo> {
    val userManager = getSystemService(Context.USER_SERVICE) as UserManager
    val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val density = resources.displayMetrics.densityDpi

    return try {
        // TODO: App appears multiple times if installed in multiple profiles (personal + work)
        // Get all user profiles associated with the current user
        userManager.userProfiles.flatMap { profile -> // Iterate through each profile (UserHandle)
            // Get the list of launchable activities for the specific profile
            val activities = packageNames?.flatMap { packageName ->
                launcherApps.getActivityList(packageName, profile)
            } ?: launcherApps.getActivityList(null, profile)
                .sortedBy { it.label.toString().lowercase() }

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
                    Log.w(
                        TAG,
                        "Failed to load info for ${activityInfo.componentName.flattenToString()}",
                        e
                    )
                    null
                }
            }
        }
    } catch (e: SecurityException) {
        Log.e(
            TAG,
            "SecurityException accessing LauncherApps. Check permissions or device policy.",
            e
        )
        emptyList() // Return empty on permission errors
    } catch (e: Exception) {
        Log.e(TAG, "Error loading apps using LauncherApps", e)
        emptyList() // Return empty list on other errors
    }
}

fun Context.isDarkModeEnabled(): Boolean {
    return uiModeManager().nightMode == UiModeManager.MODE_NIGHT_YES
}

fun Context.isDoNotDisturbAccessGranted(): Boolean {
    return notificationManager().isNotificationPolicyAccessGranted
}

fun Context.isNotificationAccessGranted(): Boolean {
    return NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
}

/**
 * Gets the user handle from the user ID.
 */
private fun Context.getUserHandleFromId(userId: Int): UserHandle? {
    val userManager = getSystemService(Context.USER_SERVICE) as UserManager
    val userProfiles = userManager.userProfiles
    return userProfiles.find { it.hashCode() == userId }
}

fun Context.notificationManager() =
    getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

fun Context.uiModeManager() = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
fun Context.wallpaperManager() = getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
fun Context.audioManager() = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager

fun Context.hasInternetConnection(): Boolean {
    val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val networkCapabilities =
        connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun Context.isDeviceInRetailDemoMode(): Boolean {
    val dpc = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    return dpc.isDeviceOwnerApp(RETAIL_DEMO_APP_PACKAGE_NAME)
}

/**
 * Checks if the current application is the default launcher.
 */
fun Context.isAppDefaultLauncher(): Boolean {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    return resolveInfo?.activityInfo?.packageName == packageName
}


fun Context.fakeApp(name: String, isWorkApp: Boolean = false): AppInfo =
    AppInfo(
        name = name,
        packageName = name,
        mainActivityClassName = name,
        icon = ContextCompat.getDrawable(this, R.drawable.ic_launcher_foreground)!!,
        isWorkApp = isWorkApp
    )