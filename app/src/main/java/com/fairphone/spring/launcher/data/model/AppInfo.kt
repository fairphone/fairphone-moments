/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.data.model

import android.graphics.drawable.Drawable
import com.fairphone.spring.launcher.data.model.protos.LauncherProfileApp
import com.fairphone.spring.launcher.data.model.protos.launcherProfileApp

data class AppInfo(
    override val name: String,
    val packageName: String,
    val mainActivityClassName: String,
    val userUuid: Int = 0,
    override val icon: Drawable,
    val isWorkApp: Boolean = false
) : SelectableItem {

    override val id: String = packageName

    override fun equals(other: Any?): Boolean {
        return other is AppInfo &&
                other.packageName == packageName &&
                other.isWorkApp == isWorkApp &&
                other.userUuid == userUuid
    }

    override fun hashCode(): Int {
        var result = userUuid
        result = 31 * result + name.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + mainActivityClassName.hashCode()
        result = 31 * result + icon.hashCode()
        return result
    }
}

fun AppInfo.toLauncherProfileApp(): LauncherProfileApp {
    val appInfoPackageName = this.packageName
    val appInfoIsWorkApp = this.isWorkApp

    return launcherProfileApp {
        packageName = appInfoPackageName
        isWorkApp = appInfoIsWorkApp
    }
}

/**
 * Maximum number of apps that can be visible in the launcher.
 */
const val LAUNCHER_MAX_APP_COUNT = 5
