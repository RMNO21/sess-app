package com.example.web

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Message
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast

class SessWebViewClient(
    private val context: Context,
    private val onPageStartedCallback: (url: String) -> Unit,
    private val onPageFinishedCallback: (url: String) -> Unit,
    private val onPageErrorCallback: (error: String) -> Unit,
    private val onUrlRequestCallback: (url: String) -> Unit = {}
) : WebViewClient() {

    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        // Proceed with Iranian university SSL certificates to avoid blank white screens
        handler?.proceed()
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val uri = request?.url ?: return false
        val isForMainFrame = request.isForMainFrame
        return handleUrl(view, uri, isForMainFrame)
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        if (url == null) return false
        return handleUrl(view, Uri.parse(url), isForMainFrame = true)
    }

    private fun handleUrl(view: WebView?, uri: Uri, isForMainFrame: Boolean): Boolean {
        val urlStr = uri.toString()
        val scheme = uri.scheme?.lowercase() ?: ""
        val host = uri.host?.lowercase() ?: ""

        onUrlRequestCallback(urlStr)

        // 1. Allow WebView to handle standard pseudo-schemes internally
        if (scheme == "javascript" || scheme == "about" || scheme == "data" || scheme == "blob") {
            return false
        }

        // 2. CRITICAL: Never hijack subframes / iframes! Returning false lets WebView load inside the frame.
        if (!isForMainFrame) {
            return false
        }

        // 3. Keep ALL *.shirazu.ac.ir domains (sups, sess, vru, etc.) and relative URLs inside this WebView!
        // CRITICAL FIX: Returning FALSE lets native Chromium handle the navigation,
        // preserving HTTP POST payloads, ViewState, headers, referrers, and redirect chains.
        // DO NOT call view.loadUrl(urlStr) and return true!
        if (host.endsWith("shirazu.ac.ir") || host == "shirazu.ac.ir" || host.isEmpty()) {
            return false
        }

        // 4. External non-http/https app schemes (tel, mailto, sms, etc.)
        if (scheme != "http" && scheme != "https") {
            return try {
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                true
            } catch (e: Exception) {
                false
            }
        }

        // 5. For truly external HTTP/HTTPS websites (outside shirazu.ac.ir), launch in external browser
        return try {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        url?.let { onPageStartedCallback(it) }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        url?.let { onPageFinishedCallback(it) }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            val desc = error?.description?.toString() ?: "خطا در اتصال به سرور دانشگاه"
            onPageErrorCallback(desc)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        onPageErrorCallback(description ?: "خطا در برقراری ارتباط با سرور سامانه سس")
    }
}

