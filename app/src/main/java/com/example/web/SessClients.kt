package com.example.web

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Message
import android.webkit.ConsoleMessage
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.ViewGroup
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
        return handleUrl(view, uri)
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        if (url == null) return false
        return handleUrl(view, Uri.parse(url))
    }

    private fun handleUrl(view: WebView?, uri: Uri): Boolean {
        val urlStr = uri.toString()
        val scheme = uri.scheme?.lowercase() ?: ""
        val host = uri.host?.lowercase() ?: ""

        onUrlRequestCallback(urlStr)

        // CRITICAL FIX: Never launch Intents for javascript, about, data, blob!
        // Returning false allows WebView to execute javascript:... (e.g. doPostBack, ShowMenu, void(0))
        if (scheme == "javascript" || scheme == "about" || scheme == "data" || scheme == "blob") {
            return false
        }

        val urlLower = urlStr.lowercase()

        // Keep ALL *.shirazu.ac.ir domains (sups, sess, vru, etc.) and relative URLs strictly inside this WebView!
        if (host.contains("shirazu.ac.ir") || urlLower.contains("shirazu.ac.ir") || host.isEmpty()) {
            if (view != null) {
                view.loadUrl(urlStr)
                return true // Stay inside the app, prevent OS browser intent
            }
            return false
        }

        // External app schemes (tel, mailto, sms, etc.)
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

        // For truly external HTTP/HTTPS websites (outside shirazu.ac.ir), launch in external phone browser
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

    // CRITICAL FIX: Handle window.open(...) and popup lists/reports used by SESS and SfxWeb in full Chromium screen!
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

            val rootLayout = android.widget.LinearLayout(ctx).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            // Clean, professional Chromium Header Bar
            val header = android.widget.RelativeLayout(ctx).apply {
                setBackgroundColor(android.graphics.Color.parseColor("#0284c7"))
                setPadding(24, 16, 24, 16)
            }

            val titleTv = android.widget.TextView(ctx).apply {
                text = "پنجره سامانه سس"
                setTextColor(android.graphics.Color.WHITE)
                textSize = 15f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                val lp = android.widget.RelativeLayout.LayoutParams(
                    android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(android.widget.RelativeLayout.ALIGN_PARENT_RIGHT)
                    addRule(android.widget.RelativeLayout.CENTER_VERTICAL)
                }
                layoutParams = lp
            }

            val closeBtn = android.widget.TextView(ctx).apply {
                text = "✖ بستن"
                setTextColor(android.graphics.Color.WHITE)
                textSize = 14f
                setPadding(16, 8, 16, 8)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setOnClickListener {
                    dialog.dismiss()
                }
                val lp = android.widget.RelativeLayout.LayoutParams(
                    android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(android.widget.RelativeLayout.ALIGN_PARENT_LEFT)
                    addRule(android.widget.RelativeLayout.CENTER_VERTICAL)
                }
                layoutParams = lp
            }

            header.addView(titleTv)
            header.addView(closeBtn)
            rootLayout.addView(header)

            val popupWebView = WebView(ctx).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
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
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    javaScriptCanOpenWindowsAutomatically = true
                    allowFileAccess = true
                    allowContentAccess = true
                }

                webViewClient = object : WebViewClient() {
                    override fun onReceivedSslError(v: WebView?, handler: SslErrorHandler?, error: SslError?) {
                        handler?.proceed()
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

    // Handle JS alert/confirm so university scripts never hang or freeze
    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        if (!message.isNullOrBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        result?.confirm()
        return true
    }

    override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        result?.confirm()
        return true
    }

    override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
        result?.confirm(defaultValue ?: "")
        return true
    }
}
