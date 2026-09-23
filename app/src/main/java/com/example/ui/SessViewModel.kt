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

    private var lastUserActionScript: String? = null

    val quickLinks: List<QuickLink> = emptyList()

    val sessMenuCategories: List<SessMenuCategory> = listOf(
        SessMenuCategory(
            id = "cat-user",
            title = "اطلاعات کاربری",
            iconName = "account_circle",
            items = listOf(
                SessMenuItem("تغيير کلمه رمز", "PerformStd('Cgp');", "lock"),
                SessMenuItem("اطلاعات پايه", "Perform('SignUp');", "badge"),
                SessMenuItem("اطلاعات افزوده", "PerformStd('Ovs');", "info"),
                SessMenuItem("ارسال مدارک", "PerformStd('Sdc');", "upload_file"),
                SessMenuItem("مشخصات دانشجويي", "Perform('BasicInfo');", "person"),
                SessMenuItem("خطاهای سیستم", "PerformStd('ErrRep');", "bug_report"),
                SessMenuItem("پرونده دیجیتال", "PerformStd('SDGF');", "folder"),
                SessMenuItem("اطلاعات كنكور", "PerformStd('Ssd');", "school"),
                SessMenuItem("آیین نامه ها", "PerformStd('Reg');", "menu_book"),
                SessMenuItem("تابلو احكام جديد", "PerformStd('Amt');", "campaign"),
                SessMenuItem("دریافت رمز اولیه", "PerformStd('KeyG');", "vpn_key"),
                SessMenuItem("تاریخچه ورود", "PerformStd('Vlg');", "history"),
                SessMenuItem("انتخاب کاربر پیش فرض SSO", "PerformStd('SDEF');", "switch_account")
            )
        ),
        SessMenuCategory(
            id = "cat-msg",
            title = "پيام‌ها",
            iconName = "mail",
            items = listOf(
                SessMenuItem("پيام‌ها", "PerformStd('Msg');", "mail"),
                SessMenuItem("پيام به استادمشاور", "Perform('Msg2Tch');", "send"),
                SessMenuItem("پيام به کارشناس بخش", "Perform('Msg2Exp');", "support_agent")
            )
        ),
        SessMenuCategory(
            id = "cat-edu",
            title = "آموزشی",
            iconName = "school",
            items = listOf(
                SessMenuItem("برنامه درسی", "Perform('Major');", "auto_stories"),
                SessMenuItem("دروس جبرانی", "Perform('Compensate');", "library_books"),
                SessMenuItem("لیست دروس گرفته", "Perform('Som');", "format_list_bulleted"),
                SessMenuItem("نمودار پیشرفت تحصیلی", "Perform('StdProgress');", "trending_up"),
                SessMenuItem("خلاصه کارنامه", "PerformStd('Sum');", "grade"),
                SessMenuItem("چک لیست ثبت نام مقدماتی", "PerformStd('Prc');", "checklist_rtl"),
                SessMenuItem("چک لیست انتخاب واحد", "PerformStd('Rcf');", "fact_check"),
                SessMenuItem("عملیات های ثبت نام", "Perform('RegLog');", "how_to_reg"),
                SessMenuItem("فارغ التحصیلی", "PerformStd('Sgr');", "celebration"),
                SessMenuItem("برنامه کلاسی نیمسال", "PerformStd('Pcl');", "calendar_month"),
                SessMenuItem("تقویم آموزشی", "PerformStd('Ssr');", "date_range"),
                SessMenuItem("سوابق تحصیلی انتقالی", "PerformStd('See');", "history_edu"),
                SessMenuItem("آزمونهای معافی", "PerformStd('Exl');", "assignment_turned_in"),
                SessMenuItem("امور دستیار استاد", "Perform('ExamTA');", "co_present"),
                SessMenuItem("جلسات مشاوره", "PerformStd('StdCons');", "record_voice_over")
            )
        ),
        SessMenuCategory(
            id = "cat-eval",
            title = "ارزیابی",
            iconName = "check_circle",
            items = listOf(
                SessMenuItem("تکمیل فرم های ارزیابی", "PerformStd('ActiveEvl');", "rule")
            )
        ),
        SessMenuCategory(
            id = "cat-student",
            title = "امور دانشجویی",
            iconName = "groups",
            items = listOf(
                SessMenuItem("خوابگاه", "PerformStd('Dst');", "apartment"),
                SessMenuItem("فرم هم اتاقی", "Perform('DormAgent');", "groups"),
                SessMenuItem("خوابگاه ورودیهای جدید", "Perform('DormitoryZero');", "meeting_room"),
                SessMenuItem("درخواست وام", "PerformStd('Erl');", "account_balance"),
                SessMenuItem("انتخابات دانشجویی", "PerformStd('Evt');", "how_to_vote"),
                SessMenuItem("خرید ژتون و رفاهی", "Perform('SfxChip');", "restaurant"),
                SessMenuItem("ثبت نام مراسم فارغ التحصیلی", "PerformStd('GRDSTD');", "school"),
                SessMenuItem("شبکه آزمایشگاهی دانشگاه", "PerformStd('LabsView');", "biotech"),
                SessMenuItem("درخواست های اسکان متفرقه", "PerformStd('NewRoomerReqs');", "hotel"),
                SessMenuItem("درخواست های نوبت دهی آزمایشگاه", "Perform('LabServReqs');", "science")
            )
        ),
        SessMenuCategory(
            id = "cat-finance",
            title = "امور مالی",
            iconName = "payments",
            items = listOf(
                SessMenuItem("پرداخت شهریه اینترنتی", "Perform('IntPy');", "payment"),
                SessMenuItem("لیست پرداختها", "PerformStd('Scr');", "receipt_long"),
                SessMenuItem("پرداختهای اینترنتی", "PerformStd('Psp');", "credit_card"),
                SessMenuItem("چک های تقسیطی", "PerformStd('Psc');", "price_check"),
                SessMenuItem("چک لیست مالی نیمسال", "PerformStd('Sfc');", "checklist"),
                SessMenuItem("جدول شهریه", "Perform('AccTable');", "table_chart"),
                SessMenuItem("شهریه", "Perform('Pst');", "attach_money"),
                SessMenuItem("شهریه رایگان", "PerformStd('Frt');", "money_off"),
                SessMenuItem("بدهی های موضوعی", "PerformStd('StdDebPay');", "request_quote"),
                SessMenuItem("حق التدریس دستیار آموزشی", "PerformStd('HSC');", "payments")
            )
        ),
        SessMenuCategory(
            id = "cat-process",
            title = "فرايندها",
            iconName = "sync",
            items = listOf(
                SessMenuItem("فرايند ثبت نام", "Perform('Spr');", "published_with_changes"),
                SessMenuItem("منابع و برنامه ها", "PerformStd('Rss');", "hub"),
                SessMenuItem("فرآیندهای دانشجویی", "PerformStd('Ssi');", "account_tree"),
                SessMenuItem("نوبت های مراجعه", "PerformStd('Ats');", "event_available"),
                SessMenuItem("فرايند ثبت نام (جدید)", "Perform('RegStdProc');", "dynamic_feed")
            )
        ),
        SessMenuCategory(
            id = "cat-cultural",
            title = "امور فرهنگی",
            iconName = "palette",
            items = listOf(
                SessMenuItem("امور فرهنگی", "PerformStd('CLE');", "local_library")
            )
        ),
        SessMenuCategory(
            id = "cat-virtual",
            title = "امور واحدهای مجازی",
            iconName = "computer",
            items = listOf(
                SessMenuItem("کتابخانه دیجیتال", "PerformStd('DgtL');", "menu_book"),
                SessMenuItem("گفتگو با کارشناس بخش", "Connect2EduExpert();", "chat"),
                SessMenuItem("گفتگو با کارشناس حسابداری", "Connect2CalExpert();", "support")
            )
        )
    )

    fun getCleanMobileUserAgent(context: Context): String {
        val defaultUa = WebSettings.getDefaultUserAgent(context)
        return defaultUa.replace("; wv", "").replace(Regex("Version/\\d+\\.\\d+\\s*"), "")
    }

    fun executeQuickLink(link: QuickLink, webView: WebView) {
        addDebugLog("QUICKLINK_RUN", "باز کردن میان‌بر: ${link.title}", "URL: ${link.url}, Action: ${link.actionScript}")
        if (link.actionScript.isNotBlank()) {
            lastUserActionScript = link.actionScript
        }
        _sessionState.update {
            it.copy(
                isDashboardVisible = false,
                pendingActionScript = link.actionScript.ifBlank { null },
                pendingTargetUrl = link.url.ifBlank { "https://sess.shirazu.ac.ir" },
                statusMessage = "در حال انتقال به «${link.title}»..."
            )
        }
        if (link.actionScript.isNotBlank()) {
            val currentUrl = _sessionState.value.currentUrl
            val matchesDomain = currentUrl.contains("sess.shirazu.ac.ir") && !currentUrl.contains("Logout", ignoreCase = true)
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

    fun showDashboard() {
        _sessionState.update { it.copy(isDashboardVisible = true) }
    }

    fun hideDashboard() {
        _sessionState.update { it.copy(isDashboardVisible = false) }
    }

    fun toggleDashboard() {
        _sessionState.update { it.copy(isDashboardVisible = !it.isDashboardVisible) }
    }

    fun addDebugLog(type: String, summary: String, details: String = "") {
        if (type == "USER_ACTION" || (type == "CLICK" && details.startsWith("onclick: "))) {
            val script = if (type == "USER_ACTION") summary else details.removePrefix("onclick: ").trim()
            if (script.contains("Perform", ignoreCase = true) || script.contains("Connect2", ignoreCase = true)) {
                lastUserActionScript = script
            }
        }
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

    fun updateCustomShortcut(shortcut: CustomShortcut) {
        prefsManager.updateCustomShortcut(shortcut)
        _customShortcuts.value = prefsManager.loadCustomShortcuts()
        _sessionState.update { it.copy(statusMessage = "میانبر «${shortcut.name}» به‌روزرسانی شد") }
        addDebugLog("SHORTCUT_UPDATE", "میانبر به‌روزرسانی شد: ${shortcut.name}")
    }

    fun deleteCustomShortcut(id: String) {
        val updated = _customShortcuts.value.filterNot { it.id == id }
        _customShortcuts.value = updated
        prefsManager.saveCustomShortcuts(updated)
        _sessionState.update { it.copy(statusMessage = "میانبر حذف گردید") }
    }

    fun executeCustomShortcut(shortcut: CustomShortcut, webView: WebView) {
        addDebugLog("SHORTCUT_RUN", "در حال رفتن به میانبر: ${shortcut.name}", "URL: ${shortcut.targetUrl}, Script: ${shortcut.actionScript}")
        if (shortcut.actionScript.isNotBlank()) {
            lastUserActionScript = shortcut.actionScript
        }
        _sessionState.update {
            it.copy(
                isDashboardVisible = false,
                pendingActionScript = shortcut.actionScript.ifBlank { null },
                pendingTargetUrl = shortcut.targetUrl.ifBlank { "https://sess.shirazu.ac.ir" },
                statusMessage = "در حال انتقال به «${shortcut.name}»..."
            )
        }
        if (shortcut.actionScript.isNotBlank()) {
            val currentUrl = _sessionState.value.currentUrl
            val isAlreadyOnSess = currentUrl.contains("sess.shirazu.ac.ir") && !currentUrl.contains("Logout", ignoreCase = true)
            if (isAlreadyOnSess) {
                webView.evaluateJavascript(shortcut.actionScript, null)
            } else {
                webView.loadUrl(shortcut.targetUrl.ifBlank { "https://sess.shirazu.ac.ir" })
            }
        } else if (shortcut.targetUrl.isNotBlank()) {
            webView.loadUrl(shortcut.targetUrl)
        }
    }

    fun executeMenuItem(item: SessMenuItem, webView: WebView) {
        addDebugLog("MENU_EXEC", "اجرای آیتم منوی ۹ گانه: ${item.title}", "Script: ${item.script}")
        lastUserActionScript = item.script
        _sessionState.update {
            it.copy(
                isDashboardVisible = false,
                pendingActionScript = item.script,
                pendingTargetUrl = "https://sess.shirazu.ac.ir",
                statusMessage = "در حال انتقال به «${item.title}»..."
            )
        }

        val currentUrl = _sessionState.value.currentUrl
        val isAlreadyOnSess = currentUrl.contains("sess.shirazu.ac.ir") && !currentUrl.contains("Logout", ignoreCase = true)
        if (isAlreadyOnSess) {
            webView.evaluateJavascript(item.script, null)
        } else {
            webView.loadUrl("https://sess.shirazu.ac.ir")
        }
    }

    fun checkAndResumePendingTarget(webView: WebView) {
        val pendingScript = _sessionState.value.pendingActionScript
        val pendingUrl = _sessionState.value.pendingTargetUrl

        if (!pendingScript.isNullOrBlank()) {
            addDebugLog("AUTO_RESUME", "بازیابی خودکار نشست و مقصد: اجرای اسکریپت پس از تایید ورود", pendingScript)
            _sessionState.update {
                it.copy(
                    pendingActionScript = null,
                    statusMessage = "ورود مجدد تایید شد؛ هدایت به بخش انتخابی..."
                )
            }
            viewModelScope.launch {
                delay(500)
                webView.evaluateJavascript(pendingScript, null)
            }
        } else if (!pendingUrl.isNullOrBlank() && !pendingUrl.equals("https://sess.shirazu.ac.ir", ignoreCase = true) && !pendingUrl.contains("login", ignoreCase = true)) {
            addDebugLog("AUTO_RESUME", "بازیابی خودکار نشست و مقصد: بارگذاری آدرس پس از تایید ورود", pendingUrl)
            _sessionState.update {
                it.copy(
                    pendingTargetUrl = null,
                    statusMessage = "ورود مجدد تایید شد؛ باز کردن آدرس درخواستی..."
                )
            }
            viewModelScope.launch {
                delay(300)
                webView.loadUrl(pendingUrl)
            }
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

        // Auto-recovery check: If redirected to logout or login unexpectedly, preserve last user action
        if (url.contains("Logout.aspx", ignoreCase = true) || (url.contains("sess.shirazu.ac.ir") && url.contains("login", ignoreCase = true))) {
            if (_sessionState.value.pendingActionScript == null && !lastUserActionScript.isNullOrBlank()) {
                _sessionState.update { it.copy(pendingActionScript = lastUserActionScript) }
                addDebugLog("SESSION_RECOVERY", "تشخیص خروج ناخواسته از نشست؛ ذخیره اسکریپت برای اجرای خودکار پس از ورود مجدد", lastUserActionScript ?: "")
            }
        }

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
                checkAndResumePendingTarget(webView)
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
                delay(800)
                splashTimeoutJob?.cancel()
                _sessionState.update { it.copy(isAutoLoginInProgress = false) }
                webViewRef?.let { checkAndResumePendingTarget(it) }
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
        if (progress >= 70) {
            webViewRef?.evaluateJavascript(SessScriptInjector.getCompatibilityAndMenuFixScript(), null)
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
