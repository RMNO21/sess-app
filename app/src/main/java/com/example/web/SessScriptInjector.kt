package com.example.web

object SessScriptInjector {

    fun getAutoLoginScript(username: String, pass: String, autoSubmit: Boolean): String {
        val safeUser = escapeJs(username)
        val safePass = escapeJs(pass)
        return """
            (function() {
                try {
                    // Check if we are on a login form
                    var userInputs = document.querySelectorAll('input[type="text"], input[name*="user" i], input[name*="id" i], #edId, #username');
                    var passInputs = document.querySelectorAll('input[type="password"], #edPass, #password');
                    
                    if (passInputs.length === 0) {
                        if (window.AndroidBridge && window.AndroidBridge.onAutoLoginSubmitted) {
                            window.AndroidBridge.onAutoLoginSubmitted(false);
                        }
                        return; // Not on a login page
                    }
                    
                    // Check for captcha
                    var captchaImgs = document.querySelectorAll('img[src*="captcha" i], img[src*="security" i], img[id*="captcha" i], img[class*="captcha" i]');
                    var captchaInputs = document.querySelectorAll('input[name*="captcha" i], input[name*="security" i], input[id*="captcha" i]');
                    
                    var isCaptchaPresent = false;
                    for (var i = 0; i < captchaImgs.length; i++) {
                        var img = captchaImgs[i];
                        if (img.offsetParent !== null && img.offsetWidth > 10 && img.offsetHeight > 10) {
                            isCaptchaPresent = true;
                            break;
                        }
                    }
                    if (!isCaptchaPresent && captchaInputs.length > 0) {
                        for (var j = 0; j < captchaInputs.length; j++) {
                            if (captchaInputs[j].offsetParent !== null) {
                                isCaptchaPresent = true;
                                break;
                            }
                        }
                    }
                    
                    if (isCaptchaPresent) {
                        if (window.AndroidBridge && window.AndroidBridge.onCaptchaDetected) {
                            window.AndroidBridge.onCaptchaDetected(true);
                        }
                        return;
                    } else {
                        if (window.AndroidBridge && window.AndroidBridge.onCaptchaDetected) {
                            window.AndroidBridge.onCaptchaDetected(false);
                        }
                    }
                    
                    var userInput = null;
                    for (var k = 0; k < userInputs.length; k++) {
                        var el = userInputs[k];
                        if (el.offsetParent !== null && !el.readOnly && !el.disabled) {
                            userInput = el;
                            break;
                        }
                    }
                    
                    var passInput = passInputs[0];
                    if (userInput && passInput) {
                        userInput.focus();
                        userInput.value = "$safeUser";
                        userInput.dispatchEvent(new Event('input', { bubbles: true }));
                        userInput.dispatchEvent(new Event('change', { bubbles: true }));
                        
                        passInput.focus();
                        passInput.value = "$safePass";
                        passInput.dispatchEvent(new Event('input', { bubbles: true }));
                        passInput.dispatchEvent(new Event('change', { bubbles: true }));
                        
                        if (window.AndroidBridge && window.AndroidBridge.onCredentialsFilled) {
                            window.AndroidBridge.onCredentialsFilled(true);
                        }
                        
                        if ($autoSubmit) {
                            setTimeout(function() {
                                var submitBtn = document.querySelector('#edEnter, input[type="submit"], button[type="submit"], input[value*="ورود"], input[value*="login" i]');
                                if (submitBtn && submitBtn.offsetParent !== null) {
                                    submitBtn.click();
                                } else {
                                    var form = passInput.form || (userInput && userInput.form);
                                    if (form) {
                                        form.submit();
                                    }
                                }
                                if (window.AndroidBridge && window.AndroidBridge.onAutoLoginSubmitted) {
                                    window.AndroidBridge.onAutoLoginSubmitted(true);
                                }
                            }, 50);
                        }
                    }
                } catch (e) {
                    console.error("AutoLogin Script error", e);
                }
            })();
        """.trimIndent()
    }

