package com.example.neptunusz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppUiState(
    val hasCredentials: Boolean = false,
    val showSettings: Boolean = false,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val secureStorage = SecureStorageManager(application)

    val credentials: StateFlow<UserCredentials> = secureStorage.credentialsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserCredentials(),
        )

    private val userSettingsOverride = MutableStateFlow<Boolean?>(null)

    val uiState: StateFlow<AppUiState> = combine(
        credentials,
        userSettingsOverride,
    ) { creds, override ->
        val showSettings = override ?: !creds.hasCredentials
        AppUiState(
            hasCredentials = creds.hasCredentials,
            showSettings = showSettings,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppUiState(hasCredentials = false, showSettings = true),
    )

    fun saveCredentials(neptunCode: String, pass: String, secret: String) {
        viewModelScope.launch {
            secureStorage.saveCredentials(neptunCode, pass, secret)
            userSettingsOverride.value = false
        }
    }

    fun openSettings() {
        userSettingsOverride.value = true
    }

    fun closeSettings() {
        if (credentials.value.hasCredentials) {
            userSettingsOverride.value = false
        }
    }

    fun getNeptunCode(): String = credentials.value.neptunCode
    fun getPassword(): String = credentials.value.password
    fun getTotpSecret(): String = credentials.value.totpSecret
    fun getTotpCode(): String = TotpGenerator.generateCode(getTotpSecret())
}
