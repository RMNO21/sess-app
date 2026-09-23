package com.example.web

import android.webkit.JavascriptInterface

class SessWebBridge(
    private val onCaptcha: (Boolean) -> Unit,
    private val onFilled: (Boolean) -> Unit,
    private val onSubmitted: (Boolean) -> Unit,
    private val onHeartbeat: (Boolean) -> Unit,
    private val onDebugLog: (type: String, summary: String, details: String) -> Unit = { _, _, _ -> }
) {
    @JavascriptInterface
    fun onCaptchaDetected(detected: Boolean) {
        onCaptcha(detected)
    }

    @JavascriptInterface
    fun onCredentialsFilled(success: Boolean) {
        onFilled(success)
    }

    @JavascriptInterface
    fun onAutoLoginSubmitted(success: Boolean) {
        onSubmitted(success)
    }

    @JavascriptInterface
    fun onHeartbeatAck(success: Boolean) {
        onHeartbeat(success)
    }

    @JavascriptInterface
    fun logEvent(type: String, summary: String, details: String) {
        onDebugLog(type, summary, details)
    }
}
