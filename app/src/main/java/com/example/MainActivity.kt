package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TransactionViewModel
import com.example.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as FinanceApplication
        val factory = ViewModelFactory(app.repository, app.preferencesManager)
        val viewModel: TransactionViewModel by viewModels { factory }

        setContent {
            val userDarkMode by viewModel.isDarkModeEnabled.collectAsState()
            val isDark = userDarkMode ?: isSystemInDarkTheme()
            val appColorHex by viewModel.targetAppColorHex.collectAsState()
            var showSplash by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
            val initialAction = androidx.compose.runtime.remember { intent?.action }
            
            androidx.compose.runtime.LaunchedEffect(Unit) {
                intent?.action = null
                kotlinx.coroutines.delay(2000)
                showSplash = false
            }

            MyApplicationTheme(darkTheme = isDark, customAppColorHex = appColorHex) {
                if (showSplash) {
                    com.example.ui.screens.SplashScreen()
                } else {
                    MainScreen(viewModel = viewModel, initialIntentAction = initialAction)
                }
            }
        }
    }
}
