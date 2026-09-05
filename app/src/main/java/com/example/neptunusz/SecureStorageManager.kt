package com.example.neptunusz

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserCredentials(
    val neptunCode: String = "",
    val password: String = "",
    val totpSecret: String = "",
) {
    val hasCredentials: Boolean
        get() = neptunCode.isNotBlank() && password.isNotBlank()
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_prefs")

class SecureStorageManager(private val context: Context) {

    companion object {
        private val KEY_NEPTUN_CODE = stringPreferencesKey("neptun_code")
        private val KEY_PASSWORD = stringPreferencesKey("password")
        private val KEY_TOTP_SECRET = stringPreferencesKey("totp_secret")
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_HAS_CREDENTIALS = "has_credentials"
    }

    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val hasSavedCredentialsQuickCheck: Boolean
        get() = sharedPrefs.getBoolean(KEY_HAS_CREDENTIALS, false)

    fun setHasSavedCredentialsQuickCheck(hasCredentials: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_HAS_CREDENTIALS, hasCredentials).apply()
    }

    val credentialsFlow: Flow<UserCredentials> = context.dataStore.data.map { prefs ->
        val creds = UserCredentials(
            neptunCode = prefs[KEY_NEPTUN_CODE] ?: "",
            password = prefs[KEY_PASSWORD] ?: "",
            totpSecret = prefs[KEY_TOTP_SECRET] ?: "",
        )
        if (creds.hasCredentials != hasSavedCredentialsQuickCheck) {
            setHasSavedCredentialsQuickCheck(creds.hasCredentials)
        }
        creds
    }

    suspend fun saveCredentials(neptunCode: String, password: String, totpSecret: String) {
        val cleanTotpSecret = totpSecret.replace("\\s".toRegex(), "")
        val hasCreds = neptunCode.isNotBlank() && password.isNotBlank()
        setHasSavedCredentialsQuickCheck(hasCreds)
        context.dataStore.edit { prefs ->
            prefs[KEY_NEPTUN_CODE] = neptunCode
            prefs[KEY_PASSWORD] = password
            prefs[KEY_TOTP_SECRET] = cleanTotpSecret
        }
    }

    @Suppress("unused")
    suspend fun clearCredentials() {
        setHasSavedCredentialsQuickCheck(false)
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
