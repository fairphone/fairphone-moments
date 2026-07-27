/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.apps.selector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.ui.FP6Preview
import com.fairphone.spring.launcher.ui.FP6PreviewDark
import com.fairphone.spring.launcher.ui.component.selector.ItemSelectorLayout
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import com.fairphone.spring.launcher.util.fakeApp

@Composable
fun VisibleAppSelectorScreen(
    screenState: VisibleAppSelectorScreenState,
    onAppClick: (AppInfo) -> Unit,
    onAppDeselected: (AppInfo) -> Unit,
    onConfirmAppSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (screenState) {
            is VisibleAppSelectorScreenState.Ready -> {
                VisibleAppSelectorScreen(
                    appList = screenState.data.appList,
                    selectedApps = screenState.data.visibleApps,
                    maxAppCount = screenState.data.maxItemCount,
                    showConfirmButton = screenState.data.showConfirmButton,
                    showAppCounter = screenState.data.showAppCounter,
                    showEmptyAppSelectedError = screenState.data.showEmptyAppSelectedError,
                    showMaxAppSelectedError = screenState.data.showMaxAppSelectedError,
                    confirmButtonTextResource = screenState.data.confirmButtonTextResource,
                    onAppClick = onAppClick,
                    onAppDeselected = onAppDeselected,
                    onConfirmAppSelection = onConfirmAppSelection,
                    maxAppCountErrorText = stringResource(
                        R.string.visible_apps_error_max_selected,
                        screenState.data.maxItemCount,
                    ),
                    emptyAppSelectedErrorText = stringResource(
                        R.string.visible_apps_error_empty_selected,
                    ),
                )
            }

            else -> {}
        }
    }
}

@Composable
fun VisibleAppSelectorScreen(
    appList: List<AppInfo>,
    selectedApps: List<AppInfo>,
    maxAppCount: Int,
    showConfirmButton: Boolean,
    showAppCounter: Boolean,
    showEmptyAppSelectedError: Boolean,
    showMaxAppSelectedError: Boolean,
    confirmButtonTextResource: Int,
    onAppClick: (AppInfo) -> Unit,
    onAppDeselected: (AppInfo) -> Unit,
    onConfirmAppSelection: () -> Unit,
    modifier: Modifier = Modifier,
    maxAppCountErrorText: String,
    emptyAppSelectedErrorText: String,
) {
    ItemSelectorLayout(
        modifier = modifier,
        itemList = appList,
        selectedItems = selectedApps,
        maxItemCount = maxAppCount,
        showConfirmButton = showConfirmButton,
        showItemCounter = showAppCounter,
        showEmptyItemSelectedError = showEmptyAppSelectedError,
        showMaxItemSelectedError = showMaxAppSelectedError,
        confirmButtonTextResource = confirmButtonTextResource,
        onItemClick = onAppClick,
        onItemDeselected = onAppDeselected,
        onConfirmItemSelection = onConfirmAppSelection,
        maxItemCountErrorText = maxAppCountErrorText,
        emptyItemSelectedErrorText = emptyAppSelectedErrorText,
        searchBarPlaceholderText = stringResource(R.string.search_app_info_bar_placeholder),
    )
}

@Composable
private fun VisibleAppSelectorScreen_Preview() {
    val context = LocalContext.current
    SpringLauncherTheme {
        VisibleAppSelectorScreen(
            screenState = VisibleAppSelectorScreenState.Ready(
                data = ScreenData(
                    appList = listOf(
                        context.fakeApp("test", isWorkApp = true),
                        context.fakeApp("test1"),
                        context.fakeApp("test2")
                    ),
                    visibleApps = listOf(context.fakeApp("test")),
                    showConfirmButton = true,
                    showAppCounter = false,
                    showEmptyAppSelectedError = false,
                    showMaxAppSelectedError = false,
                    confirmButtonTextResource = R.string.bt_confirm
                )
            ),
            onAppClick = {},
            onAppDeselected = {},
            onConfirmAppSelection = {},
        )
    }
}

@Composable
@FP6Preview()
private fun VisibleAppSelectorScreen_LightPreview() {
    VisibleAppSelectorScreen_Preview()
}

@Composable
@FP6PreviewDark()
private fun VisibleAppSelectorScreen_DarkPreview() {
    VisibleAppSelectorScreen_Preview()
}