package com.bhaloo.reminders.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bhaloo.reminders.ui.screens.AboutScreen
import com.bhaloo.reminders.ui.screens.EditorScreen
import com.bhaloo.reminders.ui.screens.HomeScreen
import com.bhaloo.reminders.ui.screens.SettingsScreen
import com.bhaloo.reminders.ui.screens.WelcomeScreen
import com.bhaloo.reminders.ui.theme.BhalooTheme

class MainActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* nothing to undo */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askForNotificationPermission()
        setContent {
            BhalooTheme {
                BhalooNavigation()
            }
        }
    }

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

/** Screens, kept deliberately simple — this app has four of them. */
sealed interface Screen {
    data object Home : Screen
    data class Editor(val reminderId: Long?) : Screen
    data object About : Screen
    data object Settings : Screen
}

@Composable
private fun BhalooNavigation() {
    val viewModel: ReminderViewModel = viewModel()
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var showWelcome by rememberSaveable { mutableStateOf(true) }

    when (val current = screen) {
        is Screen.Home -> HomeScreen(
            viewModel = viewModel,
            onAdd = { screen = Screen.Editor(null) },
            onEdit = { screen = Screen.Editor(it.id) },
            onAbout = { screen = Screen.About },
            onSettings = { screen = Screen.Settings }
        )

        is Screen.Editor -> EditorScreen(
            viewModel = viewModel,
            reminderId = current.reminderId,
            onClose = { screen = Screen.Home }
        )

        is Screen.About -> AboutScreen(
            viewModel = viewModel,
            onClose = { screen = Screen.Home }
        )

        is Screen.Settings -> SettingsScreen(
            viewModel = viewModel,
            onClose = { screen = Screen.Home }
        )
    }

    // A two-second dedication card on launch. It is, after all, the whole point.
    AnimatedVisibility(
        visible = showWelcome,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        WelcomeScreen(onDone = { showWelcome = false })
    }
}
