/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.settings.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.ui.component.AppInfoListItem
import com.fairphone.spring.launcher.ui.state.DragAndDropListState
import com.fairphone.spring.launcher.ui.theme.FairphoneTypography
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import com.fairphone.spring.launcher.util.fakeApp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun VisibleAppSettingsScreen(
    screenState: VisibleAppSettingsScreenState,
    onChangeAppsClick: () -> Unit,
    onChangeAppOrder: (Int, Int) -> Unit
) {
    when (screenState) {
        is VisibleAppSettingsScreenState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize()
            )
        }

        is VisibleAppSettingsScreenState.Ready -> {
            VisibleAppSettingsScreen(
                visibleApps = screenState.visibleApps,
                onChangeAppsClick = onChangeAppsClick,
                onChangeAppOrder = onChangeAppOrder
            )
        }
    }
}

@Composable
fun VisibleAppSettingsScreen(
    visibleApps: List<AppInfo>,
    onChangeAppsClick: () -> Unit,
    onChangeAppOrder: (Int, Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
//        Text(
//            text = stringResource(R.string.setting_header_visible_apps),
//            style = FairphoneTypography.BodySmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.padding(start = 36.dp, end = 36.dp, top = 24.dp)
//        )
        Spacer(modifier = Modifier.size(16.dp))

        val coroutineScope = rememberCoroutineScope()
        var overscrollJob by remember { mutableStateOf<Job?>(null) }
        val lazyListState = rememberLazyListState()

        val dragAndDropListState = remember {
            DragAndDropListState(lazyListState, onChangeAppOrder)
        }
        val haptic = LocalHapticFeedback.current

        LazyColumn (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDrag = { change, offset ->
                            change.consume()
                            dragAndDropListState.onDrag(offset)

                            if (overscrollJob?.isActive == true) return@detectDragGesturesAfterLongPress

                            dragAndDropListState
                                .checkOverscroll()
                                .takeIf { it != 0f }
                                ?.let {
                                    overscrollJob = coroutineScope.launch {
                                        dragAndDropListState.lazyListState.scrollBy(it)
                                    }
                                } ?: kotlin.run { overscrollJob?.cancel() }
                        },
                        onDragStart = { offset ->
                            dragAndDropListState.onDragStart(offset)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onDragEnd = {
                            dragAndDropListState.onDragFinish()
                        },
                        onDragCancel = {
                            dragAndDropListState.onDragFinish()
                        }
                    )
                },
            state = dragAndDropListState.lazyListState
        ) {
            items(
                count = visibleApps.size,
                contentType = { index -> visibleApps[index] },
            ) { index ->
                val appInfo = visibleApps[index]
                AppInfoListItem(
                    icon = appInfo.icon,
                    name = appInfo.name,
                    isWorkApp = appInfo.isWorkApp,
                    modifier = Modifier.composed {
                        val offsetOrNull =
                            dragAndDropListState.elementDisplacement.takeIf {
                                index == dragAndDropListState.currentIndexOfDraggedItem
                            }
                        Modifier.graphicsLayer {
                            translationY = offsetOrNull ?: 0f
                        }
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(size = 100.dp)
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(size = 100.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable {
                    onChangeAppsClick()
                }
        ) {
            Text(
                text = stringResource(R.string.bt_change_apps),
                style = FairphoneTypography.LabelMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
@Preview
fun VisibleAppsSettingsScreen_Preview() {
    val context = LocalContext.current
    SpringLauncherTheme {
        VisibleAppSettingsScreen(
            visibleApps = listOf(
                context.fakeApp("app 1"),
                context.fakeApp("app 2"),
                context.fakeApp("app 3"),
                context.fakeApp("app 4")
            ),
            onChangeAppsClick = {},
            onChangeAppOrder = { _, _ -> }
        )
    }
}