class SessWebChromeClient(
    private val context: Context,
    private val onProgressChangedCallback: (Int) -> Unit,
    private val onTitleReceivedCallback: (String) -> Unit,
    private val onFileChooser: (ValueCallback<Array<Uri>>?, WebChromeClient.FileChooserParams?) -> Boolean,
    private val onConsoleMessageCallback: (level: String, message: String, line: Int, source: String) -> Unit = { _, _, _, _ -> }
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgressChangedCallback(newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        title?.let { onTitleReceivedCallback(it) }
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        return onFileChooser(filePathCallback, fileChooserParams)
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        consoleMessage?.let {
            onConsoleMessageCallback(
                it.messageLevel().name,
                it.message() ?: "",
                it.lineNumber(),
                it.sourceId() ?: ""
            )
        }
        return super.onConsoleMessage(consoleMessage)
    }

    // Handle window.open(...) and popup lists/reports used by SESS and SfxWeb
    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        if (view == null || resultMsg == null) return false
        val transport = resultMsg.obj as? WebView.WebViewTransport ?: return false
        val ctx = view.context

        try {
            val dialog = android.app.Dialog(ctx, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)

            val rootLayout = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            // Header Bar
            val header = RelativeLayout(ctx).apply {
                setBackgroundColor(android.graphics.Color.parseColor("#0284c7"))
                setPadding(24, 16, 24, 16)
            }

            val titleTv = TextView(ctx).apply {
                text = "پنجره سامانه سس"
                setTextColor(android.graphics.Color.WHITE)
                textSize = 15f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                val lp = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    addRule(RelativeLayout.CENTER_VERTICAL)
                }
                layoutParams = lp
            }

            val closeBtn = TextView(ctx).apply {
                text = "✖ بستن"
                setTextColor(android.graphics.Color.WHITE)
                textSize = 14f
                setPadding(16, 8, 16, 8)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setOnClickListener {
                    dialog.dismiss()
                }
                val lp = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                    addRule(RelativeLayout.CENTER_VERTICAL)
                }
                layoutParams = lp
            }

            header.addView(titleTv)
            header.addView(closeBtn)
            rootLayout.addView(header)

            val popupWebView = WebView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    javaScriptCanOpenWindowsAutomatically = true
                    allowFileAccess = true
                    allowContentAccess = true
                }

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)

                webViewClient = object : WebViewClient() {
                    override fun onReceivedSslError(v: WebView?, handler: SslErrorHandler?, error: SslError?) {
                        handler?.proceed()
                    }

                    override fun shouldOverrideUrlLoading(v: WebView?, req: WebResourceRequest?): Boolean {
                        val u = req?.url ?: return false
                        val h = u.host?.lowercase() ?: ""
                        if (h.endsWith("shirazu.ac.ir") || h == "shirazu.ac.ir" || h.isEmpty()) {
                            return false
                        }
                        return try {
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, u))
                            true
                        } catch (_: Exception) {
                            false
                        }
                    }

                    override fun onPageFinished(v: WebView?, url: String?) {
                        super.onPageFinished(v, url)
                        v?.evaluateJavascript(SessScriptInjector.getCompatibilityAndMenuFixScript(), null)
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onReceivedTitle(v: WebView?, title: String?) {
                        if (!title.isNullOrBlank()) {
                            titleTv.text = title
                        }
                    }

                    override fun onCloseWindow(window: WebView?) {
                        dialog.dismiss()
                    }
                }
            }

            rootLayout.addView(popupWebView)
            dialog.setContentView(rootLayout)
            dialog.show()

            transport.webView = popupWebView
            resultMsg.sendToTarget()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    // Native dialog for JS alert
    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        val act = view?.context as? Activity
        if (act != null && !act.isFinishing) {
            AlertDialog.Builder(act)
                .setTitle("پیام سامانه دانشگاه")
                .setMessage(message ?: "")
                .setPositiveButton("تأیید") { _, _ -> result?.confirm() }
                .setOnCancelListener { result?.confirm() }
                .show()
            return true
        }
        if (!message.isNullOrBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        result?.confirm()
        return true
    }

    // Native confirmation dialog (prevents auto-confirming critical student operations)
    override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        val act = view?.context as? Activity
        if (act != null && !act.isFinishing) {
            AlertDialog.Builder(act)
                .setTitle("تأیید عملیات")
                .setMessage(message ?: "")
                .setPositiveButton("بله") { _, _ -> result?.confirm() }
                .setNegativeButton("خیر") { _, _ -> result?.cancel() }
                .setOnCancelListener { result?.cancel() }
                .show()
            return true
        }
        result?.confirm()
        return true
    }

    // Native prompt dialog
    override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
        val act = view?.context as? Activity
        if (act != null && !act.isFinishing) {
            val input = EditText(act).apply {
                setText(defaultValue ?: "")
            }
            AlertDialog.Builder(act)
                .setTitle(message ?: "")
                .setView(input)
                .setPositiveButton("تأیید") { _, _ -> result?.confirm(input.text.toString()) }
                .setNegativeButton("انصراف") { _, _ -> result?.cancel() }
                .setOnCancelListener { result?.cancel() }
                .show()
            return true
        }
        result?.confirm(defaultValue ?: "")
        return true
    }
}
