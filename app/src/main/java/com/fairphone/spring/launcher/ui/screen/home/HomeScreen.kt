/*
 * Copyright (C) 2025 FairPhone B.V.
 *
 * SPDX-FileCopyrightText: 2025. FairPhone B.V.
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package com.fairphone.spring.launcher.ui.screen.home

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fairphone.spring.launcher.R
import com.fairphone.spring.launcher.data.model.AppInfo
import com.fairphone.spring.launcher.data.model.Mock_Profile
import com.fairphone.spring.launcher.data.model.protos.LauncherProfile
import com.fairphone.spring.launcher.data.prefs.UsageMode
import com.fairphone.spring.launcher.ui.FP6Preview
import com.fairphone.spring.launcher.ui.FP6PreviewDark
import com.fairphone.spring.launcher.ui.component.FairphoneMomentsDemoCard
import com.fairphone.spring.launcher.ui.component.WorkAppBadge
import com.fairphone.spring.launcher.ui.screen.home.component.CurrentModeButton
import com.fairphone.spring.launcher.ui.screen.home.component.MediaControlCard
import com.fairphone.spring.launcher.util.MediaState
import com.fairphone.spring.launcher.ui.theme.FairphoneTypography
import com.fairphone.spring.launcher.ui.theme.SpringLauncherTheme
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

const val CLOCK_TIME_FORMAT = "HH:mm"
const val CLOCK_DATE_FORMAT = "EEE, dd LLL"

private const val CONTENT_FADE_IN_DURATION = 420 // Duration of the text animation

@Composable
fun HomeScreen(
    isContentVisible: Boolean,
    onModeSwitcherButtonClick: () -> Unit,
    onDemoCardClick: () -> Unit,
    onTimeClick: () -> Unit,
    viewModel: HomeScreenViewModel = koinViewModel(),
    mediaViewModel: MediaControlViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val dateTime by viewModel.dateTime.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val mediaState by mediaViewModel.mediaState.collectAsStateWithLifecycle()

    val (date, time) = remember(dateTime) {
        dateTime.format(DateTimeFormatter.ofPattern(CLOCK_DATE_FORMAT)) to
                dateTime.format(DateTimeFormatter.ofPattern(CLOCK_TIME_FORMAT))
    }

    screenState ?: return

    HomeScreen(
        isContentVisible = isContentVisible,
        date = date,
        time = time,
        appUsageMode = screenState!!.appUsageMode,
        activeProfile = screenState!!.activeProfile,
        appList = screenState!!.visibleApps,
        isRetailDemoMode = screenState!!.isRetailDemoMode,
        onAppClick = { appInfo ->
            viewModel.finishOnBoarding()
            viewModel.onAppClick(context, appInfo)
        },
        onModeSwitcherButtonClick = {
            viewModel.finishOnBoarding()
            onModeSwitcherButtonClick()
        },
        onDemoCardClick = onDemoCardClick,
        onTooltipClick = {
            viewModel.finishOnBoarding()
        },
        onTimeClick = onTimeClick,
        mediaState = mediaState,
        onMediaPlayPause = mediaViewModel::playPause,
        onMediaNext = mediaViewModel::next,
        onMediaPrevious = mediaViewModel::previous,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    isContentVisible: Boolean,
    date: String,
    time: String,
    appUsageMode: UsageMode,
    activeProfile: LauncherProfile,
    appList: List<AppInfo>,
    isRetailDemoMode: Boolean,
    onAppClick: (AppInfo) -> Unit,
    onModeSwitcherButtonClick: () -> Unit,
    onDemoCardClick: () -> Unit,
    onTooltipClick: () -> Unit,
    onTimeClick: () -> Unit,
    mediaState: MediaState? = null,
    onMediaPlayPause: () -> Unit = {},
    onMediaNext: () -> Unit = {},
    onMediaPrevious: () -> Unit = {},
) {

    val fadeInAnimation = remember {
        fadeIn(animationSpec = tween(CONTENT_FADE_IN_DURATION))
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = isContentVisible,
        enter = fadeInAnimation,
        exit = ExitTransition.None
    )
    {
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
            ) {
                Text(
                    text = time,
                    style = FairphoneTypography.Time,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable(
                        interactionSource = null,
                        indication = null,
                        onClick = onTimeClick,
                    )
                )
                Text(
                    text = date,
                    style = FairphoneTypography.Date,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                CurrentModeButton(
                    modifier = Modifier.padding(top = 12.dp),
                    activeProfile = activeProfile,
                    appUsageMode = appUsageMode,
                    onModeSwitcherButtonClick = onModeSwitcherButtonClick,
                    onTooltipClick = onTooltipClick
                )

                mediaState?.takeIf { !activeProfile.mediaControlsDisabled }?.let { media ->
                    MediaControlCard(
                        state = media,
                        onPlayPause = onMediaPlayPause,
                        onNext = onMediaNext,
                        onPrevious = onMediaPrevious,
                        modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AppList(
                appList = appList,
                onAppClick = onAppClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )

            if (isRetailDemoMode) {
                FairphoneMomentsDemoCard(
                    modifier = Modifier.padding(start = 30.dp, end = 30.dp, bottom = 30.dp),
                    onClick = { onDemoCardClick() }
                )
            }
        }
    }
}

@Composable
fun AppList(
    appList: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        appList.forEach { app ->
            LauncherAppButton(
                appName = app.name,
                onAppClick = { onAppClick(app) },
                isWorkApp = app.isWorkApp,
            )
        }
    }
}

@Composable
fun LauncherAppButton(
    appName: String,
    onAppClick: () -> Unit,
    isWorkApp: Boolean = false,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onAppClick
            )
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = appName,
            style = FairphoneTypography.AppButtonDefault,
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (isWorkApp) {
            WorkAppBadge(modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun LauncherAppButton_Preview() {
    SpringLauncherTheme {
        Column {
            LauncherAppButton(
                appName = "App name",
                onAppClick = {}
            )
            LauncherAppButton(
                appName = "App name",
                onAppClick = {},
                isWorkApp = true,
            )
        }

    }
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
fun LauncherAppButton_PreviewLight() {
    LauncherAppButton_Preview()
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun LauncherAppButton_PreviewDark() {
    LauncherAppButton_Preview()
}

@Composable
fun HomeScreen_Preview() {
    SpringLauncherTheme {
        HomeScreen(
            isContentVisible = true,
            date = "Wed, 13 Feb",
            time = "12:30",
            appUsageMode = UsageMode.DEFAULT,
            activeProfile = Mock_Profile,
            appList = previewAppList(LocalContext.current),
            isRetailDemoMode = false,
            onAppClick = {},
            onModeSwitcherButtonClick = {},
            onDemoCardClick = {},
            onTooltipClick = {},
            onTimeClick = {},
        )
    }
}

@Composable
@FP6Preview
fun HomeScreen_PreviewLight() {
    HomeScreen_Preview()
}

@Composable
@FP6PreviewDark
fun HomeScreen_PreviewDark() {
    HomeScreen_Preview()
}

fun previewAppList(context: Context) = listOf(
    AppInfo(
        name = "Phone",
        packageName = "com.package.app1",
        mainActivityClassName = "",
        icon = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)!!,
    ),
    AppInfo(
        name = "Chrome",
        packageName = "com.package.app2",
        mainActivityClassName = "",
        icon = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)!!,
    ),
    AppInfo(
        name = "Messages",
        packageName = "com.package.app3",
        mainActivityClassName = "",
        icon = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)!!,
    ),
    AppInfo(
        name = "Camera",
        packageName = "com.package.app4",
        mainActivityClassName = "",
        icon = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)!!,
    ),
    AppInfo(
        name = "Maps",
        packageName = "com.package.app5",
        mainActivityClassName = "",
        icon = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)!!,
    ),
)