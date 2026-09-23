package com.example.model

data class UserCredentials(
    val username: String = "",
    val password: String = "",
    val isAutoLoginEnabled: Boolean = true,
    val isAutoSubmitEnabled: Boolean = true,
    val isSaved: Boolean = false,
    val lastLoginTime: Long = 0L
)

data class SessionState(
    val isDashboardVisible: Boolean = true,
    val isKeepAliveRunning: Boolean = true,
    val lastHeartbeatSuccessTime: Long = 0L,
    val secondsUntilNextHeartbeat: Int = 120,
    val heartbeatIntervalMinutes: Int = 2,
    val totalHeartbeatsSent: Int = 0,
    val isCaptchaDetected: Boolean = false,
    val currentUrl: String = "https://sess.shirazu.ac.ir",
    val pageTitle: String = "سامانه سس دانشگاه شیراز",
    val pageProgress: Int = 0,
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isAutoLoginInProgress: Boolean = false,
    val statusMessage: String? = null,
    val pendingTargetUrl: String? = null,
    val pendingActionScript: String? = null
)

data class QuickLink(
    val title: String,
    val description: String,
    val url: String,
    val category: String,
    val iconName: String,
    val actionScript: String = ""
)

data class AppSettings(
    val isMobileOptimizationEnabled: Boolean = true,
    val isDesktopMode: Boolean = false,
    val isDarkModeReading: Boolean = false,
    val isKeepAliveEnabled: Boolean = true,
    val keepAliveIntervalMinutes: Int = 2,
    val autoBypassCaptchaOnDetect: Boolean = false,
    val textZoomPercent: Int = 100
)

data class DebugLogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "CLICK", "POSTBACK", "FORM_SUBMIT", "URL_REQUEST", "NAV_START", "NAV_END", "CONSOLE", "ERROR"
    val summary: String,
    val details: String = ""
)

data class CustomShortcut(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val targetUrl: String,
    val actionScript: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class SessMenuItem(
    val title: String,
    val script: String,
    val iconName: String = ""
)

data class SessMenuCategory(
    val id: String,
    val title: String,
    val iconName: String,
    val items: List<SessMenuItem>
)