    fun getCaptchaCheckScript(): String {
        return """
            (function() {
                try {
                    var captchaImgs = document.querySelectorAll('img[src*="captcha" i], img[src*="security" i], img[id*="captcha" i], img[class*="captcha" i]');
                    var captchaInputs = document.querySelectorAll('input[name*="captcha" i], input[name*="security" i], input[id*="captcha" i]');
                    var detected = false;
                    for (var i = 0; i < captchaImgs.length; i++) {
                        var img = captchaImgs[i];
                        if (img.offsetParent !== null && img.offsetWidth > 10 && img.offsetHeight > 10) {
                            detected = true;
                            break;
                        }
                    }
                    if (!detected && captchaInputs.length > 0) {
                        for (var j = 0; j < captchaInputs.length; j++) {
                            if (captchaInputs[j].offsetParent !== null) {
                                detected = true;
                                break;
                            }
                        }
                    }
                    if (window.AndroidBridge && window.AndroidBridge.onCaptchaDetected) {
                        window.AndroidBridge.onCaptchaDetected(detected);
                    }
                } catch(e) {}
            })();
        """.trimIndent()
    }

    /**
     * Non-destructive Compatibility Polyfills:
     * - Polyfills window.showModalDialog for older ASP.NET university pages (converts to window.open)
     * - Polyfills window.event for legacy Internet Explorer ASP.NET handlers
     * - Safe fallback for SfxWeb drawer (#mySidenav) if not defined
     * - NEVER hides, overrides or breaks natural CSS accordion in .nav__list
     * - NEVER forces arbitrary table styles, widths or viewports
     */
    fun getCompatibilityAndMenuFixScript(): String {
        return """
            (function() {
                try {
                    if (!window.__sessPolyfillsInjected) {
                        window.__sessPolyfillsInjected = true;

                        // 1. Polyfill window.showModalDialog (Crucial for ASP.NET WebForms dialogs & reports)
                        if (!window.showModalDialog) {
                            window.showModalDialog = function(url, arg, opt) {
                                return window.open(url, '_blank');
                            };
                        }

                        // 2. Polyfill window.event for legacy Internet Explorer ASP.NET handlers
                        if (typeof window.event === 'undefined') {
                            window.event = null;
                        }
                        document.addEventListener('mousedown', function(e) { window.event = e; }, true);
                        document.addEventListener('keydown', function(e) { window.event = e; }, true);
                    }

                    // 3. Safe drawer helper for SfxWeb (#mySidenav) if not already initialized
                    var sidenav = document.getElementById('mySidenav');
                    if (sidenav) {
                        if (typeof window.openNav !== 'function') {
                            window.openNav = function() {
                                sidenav.style.width = 'min(300px, 85vw)';
                            };
                        }
                        if (typeof window.closeNav !== 'function') {
                            window.closeNav = function() {
                                sidenav.style.width = '0';
                            };
                        }
                    }
                } catch(e) {
                    console.error("Compatibility script error", e);
                }
            })();
        """.trimIndent()
    }

    /**
     * Safe styling: NO layout alterations, NO hiding elements, NO restructuring.
     * Keeps 100% of website content, tables, menus, and buttons exactly as designed.
     * Only applies dark mode filter when enabled.
     */
    fun getMobileOptimizationScript(darkMode: Boolean): String {
        if (!darkMode) {
            return """
                (function() {
                    var existingDark = document.getElementById('sess-plus-dark-style');
                    if (existingDark && existingDark.parentNode) {
                        existingDark.parentNode.removeChild(existingDark);
                    }
                })();
            """.trimIndent()
        }

        return """
            (function() {
                try {
                    var styleId = 'sess-plus-dark-style';
                    var styleEl = document.getElementById(styleId);
                    if (!styleEl) {
                        styleEl = document.createElement('style');
                        styleEl.id = styleId;
                        document.head.appendChild(styleEl);
                    }
                    styleEl.textContent = 'html { filter: invert(0.9) hue-rotate(180deg) !important; background: #121212 !important; } img, video, canvas { filter: invert(1) hue-rotate(180deg) !important; }';
                } catch(e) {}
            })();
        """.trimIndent()
    }

