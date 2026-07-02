package com.danielcastillo.calendariointeractivo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danielcastillo.calendariointeractivo.notifications.ReminderScheduler
import com.danielcastillo.calendariointeractivo.ui.components.LoadingScrim
import com.danielcastillo.calendariointeractivo.ui.components.NotificationPermissionEffect
import com.danielcastillo.calendariointeractivo.ui.navigation.AppScreen
import com.danielcastillo.calendariointeractivo.ui.screen.AuthScreen
import com.danielcastillo.calendariointeractivo.ui.screen.CalendarScreen
import com.danielcastillo.calendariointeractivo.ui.screen.ProfileScreen
import com.danielcastillo.calendariointeractivo.ui.screen.SplashScreen
import com.danielcastillo.calendariointeractivo.ui.screen.WelcomeScreen
import kotlinx.coroutines.delay

@Composable
fun InteractiveCalendarApp(viewModel: AppViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var screen by rememberSaveable { mutableStateOf(AppScreen.Splash) }
    var splashFinished by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(2600)
        splashFinished = true
        screen = if (uiState.isAuthenticated) AppScreen.Calendar else AppScreen.Welcome
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message.text)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(uiState.isAuthenticated, splashFinished) {
        if (!splashFinished) return@LaunchedEffect

        if (uiState.isAuthenticated && screen in listOf(AppScreen.Welcome, AppScreen.Auth, AppScreen.Splash)) {
            screen = AppScreen.Calendar
        }

        if (!uiState.isAuthenticated && screen in listOf(AppScreen.Calendar, AppScreen.Profile)) {
            screen = AppScreen.Auth
        }
    }

    LaunchedEffect(uiState.events, uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            ReminderScheduler.scheduleForEvents(context, uiState.events)
        }
    }

    NotificationPermissionEffect(
        enabled = uiState.isAuthenticated,
        onPermissionSettled = viewModel::refreshMessagingToken
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (screen) {
                AppScreen.Splash -> SplashScreen()

                AppScreen.Welcome -> WelcomeScreen(
                    onEnter = {
                        screen = if (uiState.isAuthenticated) {
                            AppScreen.Calendar
                        } else {
                            AppScreen.Auth
                        }
                    }
                )

                AppScreen.Auth -> AuthScreen(
                    loading = uiState.operationInProgress,
                    onLogin = viewModel::login,
                    onRegister = viewModel::register,
                    onResetPassword = viewModel::resetPassword
                )

                AppScreen.Calendar -> CalendarScreen(
                    uiState = uiState,
                    onCreateEvent = viewModel::createEvent,
                    onDeleteEvent = viewModel::deleteEvent,
                    onProfile = {
                        screen = AppScreen.Profile
                    },
                    onLogout = {
                        viewModel.logout()
                        screen = AppScreen.Auth
                    }
                )

                AppScreen.Profile -> ProfileScreen(
                    user = uiState.user,
                    loading = uiState.operationInProgress,
                    onBack = { screen = AppScreen.Calendar },
                    onSave = viewModel::updateProfile
                )
            }

            if (uiState.isLoading && screen != AppScreen.Splash) {
                LoadingScrim()
            }
        }
    }
}