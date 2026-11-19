package com.sailinghawklabs.speakfreely.presentation.screen.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

object AudioPermissionHandler {

    fun isPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    @Composable
    fun ListenForPermissionState(
        onPermissionResult: (isGranted: Boolean, isPermanentlyDenied: Boolean) -> Unit,
        onProvideLauncher: (launchPermissionRequest: () -> Unit) -> Unit
    ) {
        val context = LocalContext.current
        val activity = context as? Activity
        val lifecycleOwner = LocalLifecycleOwner.current
        var hasAskedPermission by rememberSaveable { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasAskedPermission = true
            val showRationale =
                activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.RECORD_AUDIO
                )
            onPermissionResult(isGranted, !isGranted && !showRationale)
        }

        onProvideLauncher {
            hasAskedPermission = true
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    val isGranted = isPermissionGranted(context)
                    val showRationale =
                        activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            Manifest.permission.RECORD_AUDIO
                        )
                    onPermissionResult(
                        isGranted,
                        !isGranted && hasAskedPermission && !showRationale
                    )
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }
}
