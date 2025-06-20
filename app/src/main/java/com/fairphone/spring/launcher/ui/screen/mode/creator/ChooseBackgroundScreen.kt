/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.mode.creator

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.LauncherColors
import com.fairphone.spring.launcher.data.model.Mock_Profile
import com.fairphone.spring.launcher.ui.FP6Preview
import com.fairphone.spring.launcher.ui.FP6PreviewDark
import com.fairphone.spring.launcher.ui.component.PrimaryButton
import com.fairphone.spring.launcher.ui.component.ScreenHeader
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import com.fairphone.spring.launcher.ui.theme.backgroundShapeBorderDarkStart
import com.fairphone.spring.launcher.ui.theme.backgroundShapeBorderLightStart
import kotlin.math.roundToInt

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ChooseBackgroundScreen(
    selectedColor: Long,
    continueButtonName: String = stringResource(R.string.bt_create_moment),
    onBackgroundColorSelected: (LauncherColors) -> Unit
) {
    val colors = LauncherColors.All
    val colorSize = colors.size

    val preSelected = colors.indexOfFirst { selectedColor == it.rightColor }

    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (preSelected == -1) 0 else preSelected
    )

    var selectedColorIndex = remember {
        derivedStateOf { scrollState.firstVisibleItemIndex }
    }
    var scrollToIndex by remember { mutableIntStateOf(selectedColorIndex.value) }
    val screenWidth =
        with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

    // This listener is used to change the scrollIndex when the user scroll to the right or the left
    val scrollListener = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.SideEffect) {
                    val minJump = if (consumed.x < 0) -1 else 1
                    // We compute the jump depending on the velocity
                    val jumpSize = (consumed.x / 2 / screenWidth).roundToInt()
                    // We always have a jump
                    val newIndex = scrollToIndex - if (jumpSize == 0) minJump else jumpSize
                    // The new index depends on the color size. We have to stay in bounds
                    scrollToIndex =
                        if (newIndex >= colorSize) colorSize - 1 else if (newIndex < 0) 0 else newIndex
                }
                return super.onPostScroll(consumed, available, source)
            }
        }
    }

    LaunchedEffect(scrollToIndex) {
        scrollState.animateScrollToItem(scrollToIndex)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenHeader(
                title = stringResource(R.string.choose_app_background_header)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                state = scrollState,
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(scrollListener),
                contentPadding = PaddingValues(horizontal = 80.dp)
            ) {
                items(
                    count = colors.size,
                    contentType = { index -> colors[index] }
                ) { index ->
                    val launcherColors = colors[index]
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(32.dp))
                            .border(
                                width = 12.dp,
                                color = if (isSystemInDarkTheme()) backgroundShapeBorderDarkStart else backgroundShapeBorderLightStart,
                                shape = RoundedCornerShape(32.dp)
                            )
                            .width(218.dp)
                            .height(446.dp)
                    ) {
                        ChooseBackgroundExample(
                            launcherColors = launcherColors,
                            modifier = Modifier.fillMaxSize(),
                            onClick = {
                                scrollToIndex = index
                            }
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                colors.forEachIndexed { i, color ->
                    val isSelected = (i == selectedColorIndex.value)
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(size = 33.dp)
                            )
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else Color(0x4DFFFFFF))

                    )
                }
            }
        }

        PrimaryButton(
            text = continueButtonName,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(80.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            onBackgroundColorSelected(colors[selectedColorIndex.value])
        }
    }
}


@Composable
private fun ChooseBackgroundScreen_Preview() {
    SpringLauncherTheme {
        ChooseBackgroundScreen(
            Mock_Profile.bgColor2,
            onBackgroundColorSelected = { }
        )
    }
}

@Composable
@FP6Preview()
private fun ChooseBackgroundScreen_LightPreview() {
    ChooseBackgroundScreen_Preview()
}

@Composable
@FP6PreviewDark()
private fun ModeSwitcherScreen_DarkPreview() {
    ChooseBackgroundScreen_Preview()
}


