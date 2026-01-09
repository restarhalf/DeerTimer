package me.restarhalf.deer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.restarhalf.deer.data.ThemeRepository
import me.restarhalf.deer.ui.screens.MainScreen
import me.restarhalf.deer.ui.theme.Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs by ThemeRepository.themePreferences.collectAsState()

            Theme(colorSchemeMode = prefs.miuixColorSchemeMode) {
                MainScreen()
            }
        }
    }
}