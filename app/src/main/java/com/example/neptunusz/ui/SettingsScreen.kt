package com.example.neptunusz.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.neptunusz.MainViewModel
import com.example.neptunusz.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onSave: () -> Unit,
    onBack: () -> Unit = { viewModel.closeSettings() }
) {
    val credentials by viewModel.credentials.collectAsState()

    var neptunCode by rememberSaveable(credentials.neptunCode) { mutableStateOf(credentials.neptunCode) }
    var password by rememberSaveable(credentials.password) { mutableStateOf(credentials.password) }
    var totpSecret by rememberSaveable(credentials.totpSecret) { mutableStateOf(credentials.totpSecret) }
    var passVisible by rememberSaveable { mutableStateOf(false) }
    var secretVisible by rememberSaveable { mutableStateOf(false) }
    var is2FaEnabled by rememberSaveable(credentials.totpSecret) { mutableStateOf(credentials.totpSecret.isNotBlank()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.settings_title))
                        LanguageToggle()
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = neptunCode,
                onValueChange = { neptunCode = it },
                label = { Text(stringResource(R.string.neptun_code)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passVisible = !passVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                }
            )

            OutlinedTextField(
                value = totpSecret,
                onValueChange = { totpSecret = it },
                label = { Text(stringResource(R.string.totp_secret_label)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = is2FaEnabled,
                visualTransformation = if (secretVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (secretVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(
                        onClick = { secretVisible = !secretVisible },
                        enabled = is2FaEnabled
                    ) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                }
            )

            if (is2FaEnabled) {
                Text(
                    text = stringResource(R.string.totp_secret_warning),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.enable_2fa),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = is2FaEnabled,
                    onCheckedChange = { is2FaEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.saveCredentials(neptunCode, password, if (is2FaEnabled) totpSecret else "")
                    onSave()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = neptunCode.isNotBlank() && password.isNotBlank() && (!is2FaEnabled || totpSecret.isNotBlank())
            ) {
                Text(stringResource(R.string.save_credentials))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageToggle() {
    val currentLocaleTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val configuration = LocalConfiguration.current
    val activeLang = currentLocaleTags.ifEmpty { configuration.locales[0].language }
    val isHu = activeLang.startsWith("hu")

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.padding(end = 8.dp)
    ) {
        SegmentedButton(
            selected = isHu,
            onClick = {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("hu"))
            },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
        ) {
            Text("HU", style = MaterialTheme.typography.labelSmall)
        }
        SegmentedButton(
            selected = !isHu,
            onClick = {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
        ) {
            Text("EN", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Settings")
                            LanguageToggle()
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding))
        }
    }
}
