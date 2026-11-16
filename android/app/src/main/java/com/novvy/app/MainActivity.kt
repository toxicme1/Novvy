package com.novvy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novvy.app.data.ThemeMode
import com.novvy.app.ui.NovvyApp
import com.novvy.app.ui.NovvyViewModel
import com.novvy.app.ui.theme.NovvyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val vm: NovvyViewModel = viewModel()
            val state by vm.ui.collectAsState()
            val dark = when (state.settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            NovvyTheme(darkTheme = dark) {
                NovvyApp(vm)
            }
        }
    }
}