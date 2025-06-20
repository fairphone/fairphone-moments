/*
 * Copyright (C) 2026 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.component

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.ExperimentalAnimationSpecApi
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fairphone.spring.launcher.data.model.LauncherColors
import com.fairphone.spring.launcher.data.model.Preset
import com.fairphone.spring.launcher.ui.FP6Preview
import com.fairphone.spring.launcher.util.areStaticColors
import kotlinx.coroutines.delay

/**
 * FP6 specs:
 *
 * width: 1116 px
 * height: 2484 px
 *
 * pixel per dp = 3
 *
 * width: 372.0.dp dp
 * height: 828.0.dp dp
 *
 * General Information
 * Frame Rate: 120 fps
 * Conversion: 1 second = 120 frames
 * Standard transition duration: 0.133s (16 frames)
 * For elements with gradual position changes, timing varies as per keyframes.
 */

enum class AnimationState {
    START, END
}

@OptIn(ExperimentalAnimationSpecApi::class)
@Composable
fun LauncherBackground(
    modifier: Modifier = Modifier,
    colors: LauncherColors,
    content: @Composable BoxScope.() -> Unit = {},
) {
    if (colors.areStaticColors()) {
        StaticBackground(
            modifier = modifier,
            colors = colors,
            content = content,
        )
    } else {
        AnimatedBackground(
            modifier = modifier,
            colors = colors,
            content = content,
        )
    }
}

@OptIn(ExperimentalAnimationSpecApi::class)
@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    colors: LauncherColors,
    content: @Composable BoxScope.() -> Unit,
) {
    var animationState by remember { mutableStateOf(AnimationState.START) }
    val transition = updateTransition(targetState = animationState, label = "bg anim")
    LaunchedEffect(transition) {
        delay(100)
        animationState = AnimationState.END
    }

    val correctionX = 186.dp
    val correctionY = 500.dp

    Box(modifier = modifier.fillMaxSize()) {
        val offsetAnimationSpec = remember {
            tween<Dp>(
                durationMillis = 800,
                easing = EaseOutCubic,
            )
        }
        val rotationAnimationSpec = remember {
            tween<Float>(
                delayMillis = 400,
                durationMillis = 400,
                easing = EaseOutCubic,
            )
        }

        val leftBlobRotation by transition.animateFloat(
            targetValueByState = { state ->
                when (state) {
                    AnimationState.START -> 0f
                    AnimationState.END -> 0f
                }
            },
            transitionSpec = {
                rotationAnimationSpec

//                keyframesWithSpline {
//                    delayMillis = 400
//                    durationMillis = 400
//                    leftBLobStartRotation at 0
//                    // TODO: Add intermediate steps?
//                    leftBLobEndRotation atFraction 1f
//                }
            }
        )
        val rightBlobRotation by transition.animateFloat(
            targetValueByState = { state ->
                when (state) {
                    AnimationState.START -> 0f
                    AnimationState.END -> 0f
                }
            },
            transitionSpec = {
                rotationAnimationSpec

//                keyframesWithSpline {
//                    delayMillis = 400
//                    durationMillis = 400
//                    rightBLobStartRotation at 0
//                    // TODO: Add intermediate steps?
//                    rightBLobEndRotation atFraction 1f
//                }
            }
        )

        val leftBlobXDp by transition.animateDp(
            targetValueByState = { state ->
                when (state) {
                    AnimationState.START -> -250.dp - correctionX
                    AnimationState.END -> -70.dp - correctionX
                }
            },
            transitionSpec = {
                offsetAnimationSpec
            }
        )
        val leftBlobYDp by transition.animateDp(
            targetValueByState = { state ->
                when (state) {
                    AnimationState.START -> 826.dp - correctionY
                    AnimationState.END -> 826.dp - correctionY
                }
            },
            transitionSpec = {
                offsetAnimationSpec
            }
        )
        val rightBlobXDp by transition.animateDp(
            targetValueByState = { state ->
                when (state) {
                    AnimationState.START -> 630.dp - correctionX
                    AnimationState.END -> 450.dp - correctionX
                }
            },
            transitionSpec = {
                offsetAnimationSpec
            }
        )
        val rightBlobYDp by transition.animateDp(
            targetValueByState = { state ->
                when (state) {
                    AnimationState.START -> 828.dp - correctionY
                    AnimationState.END -> 828.dp - correctionY
                }
            },
            transitionSpec = {
                offsetAnimationSpec
            }
        )

        BackgroundBlob(
            color = Color(colors.leftColor),
            modifier = Modifier
                .offset(x = leftBlobXDp, y = leftBlobYDp)
                .rotate(leftBlobRotation)
        )

        BackgroundBlob(
            color = Color(colors.rightColor),
            modifier = Modifier
                .offset(x = rightBlobXDp, y = rightBlobYDp)
                .rotate(rightBlobRotation)
        )

        content()
    }
}

@Composable
fun BackgroundBlob(
    color: Color,
    modifier: Modifier = Modifier
) {
    val dynamicBlur = if (isSystemInDarkTheme()) 200.dp else 100.dp
    val dynamicAlpha = if (isSystemInDarkTheme()) 0.7f else 1f
    Surface(
        shape = Blob(),
        color = color,
        modifier = modifier
            .blur(radius = dynamicBlur, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            .width(392.dp)
            .height(676.dp)
            .alpha(dynamicAlpha)
    ) {}
}

@Composable
fun StaticBackground(
    colors: LauncherColors,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val overlayGradient = Brush.verticalGradient(
        colors = listOf(
            Color(colors.leftColor),
            Color(colors.rightColor)
        )
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = overlayGradient)
    ) {
        content()
    }
}

@Composable
@FP6Preview
fun AnimatedBackground_Preview() {
    LauncherBackground(
        colors = Preset.Custom.colors,
    )
}
