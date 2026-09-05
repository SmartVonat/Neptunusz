package com.example.neptunusz.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.example.neptunusz.MainViewModel
import com.example.neptunusz.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import androidx.compose.ui.tooling.preview.Preview

// Placeholder IDs - User should replace these with actual Neptun DOM IDs
private const val INPUT_USER_ID = "userName"
private const val INPUT_PASS_ID = "password-form-password"
private const val INPUT_TOTP_ID = "two-factor-qr-code-input-form-input"
private const val BTN_SUBMIT_ID = "login-button"
private const val BTN_VERIFY_ID = "login-button" // Often the same, update if different
private const val LOGIN_URL_KEYWORD = "login"
private const val TARGET_URL = "https://neptun.bme.hu/hallgatoi/login" // Replace with actual Neptun URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit
) {
    var webView: WebView? by remember { mutableStateOf(null) }
    var canGoBack by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(74.dp),
                title = { Text(stringResource(R.string.app_name), modifier = Modifier.offset(y = (-7).dp)) },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = { webView?.goBack() }, modifier = Modifier.offset(y = (-6).dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { webView?.reload() }, modifier = Modifier.offset(y = (-6).dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.cd_refresh))
                    }
                    IconButton(onClick = onOpenSettings, modifier = Modifier.offset(y = (-6).dp)) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.cd_settings))
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webView = this
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    configureWebView(this)
                    
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            super.onPageFinished(view, url)
                            canGoBack = view.canGoBack()
                            
                            if (url.contains(LOGIN_URL_KEYWORD)) {
                                scope.launch {
                                    viewModel.credentials.first { it.hasCredentials }
                                    injectCredentials(view, viewModel)
                                }
                            }
                            // Ensure cookies are flushed
                            CookieManager.getInstance().flush()
                        }
                    }
                    
                    loadUrl(TARGET_URL)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            update = { 
                webView = it 
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun configureWebView(webView: WebView) {
    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        loadWithOverviewMode = true
        useWideViewPort = true
        val defaultUa = userAgentString
        userAgentString = defaultUa.replace("; wv", "").replace(Regex("Version/\\d+\\.\\d+\\s?"), "")
    }

    CookieManager.getInstance().apply {
        setAcceptCookie(true)
        setAcceptThirdPartyCookies(webView, true)
    }
}

private fun injectCredentials(webView: WebView, viewModel: MainViewModel) {
    val user = viewModel.getNeptunCode()
    val pass = viewModel.getPassword()
    val totp = viewModel.getTotpCode()

    val js = """
        (function() {
            var step1Done = false;

            function setFieldValue(field, value) {
                if (!field || !value) return;
                field.value = value;
                // Események elsütése, hogy az Angular keretrendszer észrevegye a módosítást
                field.dispatchEvent(new Event('input', { bubbles: true }));
                field.dispatchEvent(new Event('change', { bubbles: true }));
                field.dispatchEvent(new Event('blur', { bubbles: true }));
            }

            function tryInject() {
                var userField = document.getElementById('$INPUT_USER_ID');
                var passField = document.getElementById('$INPUT_PASS_ID');
                var totpField = document.getElementById('$INPUT_TOTP_ID');
                
                // Step 2 (TOTP ablak kitöltése)
                if (totpField && totpField.offsetParent !== null) {
                    setFieldValue(totpField, '$totp');
                    
                    // Adunk 600ms-t az Angularnak, hogy levegye a 'disabled' státuszt a gombról
                    setTimeout(function() {
                        // Kifejezetten a felugró ablak (mat-dialog-actions) gombját keressük meg
                        var verifyBtn = document.querySelector('mat-dialog-actions #$BTN_VERIFY_ID');
                        
                        if (verifyBtn && !verifyBtn.hasAttribute('disabled')) {
                            verifyBtn.click();
                        }
                    }, 600);
                    
                    return true; // Leállítjuk a folyamatos keresést
                }

                // Step 1 (Első login képernyő)
                if (!step1Done && userField && passField && userField.offsetParent !== null) {
                    setFieldValue(userField, '$user');
                    setFieldValue(passField, '$pass');
                    var submitBtn = document.getElementById('$BTN_SUBMIT_ID');
                    if (submitBtn) {
                        step1Done = true;
                        setTimeout(function() {
                            submitBtn.click();
                        }, 500);
                    }
                }
                
                return false; 
            }

            // Ha az első próbálkozás nem sikerül, MutationObserverrel figyeljük a változásokat (pl. felugró ablak)
            if (!tryInject()) {
                var observer = new MutationObserver(function(mutations) {
                    if (tryInject()) {
                        observer.disconnect();
                    }
                });
                observer.observe(document.body, { 
                    childList: true, 
                    subtree: true, 
                    attributes: true 
                });
            }
        })();
    """.trimIndent()

    webView.evaluateJavascript(js, null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun WebViewScreenPreview() {
    MaterialTheme {
        TopAppBar(
            modifier = Modifier.height(80.dp),
            title = { Text(stringResource(R.string.app_name), modifier = Modifier.offset(y = (-6).dp)) },
            navigationIcon = {
                IconButton(onClick = {}, modifier = Modifier.offset(y = (-6).dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                }
            },
            actions = {
                IconButton(onClick = {}, modifier = Modifier.offset(y = (-6).dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.cd_refresh))
                }
                IconButton(onClick = {}, modifier = Modifier.offset(y = (-6).dp)) {
                    Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.cd_settings))
                }
            }
        )
    }
}
