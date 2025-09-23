/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.Mock_Profile
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.prefs.UsageMode
import com.fairphone.spring.launcher.ui.FP6Preview
import com.fairphone.spring.launcher.ui.FP6PreviewDark
import com.fairphone.spring.launcher.ui.component.ActionButton
import com.fairphone.spring.launcher.ui.component.ButtonSize
import com.fairphone.spring.launcher.ui.icons.mode.ModeIcon
import com.fairphone.spring.launcher.ui.icons.nav.NavIcon
import com.fairphone.spring.launcher.ui.theme.FairphoneTypography
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import com.fairphone.spring.launcher.ui.theme.homeButtonBackgroundDarkColor
import com.fairphone.spring.launcher.ui.theme.homeButtonBackgroundLightColor
import com.fairphone.spring.launcher.util.gradientBorder

@Composable
fun ColumnScope.CurrentModeButton(
    modifier: Modifier = Modifier,
    activeProfile: LauncherProfile,
    appUsageMode: UsageMode,
    onModeSwitcherButtonClick: () -> Unit,
    onTooltipClick: () -> Unit
) {
    CurrentModeButtonTooltip(
        modifier = modifier.align(Alignment.CenterHorizontally),
        appUsageMode = appUsageMode,
        onTooltipClick = onTooltipClick
    ) { modifier ->

        val surfaceColor = MaterialTheme.colorScheme.surface
        val gradientStroke = Brush.verticalGradient(
            colors = listOf(surfaceColor.copy(alpha = 2f), surfaceColor.copy(alpha = 0.1f))
        )
        val borderWidth = 1.dp
        val cornerRadius = 12.dp

        Button(
            onClick = onModeSwitcherButtonClick,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = RoundedCornerShape(size = cornerRadius),
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
            modifier = modifier.gradientBorder(
                borderWidth = borderWidth,
                brush = gradientStroke,
                cornerRadius = cornerRadius
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = ModeIcon.fromString(activeProfile.icon).imageVector,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = activeProfile.name,
                        style = FairphoneTypography.ButtonDefault,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentModeButtonTooltip(
    modifier: Modifier = Modifier,
    appUsageMode: UsageMode,
    onTooltipClick: () -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    val dynamicText = when (appUsageMode) {
        UsageMode.ON_BOARDING -> stringResource(R.string.onboarding_home_button_help)
        UsageMode.ON_BOARDING_COMPLETE -> stringResource(R.string.onboarding_home_tooltitp)
        UsageMode.DEFAULT -> stringResource(R.string.onboarding_home_button_help)
    }
    val displayed =
        appUsageMode == UsageMode.ON_BOARDING || appUsageMode == UsageMode.ON_BOARDING_COMPLETE

    val tooltipState = rememberTooltipState(initialIsVisible = displayed, isPersistent = true)
    val anchorBounds: MutableState<LayoutCoordinates?> = remember { mutableStateOf(null) }
    val density = LocalDensity.current
    val isDark = isSystemInDarkTheme()

    TooltipBox(
        positionProvider = rememberBottomPositionProvider(density),
        tooltip = {
            RichTooltip(
                // We need to manage the caret because we can't customize the material
                // tooltip position
                modifier = Modifier
                    .padding(horizontal = 55.dp)
                    .drawBehind(drawCaret(anchorBounds, density, isDark)),

                colors = TooltipDefaults.richTooltipColors().copy(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    Text(
                        text = dynamicText,
                        modifier = Modifier.weight(4.0f),
                        textAlign = TextAlign.Center,
                    )
                    ActionButton(
                        icon = NavIcon.Close.imageVector,
                        size = ButtonSize.Small,
                        modifier = Modifier.weight(1.0f),
                        horizontalAlignment = Alignment.End,
                        onClick = {
                            tooltipState.dismiss()
                            onTooltipClick()
                        }
                    )
                }
            }
        },
        state = tooltipState,
        content = {
            content(modifier.onGloballyPositioned { coordinates ->
                anchorBounds.value = coordinates
            })
        }

    )
}

/**
 * We need to manage the caret because we can't customize the material tooltip position. This
 * function draw it on the top of the tooltip box
 */
fun drawCaret(
    anchorBounds: MutableState<LayoutCoordinates?>,
    density: Density,
    isDark: Boolean
): DrawScope.() -> Unit = {
    val (caretWidthPx, caretHeightPx) = with(density) {
        8.dp.roundToPx() to 8.dp.roundToPx()
    }
    // The x position depends on the anchor coordinate and the caret width
    val caretPositionX = (anchorBounds.value?.size?.width?.toFloat() ?: 0f) - caretWidthPx
    // We want to draw the caret on the top
    val caretPositionY = 0.0f
    if (caretPositionX >= 0f) {
        Path()
            .apply {
                moveTo(x = caretPositionX, y = caretPositionY)
                lineTo(x = caretPositionX + caretHeightPx, y = caretPositionY)
                lineTo(x = caretPositionX, y = caretPositionY - caretHeightPx)
                lineTo(x = caretPositionX - caretHeightPx, y = caretPositionY)
                lineTo(x = caretPositionX, y = caretPositionY)
            }.also { path ->
                drawPath(
                    path = path,
                    color = if (isDark) homeButtonBackgroundDarkColor else homeButtonBackgroundLightColor,
                )
                path.close()
            }
    }
}

/**
 * As the default position calculator try to display the tooltip on the top of the button we
 * use our own [PopupPositionProvider]
 */
@Composable
fun rememberBottomPositionProvider(density: Density): PopupPositionProvider {
    val tooltipAnchorSpacing = with(density) { 20.dp.roundToPx() }
    return remember(tooltipAnchorSpacing) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset =
                IntOffset(
                    // We want to center the popup
                    x = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2,
                    // We place it below the anchor
                    y = anchorBounds.bottom + tooltipAnchorSpacing
                )
        }
    }
}

@Composable
private fun CurrentModeButton_Preview(usageMode: UsageMode) {
    SpringLauncherTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CurrentModeButton(
                activeProfile = Mock_Profile,
                appUsageMode = usageMode,
                onModeSwitcherButtonClick = {},
                onTooltipClick = {}
            )

        }
    }
}

@Composable
@FP6Preview
@FP6PreviewDark
private fun CurrentModeButton_DefaultLightPreview() {
    CurrentModeButton_Preview(UsageMode.DEFAULT)
}

@Composable
@FP6Preview
@FP6PreviewDark
private fun CurrentModeButton_OnBoardingLightPreview() {
    CurrentModeButton_Preview(UsageMode.ON_BOARDING)
}

@Composable
@FP6Preview
@FP6PreviewDark
private fun CurrentModeButton_OnBoardingCompleteLightPreview() {
    CurrentModeButton_Preview(UsageMode.ON_BOARDING_COMPLETE)
}
