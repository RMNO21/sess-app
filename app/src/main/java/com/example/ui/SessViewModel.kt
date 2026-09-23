package com.example.ui

import android.app.Application
import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesManager
import com.example.model.AppSettings
import com.example.model.CustomShortcut
import com.example.model.DebugLogEntry
import com.example.model.QuickLink
import com.example.model.SessionState
import com.example.model.UserCredentials
import com.example.web.SessScriptInjector
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SessViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)

    val credentials: StateFlow<UserCredentials> = prefsManager.credentials
    val settings: StateFlow<AppSettings> = prefsManager.settings

    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _debugLogs = MutableStateFlow<List<DebugLogEntry>>(emptyList())
    val debugLogs: StateFlow<List<DebugLogEntry>> = _debugLogs.asStateFlow()

    private val _customShortcuts = MutableStateFlow<List<CustomShortcut>>(prefsManager.loadCustomShortcuts())
    val customShortcuts: StateFlow<List<CustomShortcut>> = _customShortcuts.asStateFlow()

    private var keepAliveJob: Job? = null
    private var splashTimeoutJob: Job? = null
    private var webViewRef: WebView? = null

    val quickLinks = listOf(
        QuickLink(
            title = "سامانه سس (اصلی)",
            description = "ورود به پورتال اصلی سس دانشگاه شیراز",
            url = "https://sess.shirazu.ac.ir",
            category = "اصلی",
            iconName = "home"
        ),
        QuickLink(
            title = "سامانه تغذیه و سلف (SUPS)",
            description = "رزرو و خرید اعتبار غذای دانشجویی",
            url = "https://sups.shirazu.ac.ir/SfxWeb",
            category = "رفاهی",
            iconName = "restaurant"
        ),
        QuickLink(
            title = "خلاصه کارنامه و نمرات",
            description = "مشاهده کارنامه کلی و نمرات ثبت شده",
            url = "https://sess.shirazu.ac.ir",
            category = "آموزشی",
            iconName = "grade",
            actionScript = "PerformStd('Sum');"
        ),
        QuickLink(
            title = "برنامه درسی",
            description = "مشاهده سرفصل و برنامه دروس",
            url = "https://sess.shirazu.ac.ir",
            category = "آموزشی",
            iconName = "schedule",
            actionScript = "Perform('Major');"
        ),
        QuickLink(
            title = "چک‌لیست انتخاب واحد",
            description = "بررسی وضعیت و مجوز انتخاب واحد",
            url = "https://sess.shirazu.ac.ir",
            category = "آموزشی",
            iconName = "schedule",
            actionScript = "PerformStd('Rcf');"
        ),
        QuickLink(
            title = "برنامه کلاسی نیمسال",
            description = "ساعت و روز تشکیل کلاس‌های ترم",
            url = "https://sess.shirazu.ac.ir",
            category = "آموزشی",
            iconName = "event",
            actionScript = "PerformStd('Pcl');"
        ),
        QuickLink(
            title = "صندوق پیام‌ها",
            description = "پیام‌های سیستم، استاد و کارشناس",
            url = "https://sess.shirazu.ac.ir",
            category = "ارتباطی",
            iconName = "email",
            actionScript = "PerformStd('Msg');"
        ),
        QuickLink(
            title = "امور اسکان و خوابگاه",
            description = "ثبت نام و مدیریت اتاق خوابگاه",
            url = "https://sess.shirazu.ac.ir",
            category = "رفاهی",
            iconName = "hotel",
            actionScript = "PerformStd('Dst');"
        ),
        QuickLink(
            title = "خرید ژتون و رفاهی",
            description = "شارژ ژتون و خدمات رفاهی دانشجویی",
            url = "https://sess.shirazu.ac.ir",
            category = "رفاهی",
            iconName = "restaurant",
            actionScript = "Perform('SfxChip');"
        ),
        QuickLink(
            title = "پرداخت شهریه اینترنتی",
            description = "پرداخت آنلاین و تسویه‌حساب مالی",
            url = "https://sess.shirazu.ac.ir",
            category = "مالی",
            iconName = "payment",
            actionScript = "Perform('IntPy');"
        ),
        QuickLink(
            title = "لیست پرداخت‌ها",
            description = "مشاهده تاریخچه و رسید تراکنش‌ها",
            url = "https://sess.shirazu.ac.ir",
            category = "مالی",
            iconName = "payment",
            actionScript = "PerformStd('Scr');"
        ),
        QuickLink(
            title = "پرونده دیجیتال",
            description = "سوابق، احکام و پرونده دانشجویی",
            url = "https://sess.shirazu.ac.ir",
            category = "کاربری",
            iconName = "school",
            actionScript = "PerformStd('SDGF');"
        ),
        QuickLink(
            title = "پورتال اصلی دانشگاه شیراز",
            description = "اخبار، تقویم و اطلاعیه‌های رسمی",
            url = "https://shirazu.ac.ir",
            category = "دانشگاه",
            iconName = "school"
        ),
        QuickLink(
            title = "سامانه یادگیری نوید (LMS)",
            description = "تکالیف، آزمون‌ها و محتوای الکترونیکی",
            url = "https://vru.shirazu.ac.ir",
            category = "دانشگاه",
            iconName = "computer"
        ),
        QuickLink(
            title = "خرید ژتون هفتگی (سلف)",
            description = "انتخاب و رزرو ژتون هفتگی غذا در سلف",
            url = "https://sups.shirazu.ac.ir/SfxWeb",
            category = "رفاهی",
            iconName = "restaurant",
            actionScript = "var b = document.getElementById('pbcw'); if (b) b.click(); else if (window.openNav) openNav();"
        ),
        QuickLink(
            title = "خرید ژتون لیستی (سلف)",
            description = "رزرو وعده‌های غذایی به صورت لیستی",
            url = "https://sups.shirazu.ac.ir/SfxWeb",
            category = "رفاهی",
            iconName = "restaurant",
            actionScript = "var b = document.getElementById('pbcL'); if (b) b.click(); else if (window.openNav) openNav();"
        ),
        QuickLink(
            title = "افزایش اعتبار سلف",
            description = "شارژ موجودی کارت تغذیه دانشجویی",
            url = "https://sups.shirazu.ac.ir/SfxWeb",
            category = "رفاهی",
            iconName = "payment",
            actionScript = "var b = document.getElementById('pbc'); if (b) b.click(); else if (window.openNav) openNav();"
        ),
        QuickLink(
            title = "گزارش خرید ژتون",
            description = "مشاهده تاریخچه وعده‌های رزرو شده",
            url = "https://sups.shirazu.ac.ir/SfxWeb/Emp/BoughtChip.aspx",
            category = "رفاهی",
            iconName = "receipt"
        ),
        QuickLink(
            title = "رزرو رفاهی (استخر و سونا)",
            description = "رزرو اماکن ورزشی و رفاهی دانشگاه",
            url = "https://sups.shirazu.ac.ir/SfxWeb",
            category = "رفاهی",
            iconName = "hotel",
            actionScript = "var b = document.getElementById('pr'); if (b) b.click(); else if (window.openNav) openNav();"
        ),
        QuickLink(
            title = "تفویض تحویل ژتون",
            description = "واگذاری و انتقال ژتون به سایر دانشجویان",
            url = "https://sups.shirazu.ac.ir/SfxWeb/SFX/SfxAssignment.aspx",
            category = "رفاهی",
            iconName = "people"
        )
    )

    fun getCleanMobileUserAgent(context: Context): String {
        val defaultUa = WebSettings.getDefaultUserAgent(context)
        return defaultUa.replace("; wv", "").replace(Regex("Version/\\d+\\.\\d+\\s*"), "")
    }

    fun executeQuickLink(link: QuickLink, webView: WebView) {
        addDebugLog("QUICKLINK_RUN", "باز کردن میان‌بر: ${link.title}", "URL: ${link.url}, Action: ${link.actionScript}")
        if (link.actionScript.isNotBlank()) {
            val currentUrl = _sessionState.value.currentUrl
            val matchesDomain = if (link.url.contains("sups.shirazu.ac.ir")) {
                currentUrl.contains("sups.shirazu.ac.ir")
            } else {
                currentUrl.contains("sess.shirazu.ac.ir")
            }

            if (matchesDomain) {
                webView.evaluateJavascript(link.actionScript, null)
            } else {
                webView.loadUrl(link.url.ifBlank { "https://sess.shirazu.ac.ir" })
            }
        } else if (link.url.isNotBlank()) {
            webView.loadUrl(link.url)
        }
    }

    init {
        startKeepAliveTimer()
        addDebugLog("SYSTEM", "برنامه Sess Plus آماده به کار شد", "نسخه دیباگ فعال است")
    }

    fun attachWebView(webView: WebView) {
        this.webViewRef = webView
    }

    fun detachWebView() {
        this.webViewRef = null
    }

    fun addDebugLog(type: String, summary: String, details: String = "") {
        val entry = DebugLogEntry(
            type = type,
            summary = summary,
            details = details
        )
        _debugLogs.update { current ->
            (listOf(entry) + current).take(300)
        }
    }

    fun clearDebugLogs() {
        _debugLogs.value = emptyList()
        addDebugLog("SYSTEM", "لاگ‌ها پاک‌سازی شدند")
    }

    fun copyLogsToClipboard(context: Context) {
        val logs = _debugLogs.value
        val sb = StringBuilder()
        sb.append("=== گزارش دیباگ سامانه سس دانشگاه شیراز (Sess Plus) ===\n")
        sb.append("زمان ایجاد گزارش: ").append(java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())).append("\n")
        sb.append("آدرس فعلی: ").append(_sessionState.value.currentUrl).append("\n")
        sb.append("عنوان صفحه: ").append(_sessionState.value.pageTitle).append("\n")
        sb.append("وضعیت کپچا: ").append(if (_sessionState.value.isCaptchaDetected) "تشخیص داده شده" else "عادی").append("\n")
        sb.append("تعداد کل لاگ‌ها: ").append(logs.size).append("\n")
        sb.append("========================================================\n\n")

        val sdf = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault())
        for (log in logs.reversed()) {
            sb.append("[").append(sdf.format(java.util.Date(log.timestamp))).append("] ")
            sb.append("[").append(log.type).append("] ")
            sb.append(log.summary).append("\n")
            if (log.details.isNotBlank()) {
                sb.append("  جزئیات: ").append(log.details).append("\n")
            }
            sb.append("--------------------------------------------------------\n")
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Sess Debug Logs", sb.toString())
        clipboard.setPrimaryClip(clip)
        _sessionState.update { it.copy(statusMessage = "تمام ${logs.size} لاگ در کلیپ‌بورد کپی شد") }
    }

    fun addCustomShortcut(name: String, targetUrl: String? = null, actionScript: String = "") {
        val url = targetUrl ?: _sessionState.value.currentUrl
        val finalName = name.ifBlank { "صفحه ${customShortcuts.value.size + 1}" }
        val newShortcut = CustomShortcut(
            name = finalName,
            targetUrl = url,
            actionScript = actionScript
        )
        val updated = _customShortcuts.value + newShortcut
        _customShortcuts.value = updated
        prefsManager.saveCustomShortcuts(updated)
        _sessionState.update { it.copy(statusMessage = "میانبر «$finalName» ذخیره شد") }
        addDebugLog("SHORTCUT_ADD", "میانبر جدید ثبت شد: $finalName", "URL: $url\nScript: $actionScript")
    }

    fun deleteCustomShortcut(id: String) {
        val updated = _customShortcuts.value.filterNot { it.id == id }
        _customShortcuts.value = updated
        prefsManager.saveCustomShortcuts(updated)
        _sessionState.update { it.copy(statusMessage = "میانبر حذف گردید") }
    }

    fun executeCustomShortcut(shortcut: CustomShortcut, webView: WebView) {
        addDebugLog("SHORTCUT_RUN", "در حال رفتن به میانبر: ${shortcut.name}", "URL: ${shortcut.targetUrl}")
        if (shortcut.actionScript.isNotBlank()) {
            webView.evaluateJavascript(shortcut.actionScript, null)
        } else if (shortcut.targetUrl.isNotBlank()) {
            webView.loadUrl(shortcut.targetUrl)
        }
    }

    private fun startKeepAliveTimer() {
        keepAliveJob?.cancel()
        keepAliveJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val currentSettings = settings.value
                if (!currentSettings.isKeepAliveEnabled) continue

                val targetSeconds = currentSettings.keepAliveIntervalMinutes * 60
                val currentRemaining = _sessionState.value.secondsUntilNextHeartbeat

                if (currentRemaining <= 1) {
                    _sessionState.update {
                        it.copy(
                            secondsUntilNextHeartbeat = targetSeconds,
                            totalHeartbeatsSent = it.totalHeartbeatsSent + 1
                        )
                    }
                    sendHeartbeatPing()
                } else {
                    _sessionState.update {
                        it.copy(secondsUntilNextHeartbeat = currentRemaining - 1)
                    }
                }
            }
        }
    }

    fun sendHeartbeatPing() {
        webViewRef?.let { webView ->
            webView.post {
                webView.evaluateJavascript(SessScriptInjector.getKeepAliveScript(), null)
            }
        }
    }

    fun onHeartbeatAck(success: Boolean) {
        if (success) {
            _sessionState.update {
                it.copy(
                    lastHeartbeatSuccessTime = System.currentTimeMillis(),
                    statusMessage = "نشست با موفقیت تمدید شد"
                )
            }
            addDebugLog("HEARTBEAT", "پالس پایداری نشست با موفقیت ثبت شد")
        } else {
            addDebugLog("HEARTBEAT_FAIL", "پالس پایداری ناموفق بود")
        }
    }

    fun bypassCaptchaAndResetSession(webView: WebView) {
        viewModelScope.launch {
            _sessionState.update {
                it.copy(
                    isLoading = true,
                    statusMessage = "در حال ایجاد نشست تازه..."
                )
            }
            addDebugLog("CAPTCHA_BYPASS", "درخواست عبور از کد امنیتی و بازنشانی نشست")

            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            WebStorage.getInstance().deleteAllData()

            _sessionState.update { it.copy(isCaptchaDetected = false) }

            delay(300)
            webView.loadUrl("https://sess.shirazu.ac.ir")
        }
    }

    private fun startSplashTimeoutTimer(maxDelayMs: Long = 2000L) {
        splashTimeoutJob?.cancel()
        splashTimeoutJob = viewModelScope.launch {
            delay(maxDelayMs)
            if (_sessionState.value.isAutoLoginInProgress) {
                _sessionState.update { it.copy(isAutoLoginInProgress = false) }
                addDebugLog("AUTOLOGIN", "تایمر ایمنی اسپلاش پایان یافت")
            }
        }
    }

    fun cancelAutoLoginSplash() {
        splashTimeoutJob?.cancel()
        _sessionState.update { it.copy(isAutoLoginInProgress = false) }
        addDebugLog("AUTOLOGIN", "کاربر انصراف از صفحه اسپلاش را انتخاب کرد")
    }

    fun onPageStarted(url: String) {
        addDebugLog("NAV_START", "شروع بارگذاری: $url")
        val creds = credentials.value
        val isLoginPageCandidate = creds.isSaved && creds.isAutoLoginEnabled && creds.username.isNotBlank() && url.contains("sess.shirazu.ac.ir")
        _sessionState.update {
            it.copy(
                currentUrl = url,
                isLoading = true,
                pageProgress = 15,
                isAutoLoginInProgress = if (isLoginPageCandidate) true else it.isAutoLoginInProgress,
                statusMessage = null
            )
        }
        if (isLoginPageCandidate) {
            startSplashTimeoutTimer(2200L)
        }
    }

    fun onPageFinished(url: String, webView: WebView) {
        val creds = credentials.value
        val isSavedAndEnabled = creds.isSaved && creds.isAutoLoginEnabled && creds.username.isNotBlank() && creds.password.isNotBlank()
        val isSessDomain = url.contains("sess.shirazu.ac.ir")

        addDebugLog("NAV_FINISH", "پایان بارگذاری: $url")

        // 1. Inject Comprehensive Debug Tracker (captures clicks, ASP.NET postbacks, forms)
        webView.evaluateJavascript(SessScriptInjector.getDebugTrackerScript(), null)

        // 2. Inject Compatibility & Menu List Fix (fixes hamburger menu toggle in SfxWeb & lists in SESS)
        webView.evaluateJavascript(SessScriptInjector.getCompatibilityAndMenuFixScript(), null)

        // 3. Safe Mobile Optimization (non-intrusive)
        webView.evaluateJavascript(
            SessScriptInjector.getMobileOptimizationScript(settings.value.isDarkModeReading),
            null
        )

        // 4. Captcha check
        webView.evaluateJavascript(SessScriptInjector.getCaptchaCheckScript(), null)

        // Update page state
        _sessionState.update {
            it.copy(
                currentUrl = url,
                isLoading = false,
                pageProgress = 100,
                canGoBack = webView.canGoBack(),
                canGoForward = webView.canGoForward()
            )
        }

        // 5. Evaluate if user is already logged in or not on login form
        webView.evaluateJavascript("""
            (function() {
                var passInput = document.querySelector('input[type="password"], #edPass');
                var hasLoggedInMenu = document.querySelector('#edSRightMenu, .nav__list, a[href*="Logout" i], a[href*="Exit" i], #mySidenav, .sidenav') !== null;
                var isLoginPage = passInput !== null && passInput.offsetParent !== null;
                return (!isLoginPage || hasLoggedInMenu);
            })()
        """.trimIndent()) { result ->
            if (result == "true") {
                splashTimeoutJob?.cancel()
                _sessionState.update { it.copy(isAutoLoginInProgress = false) }
            }
        }

        // 6. Immediate Auto-login if configured
        if (isSavedAndEnabled && isSessDomain) {
            val script = SessScriptInjector.getAutoLoginScript(
                username = creds.username,
                pass = creds.password,
                autoSubmit = creds.isAutoSubmitEnabled
            )
            webView.postDelayed({
                webView.evaluateJavascript(script, null)
            }, 50)
        }
    }

    fun onCaptchaDetected(detected: Boolean) {
        if (detected) {
            splashTimeoutJob?.cancel()
            _sessionState.update { 
                it.copy(
                    isCaptchaDetected = true,
                    isAutoLoginInProgress = false
                ) 
            }
            addDebugLog("CAPTCHA", "کد امنیتی در صفحه شناسایی شد")
            if (settings.value.autoBypassCaptchaOnDetect) {
                webViewRef?.let { bypassCaptchaAndResetSession(it) }
            }
        } else {
            _sessionState.update { it.copy(isCaptchaDetected = false) }
        }
    }

    fun onCredentialsFilled(success: Boolean) {
        if (success) {
            _sessionState.update { it.copy(statusMessage = "اطلاعات ورود تکمیل شد") }
            addDebugLog("AUTOLOGIN", "فیلدهای نام کاربری و رمز با موفقیت تکمیل شدند")
        }
    }

    fun onAutoLoginSubmitted(success: Boolean) {
        if (success) {
            _sessionState.update { it.copy(statusMessage = "ورود خودکار انجام شد") }
            addDebugLog("AUTOLOGIN_SUBMIT", "فرم ورود به صورت خودکار ارسال شد")
            viewModelScope.launch {
                delay(700)
                splashTimeoutJob?.cancel()
                _sessionState.update { it.copy(isAutoLoginInProgress = false) }
            }
        } else {
            splashTimeoutJob?.cancel()
            _sessionState.update { it.copy(isAutoLoginInProgress = false) }
        }
    }

    fun onProgressChanged(progress: Int) {
        _sessionState.update {
            it.copy(
                pageProgress = progress,
                isLoading = progress < 100
            )
        }
    }

    fun onTitleReceived(title: String) {
        _sessionState.update { it.copy(pageTitle = title) }
    }

    fun onPageError(error: String) {
        addDebugLog("ERROR", "خطای بارگذاری صفحه: $error")
        _sessionState.update {
            it.copy(
                isLoading = false,
                statusMessage = error
            )
        }
    }

    fun saveCredentials(username: String, pass: String, autoLogin: Boolean, autoSubmit: Boolean) {
        prefsManager.saveCredentials(username, pass, autoLogin, autoSubmit)
        _sessionState.update { it.copy(statusMessage = "اطلاعات دانشجویی با موفقیت ذخیره شد") }
        addDebugLog("CREDENTIALS", "اطلاعات ورود به‌روزرسانی شد: $username")
    }

    fun clearCredentials() {
        prefsManager.clearCredentials()
        _sessionState.update { it.copy(statusMessage = "اطلاعات ورود حذف شد") }
        addDebugLog("CREDENTIALS", "اطلاعات ورود حذف شد")
    }

    fun updateSettings(newSettings: AppSettings) {
        prefsManager.saveSettings(newSettings)
        webViewRef?.let { webView ->
            webView.evaluateJavascript(
                SessScriptInjector.getMobileOptimizationScript(newSettings.isDarkModeReading),
                null
            )
            webView.settings.textZoom = newSettings.textZoomPercent
        }
    }

    fun toggleDarkMode() {
        val current = settings.value
        val updated = current.copy(isDarkModeReading = !current.isDarkModeReading)
        updateSettings(updated)
    }

    fun toggleDesktopMode(webView: WebView) {
        val current = settings.value
        val updated = current.copy(isDesktopMode = !current.isDesktopMode)
        updateSettings(updated)

        if (updated.isDesktopMode) {
            val desktopUa = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            webView.settings.userAgentString = desktopUa
        } else {
            webView.settings.userAgentString = getCleanMobileUserAgent(webView.context)
        }
        addDebugLog("MODE", "تغییر حالت به: " + if (updated.isDesktopMode) "رایانه (Desktop)" else "موبایل استاندارد (Chrome)")
        webView.reload()
    }

    fun zoomIn(webView: WebView) {
        try {
            webView.zoomIn()
        } catch (_: Exception) {}
        val currentZoom = settings.value.textZoomPercent
        if (currentZoom < 180) {
            val newZoom = currentZoom + 10
            updateSettings(settings.value.copy(textZoomPercent = newZoom))
        }
        addDebugLog("ZOOM", "بزرگ‌نمایی: ${settings.value.textZoomPercent}%")
    }

    fun zoomOut(webView: WebView) {
        try {
            webView.zoomOut()
        } catch (_: Exception) {}
        val currentZoom = settings.value.textZoomPercent
        if (currentZoom > 70) {
            val newZoom = currentZoom - 10
            updateSettings(settings.value.copy(textZoomPercent = newZoom))
        }
        addDebugLog("ZOOM", "کوچک‌نمایی: ${settings.value.textZoomPercent}%")
    }

    fun resetZoom(webView: WebView) {
        try {
            webView.settings.textZoom = 100
        } catch (_: Exception) {}
        updateSettings(settings.value.copy(textZoomPercent = 100))
        _sessionState.update { it.copy(statusMessage = "اندازه صفحه به حالت استاندارد ۱۰۰٪ بازگشت") }
        addDebugLog("ZOOM", "اندازه صفحه به ۱۰۰٪ بازنشانی شد")
    }

    fun dismissStatusMessage() {
        _sessionState.update { it.copy(statusMessage = null) }
    }
}
