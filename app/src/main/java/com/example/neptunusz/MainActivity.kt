package com.example.neptunusz

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.neptunusz.ui.SettingsScreen
import com.example.neptunusz.ui.WebViewScreen
import com.example.neptunusz.ui.theme.NeptunuszTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeptunuszTheme {
                val viewModel: MainViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                if (uiState.showSettings) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onSave = { viewModel.closeSettings() },
                        onBack = { viewModel.closeSettings() }
                    )
                } else {
                    WebViewScreen(
                        viewModel = viewModel,
                        onOpenSettings = { viewModel.openSettings() }
                    )
                }
            }
        }
    }
}
