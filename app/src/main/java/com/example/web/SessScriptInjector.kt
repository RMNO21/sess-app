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
     * Compatibility & Full Nav List Display Fix:
     * - Guarantees #edSRightMenu, .baghdi, and ul.nav__list are ALWAYS visible on mobile & desktop
     * - Re-arranges 3-column table on mobile screens so columns never clip or push menu offscreen
     * - Makes ul.nav__list accordion groups (اطلاعات کاربری، پیامها، آموزشی، ...) 100% reliably expand/collapse on tap
     * - Polyfills window.showModalDialog and window.event for legacy ASP.NET scripts
     */
    fun getCompatibilityAndMenuFixScript(): String {
        return """
            (function() {
                try {
                    // 1. Global Polyfills
                    if (!window.__sessPolyfillsInjected) {
                        window.__sessPolyfillsInjected = true;

                        if (!window.showModalDialog) {
                            window.showModalDialog = function(url, arg, opt) {
                                return window.open(url, '_blank');
                            };
                        }

                        if (typeof window.event === 'undefined') {
                            window.event = null;
                        }
                        document.addEventListener('mousedown', function(e) { window.event = e; }, true);
                        document.addEventListener('keydown', function(e) { window.event = e; }, true);
                    }

                    // 2. SfxWeb Drawer helper
                    var sidenav = document.getElementById('mySidenav');
                    if (sidenav) {
                        if (typeof window.openNav !== 'function') {
                            window.openNav = function() { sidenav.style.width = 'min(300px, 85vw)'; };
                        }
                        if (typeof window.closeNav !== 'function') {
                            window.closeNav = function() { sidenav.style.width = '0'; };
                        }
                    }

                    // 3. Inject CSS to guarantee visibility of .nav__list and responsive stacking
                    function injectMenuVisibilityCss(doc) {
                        if (!doc || doc.getElementById('sess-nav-visible-css')) return;
                        var style = doc.createElement('style');
                        style.id = 'sess-nav-visible-css';
                        style.innerHTML = [
                            '/* Guarantee Right Menu and Navigation List are never hidden */',
                            '#edSRightMenu, td#edSRightMenu, .baghdi, .baghdi.scroll, nav.nav, nav.header, ul.nav__list {',
                            '    display: block !important;',
                            '    visibility: visible !important;',
                            '    opacity: 1 !important;',
                            '    max-height: none !important;',
                            '    overflow: visible !important;',
                            '}',
                            '/* Responsive adaptation for mobile: prevent table columns from clipping offscreen */',
                            '@media (max-width: 900px) {',
                            '    #maintbl {',
                            '        display: block !important;',
                            '        width: 100% !important;',
                            '        max-width: 100% !important;',
                            '    }',
                            '    #maintbl > tbody, #maintbl > tbody > tr {',
                            '        display: flex !important;',
                            '        flex-direction: column !important;',
                            '        width: 100% !important;',
                            '    }',
                            '    #maintbl > tbody > tr > td, #edSRightMenu, td#edSRightMenu {',
                            '        display: block !important;',
                            '        width: 100% !important;',
                            '        max-width: 100% !important;',
                            '        box-sizing: border-box !important;',
                            '    }',
                            '}',
                            '/* Accordion styling for ul.nav__list */',
                            'ul.nav__list {',
                            '    display: block !important;',
                            '    visibility: visible !important;',
                            '    list-style: none !important;',
                            '    padding: 4px !important;',
                            '    margin: 0 !important;',
                            '    direction: rtl !important;',
                            '    text-align: right !important;',
                            '}',
                            'ul.nav__list > li {',
                            '    display: block !important;',
                            '    visibility: visible !important;',
                            '    margin-bottom: 6px !important;',
                            '    border-radius: 8px !important;',
                            '    border: 1px solid #cbd5e1 !important;',
                            '    background: #ffffff !important;',
                            '    overflow: hidden !important;',
                            '}',
                            'ul.nav__list label {',
                            '    display: flex !important;',
                            '    align-items: center !important;',
                            '    justify-content: space-between !important;',
                            '    padding: 11px 13px !important;',
                            '    font-size: 14px !important;',
                            '    font-weight: bold !important;',
                            '    color: #0369a1 !important;',
                            '    background: #f0f9ff !important;',
                            '    cursor: pointer !important;',
                            '    user-select: none !important;',
                            '    -webkit-user-select: none !important;',
                            '    margin: 0 !important;',
                            '}',
                            'ul.nav__list label::after {',
                            '    content: "▼" !important;',
                            '    font-size: 10px !important;',
                            '    color: #0284c7 !important;',
                            '    transition: transform 0.2s ease !important;',
                            '}',
                            'ul.nav__list li.is-open > label::after,',
                            'ul.nav__list input[type="checkbox"]:checked ~ label::after {',
                            '    transform: rotate(180deg) !important;',
                            '}',
                            '/* Group list items (تغییر کلمه رمز، اطلاعات پایه، ...) */',
                            'ul.nav__list ul.group-list, ul.nav__list .group-list {',
                            '    display: none;',
                            '    list-style: none !important;',
                            '    padding: 4px 8px !important;',
                            '    margin: 0 !important;',
                            '    background: #ffffff !important;',
                            '}',
                            'ul.nav__list li.is-open > ul.group-list,',
                            'ul.nav__list li.is-open > .group-list,',
                            'ul.nav__list input[type="checkbox"]:checked ~ ul.group-list,',
                            'ul.nav__list input[type="checkbox"]:checked ~ .group-list {',
                            '    display: block !important;',
                            '    visibility: visible !important;',
                            '    opacity: 1 !important;',
                            '}',
                            'ul.nav__list ul.group-list li {',
                            '    display: block !important;',
                            '    margin: 4px 0 !important;',
                            '}',
                            'ul.nav__list ul.group-list a.link, ul.nav__list a.link {',
                            '    display: block !important;',
                            '    padding: 9px 12px !important;',
                            '    font-size: 13px !important;',
                            '    color: #0f172a !important;',
                            '    background: #f8fafc !important;',
                            '    border: 1px solid #e2e8f0 !important;',
                            '    border-radius: 6px !important;',
                            '    text-decoration: none !important;',
                            '    cursor: pointer !important;',
                            '    font-weight: 500 !important;',
                            '}',
                            'ul.nav__list ul.group-list a.link:active {',
                            '    background: #e0f2fe !important;',
                            '    color: #0284c7 !important;',
                            '}'
                        ].join('\n');
                        (doc.head || doc.documentElement || doc.body).appendChild(style);
                    }

                    // 4. Bulletproof interactive toggle for nav__list accordion
                    function setupNavListAccordion(doc) {
                        if (!doc) return;
                        var navLists = doc.querySelectorAll('.nav__list, ul.nav__list');
                        navLists.forEach(function(navList) {
                            var items = navList.querySelectorAll('> li');
                            items.forEach(function(li) {
                                var input = li.querySelector('input[type="checkbox"]');
                                var label = li.querySelector('label');
                                var subList = li.querySelector('ul.group-list, .group-list');

                                if (!label || !subList) return;

                                // Allow touch events on label to toggle without native checkbox suppression
                                if (input && input.hasAttribute('hidden')) {
                                    input.removeAttribute('hidden');
                                    input.style.cssText = 'position:absolute!important;opacity:0!important;pointer-events:none!important;width:1px!important;height:1px!important;';
                                }

                                if (label.__sessAccordionAttached) return;
                                label.__sessAccordionAttached = true;

                                var toggleHandler = function(e) {
                                    e.preventDefault();
                                    e.stopPropagation();

                                    var isExpanded = li.classList.contains('is-open') || 
                                                     subList.style.display === 'block' || 
                                                     (input && input.checked);

                                    if (isExpanded) {
                                        li.classList.remove('is-open');
                                        subList.style.setProperty('display', 'none', 'important');
                                        if (input) input.checked = false;
                                    } else {
                                        li.classList.add('is-open');
                                        subList.style.setProperty('display', 'block', 'important');
                                        subList.style.setProperty('visibility', 'visible', 'important');
                                        subList.style.setProperty('opacity', '1', 'important');
                                        if (input) input.checked = true;
                                    }
                                };

                                label.addEventListener('click', toggleHandler, true);
                            });
                        });
                    }

                    // Apply to current document and any child frames
                    function applyAll(d) {
                        try {
                            injectMenuVisibilityCss(d);
                            setupNavListAccordion(d);
                        } catch(e) {}
                    }

                    var allDocs = [document];
                    try {
                        var frames = document.querySelectorAll('iframe, frame');
                        for (var i = 0; i < frames.length; i++) {
                            try {
                                var fDoc = frames[i].contentDocument || (frames[i].contentWindow && frames[i].contentWindow.document);
                                if (fDoc && allDocs.indexOf(fDoc) === -1) {
                                    allDocs.push(fDoc);
                                }
                            } catch(e) {}
                        }
                    } catch(e) {}

                    allDocs.forEach(applyAll);

                    // Run gentle checks to handle delayed ASP.NET WebForms DOM rendering
                    setTimeout(function() { allDocs.forEach(applyAll); }, 300);
                    setTimeout(function() { allDocs.forEach(applyAll); }, 900);
                    setTimeout(function() { allDocs.forEach(applyAll); }, 2000);

                } catch(e) {
                    console.error("Compatibility script error", e);
                }
            })();
        """.trimIndent()
    }

    /**
     * Safe styling: Keeps 100% of website content, tables, menus, and buttons intact.
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
