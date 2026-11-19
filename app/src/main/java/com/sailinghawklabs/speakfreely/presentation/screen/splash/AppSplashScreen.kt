package com.sailinghawklabs.speakfreely.presentation.screen.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.sailinghawklabs.speakfreely.R
import com.sailinghawklabs.speakfreely.presentation.screen.util.AudioPermissionHandler
import com.sailinghawklabs.speakfreely.presentation.screen.util.AudioPermissionRationaleCard
import com.sailinghawklabs.speakfreely.presentation.theme.AppTheme
import com.sailinghawklabs.speakfreely.presentation.theme.TextCream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppSplashScreen(
    viewModel: AppSplashViewModel = koinViewModel(),
    onGotoMainScreen: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var launchPermissionRequest by remember { mutableStateOf<(() -> Unit)?>(null) }

    AudioPermissionHandler.ListenForPermissionState(
        onPermissionResult = { isGranted, isPermanentlyDenied ->
            viewModel.onEvent(
                AppSplashScreenEvent.PermissionIsChecked(
                    isGranted,
                    isPermanentlyDenied
                )
            )
        },
        onProvideLauncher = { launcher ->
            launchPermissionRequest = launcher
        }
    )

    AppSplashScreenContent(
        state = state,
        onGrantPermissionClick = { launchPermissionRequest?.invoke() },
        onOpenSettingsClick = { AudioPermissionHandler.openAppSettings(context) }
    )

    ObserveAsCommand(flow = viewModel.commandsForScreen) { command ->
        when (command) {
            AppSplashScreenCommand.NavigateToMainScreen -> onGotoMainScreen()
        }
    }
}

@Composable
fun AppSplashScreenContent(
    state: AppSplashState,
    onGrantPermissionClick: () -> Unit = {},
    onOpenSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Animation properties
    val scale = remember { Animatable(0f) }
    val rotation = remember { Animatable(-90f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 900,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            rotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 900,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600)
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFBC4F11),
                            Color(0xFF551D1D),
                            Color(0xFF390D0D),
                            Color.Black
                        )
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logo_static_no_bg),
                contentDescription = null,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    rotationZ = rotation.value
                    this.alpha = alpha.value
                }
            )
        }

        Image(
            painter = painterResource(R.drawable.app_name_pic),
            colorFilter = ColorFilter.tint(TextCream),
            contentScale = ContentScale.FillWidth,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 24.dp)
        )

        if (state.statusLogs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val visibleLogs = state.statusLogs.takeLast(5)
                visibleLogs.forEach { log ->
                    val isLatest = log == visibleLogs.last()
                    Text(
                        text = log.asString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextCream.copy(alpha = if (isLatest) 0.9f else 0.4f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        if (state.showPermissionRationale) {
            Spacer(modifier = Modifier.height(16.dp))
            AudioPermissionRationaleCard(
                isPermanentlyDenied = state.isPermissionPermanentlyDenied,
                onGrantClick = onGrantPermissionClick,
                onOpenSettingsClick = onOpenSettingsClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    AppTheme {
        AppSplashScreenContent(
            state = AppSplashState(
                showPermissionRationale = true,
                isPermissionPermanentlyDenied = true
            ),
        )
    }
}

@Composable
fun <T> ObserveAsCommand(flow: Flow<T>, onCommand: suspend (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(flow, lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(onCommand)
            }
        }
    }
}
