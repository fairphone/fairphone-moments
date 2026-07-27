/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.ui.FP6Preview
import com.fairphone.spring.launcher.ui.FP6PreviewDark
import com.fairphone.spring.launcher.ui.component.switcher.ItemSwitcherLayout
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import com.fairphone.spring.launcher.util.fakeApp

@Composable
fun AllowedAppsScreen(
    screenState: AllowedNotificationsAppsScreenState,
    onAllowAppSwitchClick: (String, Boolean) -> Unit
) {
    when (screenState) {
        is AllowedNotificationsAppsScreenState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize()
            )
        }

        is AllowedNotificationsAppsScreenState.Success -> {
            AllowedAppsScreen(
                screenData = screenState.data,
                onAllowAppSwitchClick = onAllowAppSwitchClick
            )
        }
    }
}

@Composable
fun AllowedAppsScreen(
    screenData: AllowedNotificationsAppsScreenData,
    onAllowAppSwitchClick: (String, Boolean) -> Unit
) {
    val selectedItems = remember { screenData.allowedNotificationApps.map { it.id } }

    ItemSwitcherLayout(
        itemList = screenData.allNotificationApps,
        isSelected = { id -> id in selectedItems },
        onItemClick = { app, value ->
            onAllowAppSwitchClick(app.id, value)
        },
        searchBarPlaceholderText = stringResource(R.string.search_app_info_bar_placeholder),
    )
}


@Composable
private fun AllowedNotificationsAppsScreen_Preview() {
    val context = LocalContext.current
    SpringLauncherTheme {
        AllowedAppsScreen(
            screenState = AllowedNotificationsAppsScreenState.Success(
                AllowedNotificationsAppsScreenData(
                    profileId = "test",
                    allNotificationApps = listOf(
                        context.fakeApp("test"),
                        context.fakeApp("test1"),
                        context.fakeApp("test2")
                    ),
                    allowedNotificationApps = listOf(
                        context.fakeApp("test")
                    ),
                    repeatCallEnabled = true,
                )
            ),
            onAllowAppSwitchClick = { app, value -> },
        )
    }
}

@Composable
@FP6Preview()
private fun AllowedNotificationsAppsScreen_LightPreview() {
    AllowedNotificationsAppsScreen_Preview()
}

@Composable
@FP6PreviewDark()
private fun AllowedNotificationsAppsScreen_DarkPreview() {
    AllowedNotificationsAppsScreen_Preview()
}