    /**
     * Non-blocking Debug & Action Logger
     * Captures Clicks, ASP.NET __doPostBack, and Forms without interfering with page behavior.
     */
    fun getDebugTrackerScript(): String {
        return """
            (function() {
                try {
                    if (window.__sessDebugTrackerInjected) return;
                    window.__sessDebugTrackerInjected = true;

                    function sendLog(type, summary, details) {
                        setTimeout(function() {
                            try {
                                if (window.AndroidBridge && window.AndroidBridge.logEvent) {
                                    window.AndroidBridge.logEvent(type, summary, typeof details === 'object' ? JSON.stringify(details, null, 2) : String(details));
                                }
                            } catch(e) {}
                        }, 0);
                    }

                    // 1. Initial page info
                    sendLog(
                        'PAGE_LOAD',
                        'صفحه بارگذاری شد: ' + (document.title || window.location.pathname),
                        {
                            title: document.title,
                            url: window.location.href,
                            referrer: document.referrer
                        }
                    );

                    // 2. Safely observe ASP.NET __doPostBack without breaking arguments
                    function hookPostBack(targetWin) {
                        var w = targetWin || window;
                        try {
                            if (typeof w.__doPostBack === 'function' && !w.__doPostBack.__isHooked) {
                                var originalDoPostBack = w.__doPostBack;
                                w.__doPostBack = function(eventTarget, eventArgument) {
                                    sendLog(
                                        'POSTBACK',
                                        'رویداد سرور ASP.NET: ' + eventTarget,
                                        {
                                            eventTarget: eventTarget,
                                            eventArgument: eventArgument,
                                            currentUrl: w.location.href
                                        }
                                    );
                                    return originalDoPostBack.apply(this, arguments);
                                };
                                w.__doPostBack.__isHooked = true;
                            }
                        } catch(e) {}
                    }
                    hookPostBack(window);

                    // 3. Document-wide Click Interceptor (Non-blocking passive listener)
                    document.addEventListener('click', function(e) {
                        try {
                            var el = e.target;
                            if (!el) return;
                            
                            var interactive = el.closest('a, button, input, [onclick], label') || el;
                            var tag = (interactive.tagName || '').toLowerCase();
                            var id = interactive.id || '';
                            var text = (interactive.innerText || interactive.value || '').trim();
                            if (text.length > 50) text = text.substring(0, 50) + '...';
                            
                            sendLog('CLICK', 'کلیک روی <' + tag + (id ? '#' + id : '') + '> ' + (text ? '"' + text + '"' : ''));
                        } catch(err) {}
                    }, true);

                } catch(e) {
                    console.error("Debug tracker init error", e);
                }
            })();
        """.trimIndent()
    }

    fun getKeepAliveScript(): String {
        return """
            (function() {
                try {
                    var target = '/sess/keepalive';
                    fetch(target, { method: 'GET', credentials: 'include', cache: 'no-cache' })
                        .then(function(res) {
                            if (window.AndroidBridge && window.AndroidBridge.onHeartbeatAck) {
                                window.AndroidBridge.onHeartbeatAck(true);
                            }
                        })
                        .catch(function(err) {
                            fetch(window.location.href, { method: 'HEAD', credentials: 'include', cache: 'no-cache' })
                                .then(function() {
                                    if (window.AndroidBridge && window.AndroidBridge.onHeartbeatAck) {
                                        window.AndroidBridge.onHeartbeatAck(true);
                                    }
                                })
                                .catch(function() {
                                    if (window.AndroidBridge && window.AndroidBridge.onHeartbeatAck) {
                                        window.AndroidBridge.onHeartbeatAck(false);
                                    }
                                });
                        });
                } catch(e) {
                    if (window.AndroidBridge && window.AndroidBridge.onHeartbeatAck) {
                        window.AndroidBridge.onHeartbeatAck(false);
                    }
                }
            })();
        """.trimIndent()
    }

    private fun escapeJs(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\'", "\\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
    }
}
