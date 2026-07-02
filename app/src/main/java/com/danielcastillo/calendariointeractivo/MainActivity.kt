package com.danielcastillo.calendariointeractivo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danielcastillo.calendariointeractivo.ui.AppViewModel
import com.danielcastillo.calendariointeractivo.ui.AppViewModelFactory
import com.danielcastillo.calendariointeractivo.ui.InteractiveCalendarApp
import com.danielcastillo.calendariointeractivo.ui.theme.CalendarioInteractivoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CalendarioInteractivoTheme {
                val viewModel: AppViewModel = viewModel(
                    factory = AppViewModelFactory(application)
                )

                InteractiveCalendarApp(viewModel = viewModel)
            }
        }
    }
}