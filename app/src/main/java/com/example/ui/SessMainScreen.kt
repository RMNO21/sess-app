package com.example.ui

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CaptchaAlertBanner
import com.example.ui.components.DebugConsoleSheet
import com.example.ui.components.LoginCredentialsDialog
import com.example.ui.components.QuickLinksSheet
import com.example.ui.components.SessBottomBar
import com.example.ui.components.SessSplashScreen
import com.example.ui.components.SessTopAppBar
import com.example.ui.components.SessionInfoDialog
import com.example.ui.components.SettingsSheet
import com.example.web.SessWebBridge
import com.example.web.SessWebChromeClient
import com.example.web.SessWebViewClient

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SessMainScreen(
    viewModel: SessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val credentials by viewModel.credentials.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val debugLogs by viewModel.debugLogs.collectAsStateWithLifecycle()
    val customShortcuts by viewModel.customShortcuts.collectAsStateWithLifecycle()

    var showCredentialsDialog by remember { mutableStateOf(false) }
    var showSessionInfoDialog by remember { mutableStateOf(false) }
    var showShortcutsSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showDebugConsole by remember { mutableStateOf(false) }

    // Automatic prompt for credentials on first run if never saved
    var hasCheckedFirstRunPrompt by remember { mutableStateOf(false) }
    LaunchedEffect(credentials.isSaved) {
        if (!hasCheckedFirstRunPrompt) {
            hasCheckedFirstRunPrompt = true
            if (!credentials.isSaved) {
                showCredentialsDialog = true
            }
        }
    }

    // Reference to active WebView
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // File Chooser for SESS / Navid uploads
    var fileChooserCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val clipData = result.data?.clipData
        val dataUri = result.data?.data
        val results = mutableListOf<Uri>()
        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                results.add(clipData.getItemAt(i).uri)
            }
        } else if (dataUri != null) {
            results.add(dataUri)
        }
        fileChooserCallback?.onReceiveValue(if (results.isNotEmpty()) results.toTypedArray() else null)
        fileChooserCallback = null
    }

    // Handle Hardware / Gesture Back Press
    BackHandler(enabled = sessionState.canGoBack) {
        webViewInstance?.let { webView ->
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }
    }

    // Show temporary status messages
    LaunchedEffect(sessionState.statusMessage) {
        sessionState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SessTopAppBar(
                sessionState = sessionState,
                settings = settings,
                credentials = credentials,
                logCount = debugLogs.size,
                onRefresh = { webViewInstance?.reload() },
                onResetZoom = { webViewInstance?.let { viewModel.resetZoom(it) } },
                onOpenDebugConsole = { showDebugConsole = true },
                onBypassCaptcha = {
                    webViewInstance?.let { viewModel.bypassCaptchaAndResetSession(it) }
                },
                onOpenSessionInfo = { showSessionInfoDialog = true },
                onOpenCredentials = { showCredentialsDialog = true },
                onOpenSettings = { showSettingsSheet = true },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onToggleDesktop = {
                    webViewInstance?.let { viewModel.toggleDesktopMode(it) }
                },
                onZoomIn = { webViewInstance?.let { viewModel.zoomIn(it) } },
                onZoomOut = { webViewInstance?.let { viewModel.zoomOut(it) } }
            )
        },
        bottomBar = {
            SessBottomBar(
                sessionState = sessionState,
                settings = settings,
                credentials = credentials,
                onBack = {
                    webViewInstance?.let { if (it.canGoBack()) it.goBack() }
                },
                onForward = {
                    webViewInstance?.let { if (it.canGoForward()) it.goForward() }
                },
                onHome = {
                    webViewInstance?.loadUrl("https://sess.shirazu.ac.ir")
                },
                onRefresh = {
                    webViewInstance?.reload()
                },
                onResetZoom = {
                    webViewInstance?.let { viewModel.resetZoom(it) }
                },
                onZoomIn = {
                    webViewInstance?.let { viewModel.zoomIn(it) }
                },
                onZoomOut = {
                    webViewInstance?.let { viewModel.zoomOut(it) }
                },
                onOpenShortcuts = { showShortcutsSheet = true },
                onBypassCaptcha = {
                    webViewInstance?.let { viewModel.bypassCaptchaAndResetSession(it) }
                },
                onOpenCredentials = { showCredentialsDialog = true },
                onOpenSessionInfo = { showSessionInfoDialog = true },
                onOpenSettings = { showSettingsSheet = true },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onToggleDesktop = {
                    webViewInstance?.let { viewModel.toggleDesktopMode(it) }
                },
                onOpenDebugConsole = { showDebugConsole = true }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Captcha Warning Banner
            CaptchaAlertBanner(
                isVisible = sessionState.isCaptchaDetected,
                onBypassCaptcha = {
                    webViewInstance?.let { viewModel.bypassCaptchaAndResetSession(it) }
                },
                onDismiss = {
                    viewModel.onCaptchaDetected(false)
                }
            )

            // WebView Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewInstance = this
                            viewModel.attachWebView(this)

                            val cleanMobileUa = viewModel.getCleanMobileUserAgent(ctx)
                            val effectiveUa = if (settings.isDesktopMode) {
                                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                            } else {
                                cleanMobileUa
                            }

                            this@apply.settings.apply {
                                userAgentString = effectiveUa
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                textZoom = settings.textZoomPercent
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                                allowFileAccess = true
                                allowContentAccess = true
                                javaScriptCanOpenWindowsAutomatically = true
                                setSupportMultipleWindows(true)
                                mediaPlaybackRequiresUserGesture = false
                            }

                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            // Bridge for AutoLogin, Captcha, Heartbeat & Live Debug Logging
                            addJavascriptInterface(
                                SessWebBridge(
                                    onCaptcha = { detected ->
                                        post { viewModel.onCaptchaDetected(detected) }
                                    },
                                    onFilled = { success ->
                                        post { viewModel.onCredentialsFilled(success) }
                                    },
                                    onSubmitted = { success ->
                                        post { viewModel.onAutoLoginSubmitted(success) }
                                    },
                                    onHeartbeat = { ack ->
                                        post { viewModel.onHeartbeatAck(ack) }
                                    },
                                    onDebugLog = { type, summary, details ->
                                        post { viewModel.addDebugLog(type, summary, details) }
                                    }
                                ),
                                "AndroidBridge"
                            )

                            // Client for in-app navigation & URL tracking
                            webViewClient = SessWebViewClient(
                                context = ctx,
                                onPageStartedCallback = { url ->
                                    viewModel.onPageStarted(url)
                                },
                                onPageFinishedCallback = { url ->
                                    viewModel.onPageFinished(url, this)
                                },
                                onPageErrorCallback = { err ->
                                    viewModel.onPageError(err)
                                },
                                onUrlRequestCallback = { url ->
                                    viewModel.addDebugLog("URL_REQUEST", "درخواست ناوبری: $url")
                                }
                            )

                            // ChromeClient for progress, title, console logs, file uploads, and popups
                            webChromeClient = SessWebChromeClient(
                                context = ctx,
                                onProgressChangedCallback = { progress ->
                                    viewModel.onProgressChanged(progress)
                                },
                                onTitleReceivedCallback = { title ->
                                    viewModel.onTitleReceived(title)
                                },
                                onFileChooser = { filePathCallback, fileChooserParams ->
                                    fileChooserCallback?.onReceiveValue(null)
                                    fileChooserCallback = filePathCallback
                                    val intent = fileChooserParams?.createIntent()
                                        ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                            type = "*/*"
                                            addCategory(Intent.CATEGORY_OPENABLE)
                                        }
                                    try {
                                        filePickerLauncher.launch(intent)
                                        true
                                    } catch (e: Exception) {
                                        fileChooserCallback?.onReceiveValue(null)
                                        fileChooserCallback = null
                                        false
                                    }
                                },
                                onConsoleMessageCallback = { level, message, line, source ->
                                    viewModel.addDebugLog("CONSOLE_$level", message, "خط $line در $source")
                                }
                            )

                            // Download listener for transcripts, PDFs, receipts
                            setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
                                try {
                                    val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
                                    val request = DownloadManager.Request(Uri.parse(url)).apply {
                                        setMimeType(mimetype)
                                        val cookies = CookieManager.getInstance().getCookie(url)
                                        addRequestHeader("cookie", cookies)
                                        addRequestHeader("User-Agent", userAgent)
                                        setDescription("سامانه سس دانشگاه شیراز")
                                        setTitle(fileName)
                                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                                    }
                                    val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                    dm.enqueue(request)
                                    Toast.makeText(ctx, "در حال دانلود: $fileName", Toast.LENGTH_SHORT).show()
                                    viewModel.addDebugLog("DOWNLOAD", "دانلود فایل آغاز شد: $fileName", url)
                                } catch (e: Exception) {
                                    Toast.makeText(ctx, "خطا در شروع دانلود فایل", Toast.LENGTH_SHORT).show()
                                    viewModel.addDebugLog("DOWNLOAD_ERROR", "خطا در دانلود", e.localizedMessage ?: "")
                                }
                            }

                            loadUrl(sessionState.currentUrl)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("sess_webview")
                )

                // Page Loading Indicator
                if (sessionState.isLoading && sessionState.pageProgress < 75 && !sessionState.isAutoLoginInProgress) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Fast Auto-Login Splash Screen Overlay
                SessSplashScreen(
                    isVisible = sessionState.isAutoLoginInProgress,
                    credentials = credentials,
                    onCancel = { viewModel.cancelAutoLoginSplash() }
                )
            }
        }
    }

    // Debug Console Sheet (Logs & Custom Shortcut Creator)
    DebugConsoleSheet(
        isOpen = showDebugConsole,
        onDismiss = { showDebugConsole = false },
        logs = debugLogs,
        currentUrl = sessionState.currentUrl,
        customShortcuts = customShortcuts,
        onCopyAllLogs = { viewModel.copyLogsToClipboard(context) },
        onClearLogs = { viewModel.clearDebugLogs() },
        onAddShortcut = { name, targetUrl, script ->
            viewModel.addCustomShortcut(name, targetUrl, script)
        },
        onDeleteShortcut = { id ->
            viewModel.deleteCustomShortcut(id)
        },
        onExecuteShortcut = { shortcut ->
            webViewInstance?.let { viewModel.executeCustomShortcut(shortcut, it) }
        }
    )

    // Credentials Dialog
    if (showCredentialsDialog) {
        LoginCredentialsDialog(
            credentials = credentials,
            onSave = { user, pass, autoLogin, autoSubmit ->
                viewModel.saveCredentials(user, pass, autoLogin, autoSubmit)
                showCredentialsDialog = false
            },
            onClear = {
                viewModel.clearCredentials()
                showCredentialsDialog = false
            },
            onDismiss = { showCredentialsDialog = false }
        )
    }

    // Session Info & KeepAlive Dialog
    if (showSessionInfoDialog) {
        SessionInfoDialog(
            sessionState = sessionState,
            onTriggerHeartbeat = { viewModel.sendHeartbeatPing() },
            onDismiss = { showSessionInfoDialog = false }
        )
    }

    // Shortcuts Sheet
    if (showShortcutsSheet) {
        QuickLinksSheet(
            links = viewModel.quickLinks,
            customShortcuts = customShortcuts,
            onSelectLink = { link ->
                webViewInstance?.let { webView ->
                    viewModel.executeQuickLink(link, webView)
                }
            },
            onSelectCustomShortcut = { shortcut ->
                webViewInstance?.let { viewModel.executeCustomShortcut(shortcut, it) }
            },
            onDismiss = { showShortcutsSheet = false }
        )
    }

    // Settings Sheet
    if (showSettingsSheet) {
        SettingsSheet(
            settings = settings,
            onUpdateSettings = { newSettings ->
                viewModel.updateSettings(newSettings)
            },
            onClearAllData = {
                webViewInstance?.let { viewModel.bypassCaptchaAndResetSession(it) }
                viewModel.clearCredentials()
                showSettingsSheet = false
            },
            onDismiss = { showSettingsSheet = false }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.detachWebView()
        }
    }
}
