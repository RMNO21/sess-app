package com.example.web

object SessScriptInjector {

    fun getAutoLoginScript(username: String, pass: String, autoSubmit: Boolean): String {
        val safeUser = escapeJs(username)
        val safePass = escapeJs(pass)
        return """
            (function() {
                try {
                    // 1. Locate SESS or standard login inputs
                    var userInput = document.getElementById('edId') ||
                        document.querySelector('input[name*="user" i], input[name*="id" i], #username, input[type="text"]');
                    var passInput = document.getElementById('edPass') ||
                        document.querySelector('input[type="password"], #password');
                    
                    if (!passInput) {
                        if (window.AndroidBridge && window.AndroidBridge.onAutoLoginSubmitted) {
                            window.AndroidBridge.onAutoLoginSubmitted(false);
                        }
                        return; // Not a login page
                    }
                    
                    // 2. Check for captcha
                    var captchaImgs = document.querySelectorAll('img[src*="captcha" i], img[src*="security" i], img[id*="captcha" i], img[id*="edCodeImage" i]');
                    var captchaInput = document.getElementById('edCode') ||
                        document.querySelector('input[name*="captcha" i], input[name*="security" i]');
                    
                    var isCaptchaPresent = false;
                    for (var i = 0; i < captchaImgs.length; i++) {
                        var img = captchaImgs[i];
                        if (img.offsetWidth > 10 && img.offsetHeight > 10 || img.offsetParent !== null) {
                            isCaptchaPresent = true;
                            break;
                        }
                    }
                    if (!isCaptchaPresent && captchaInput) {
                        var isVisible = (captchaInput.offsetParent !== null || window.getComputedStyle(captchaInput).display !== 'none');
                        if (isVisible && !captchaInput.disabled && !captchaInput.readOnly) {
                            isCaptchaPresent = true;
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
                    
                    // 3. Fill Credentials
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
                            // In SESS, Login.js defines Save() which hashes password with _RKey and calls MakeMember
                            var submitAction = function() {
                                if (typeof window.Save === 'function') {
                                    window.Save();
                                    return true;
                                }
                                var submitBtn = document.getElementById('edEnter') ||
                                    document.querySelector('input[type="submit"], button[type="submit"], input[value*="ورود"], input[value*="login" i]');
                                if (submitBtn) {
                                    submitBtn.click();
                                    return true;
                                }
                                var form = passInput.form || (userInput && userInput.form);
                                if (form) {
                                    form.submit();
                                    return true;
                                }
                                return false;
                            };

                            setTimeout(function() {
                                var submitted = submitAction();
                                if (window.AndroidBridge && window.AndroidBridge.onAutoLoginSubmitted) {
                                    window.AndroidBridge.onAutoLoginSubmitted(submitted);
                                }
                            }, 120);
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

                        // Polyfill PerformStd if not present
                        if (typeof window.PerformStd !== 'function') {
                            window.PerformStd = function(act) {
                                if (typeof window.Perform === 'function') {
                                    window.Perform(act);
                                } else {
                                    var ch = document.getElementById('Channel');
                                    if (ch && document.forms.length > 0) {
                                        ch.value = 'Act=' + act + ';';
                                        document.forms[0].submit();
                                    }
                                }
                            };
                        }
                    }

                    // Detect SESS Session Expiration (ErrorResetInfo screen)
                    var isExpiredPage = document.title === 'ErrorResetInfo' ||
                        document.querySelector('form[action*="ErrorResetInfo" i]') !== null ||
                        (document.body && document.body.innerText && document.body.innerText.indexOf('خروج خودکار کاربران توسط سیستم') !== -1);
                    if (isExpiredPage) {
                        if (window.AndroidBridge && window.AndroidBridge.logEvent) {
                            window.AndroidBridge.logEvent('SESSION_EXPIRED', 'صفحه انقضای نشست (ErrorResetInfo) شناسایی شد', window.location.href);
                        }
                        var edCopy = document.getElementById('edCopy');
                        if (edCopy) {
                            edCopy.click();
                        } else {
                            window.location.href = '/sess/Script/Logout.aspx';
                        }
                        return;
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
                            '    padding: 12px 14px !important;',
                            '    font-size: 14px !important;',
                            '    font-weight: bold !important;',
                            '    color: #0369a1 !important;',
                            '    background: #f0f9ff !important;',
                            '    cursor: pointer !important;',
                            '    user-select: none !important;',
                            '    -webkit-user-select: none !important;',
                            '    margin: 0 !important;',
                            '}',
                            'ul.nav__list label * {',
                            '    pointer-events: none !important;',
                            '}',
                            'ul.nav__list label::after {',
                            '    content: "▼" !important;',
                            '    font-size: 10px !important;',
                            '    color: #0284c7 !important;',
                            '    transition: transform 0.2s ease !important;',
                            '}',
                            'ul.nav__list li[data-open="true"] > label::after,',
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
                            'ul.nav__list li[data-open="true"] > ul.group-list,',
                            'ul.nav__list li[data-open="true"] > .group-list,',
                            'ul.nav__list li.is-open > ul.group-list,',
                            'ul.nav__list li.is-open > .group-list,',
                            'ul.nav__list input[type="checkbox"]:checked ~ ul.group-list,',
                            'ul.nav__list input[type="checkbox"]:checked ~ .group-list {',
                            '    display: block !important;',
                            '    visibility: visible !important;',
                            '    opacity: 1 !important;',
                            '    max-height: 5000px !important;',
                            '    height: auto !important;',
                            '    overflow: visible !important;',
                            '}',
                            'ul.nav__list ul.group-list li {',
                            '    display: block !important;',
                            '    visibility: visible !important;',
                            '    opacity: 1 !important;',
                            '    margin: 4px 0 !important;',
                            '}',
                            'ul.nav__list ul.group-list a, ul.nav__list ul.group-list a.link {',
                            '    display: block !important;',
                            '    visibility: visible !important;',
                            '    opacity: 1 !important;',
                            '    padding: 10px 14px !important;',
                            '    font-size: 13px !important;',
                            '    color: #0f172a !important;',
                            '    background: #f8fafc !important;',
                            '    border: 1px solid #e2e8f0 !important;',
                            '    border-radius: 6px !important;',
                            '    text-decoration: none !important;',
                            '    cursor: pointer !important;',
                            '    font-weight: 500 !important;',
                            '    min-height: 38px !important;',
                            '    box-sizing: border-box !important;',
                            '}',
                            'ul.nav__list ul.group-list a:active, ul.nav__list ul.group-list a.link:active {',
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
                            // Helper to set group open/closed state
                            function setGroupState(li, open) {
                                var input = li.querySelector('input[type="checkbox"]');
                                var subList = li.querySelector('ul.group-list, .group-list');
                                if (!subList) return;

                                if (open) {
                                    li.setAttribute('data-open', 'true');
                                    li.classList.add('is-open');
                                    subList.style.setProperty('display', 'block', 'important');
                                    subList.style.setProperty('visibility', 'visible', 'important');
                                    subList.style.setProperty('opacity', '1', 'important');
                                    subList.style.setProperty('max-height', 'none', 'important');
                                    subList.style.setProperty('height', 'auto', 'important');
                                    subList.style.setProperty('overflow', 'visible', 'important');
                                    if (input) input.checked = true;
                                } else {
                                    li.setAttribute('data-open', 'false');
                                    li.classList.remove('is-open');
                                    subList.style.setProperty('display', 'none', 'important');
                                    subList.style.setProperty('max-height', '0px', 'important');
                                    subList.style.setProperty('height', '0px', 'important');
                                    subList.style.setProperty('overflow', 'hidden', 'important');
                                    if (input) input.checked = false;
                                }
                            }

                            // Add Expand-All / Collapse-All Toolbar above menu
                            var parent = navList.parentElement || navList.parentNode;
                            if (parent && !parent.querySelector('#sess-nav-controls')) {
                                var toolbar = doc.createElement('div');
                                toolbar.id = 'sess-nav-controls';
                                toolbar.style.cssText = 'display:flex; gap:8px; margin:8px 4px; direction:rtl;';
                                toolbar.innerHTML = [
                                    '<button type="button" id="sess-btn-expand-all" style="flex:1; padding:8px 10px; font-size:12px; font-weight:bold; color:#0284c7; background:#e0f2fe; border:1px solid #bae6fd; border-radius:6px; cursor:pointer;">📂 باز کردن همه</button>',
                                    '<button type="button" id="sess-btn-collapse-all" style="flex:1; padding:8px 10px; font-size:12px; font-weight:bold; color:#475569; background:#f1f5f9; border:1px solid #cbd5e1; border-radius:6px; cursor:pointer;">📁 بستن همه</button>'
                                ].join('');
                                parent.insertBefore(toolbar, navList);

                                toolbar.querySelector('#sess-btn-expand-all').addEventListener('click', function(e) {
                                    e.preventDefault();
                                    navList.querySelectorAll('> li').forEach(function(li) {
                                        setGroupState(li, true);
                                    });
                                });

                                toolbar.querySelector('#sess-btn-collapse-all').addEventListener('click', function(e) {
                                    e.preventDefault();
                                    navList.querySelectorAll('> li').forEach(function(li) {
                                        setGroupState(li, false);
                                    });
                                });
                            }

                            var items = navList.querySelectorAll('> li');
                            items.forEach(function(li) {
                                var input = li.querySelector('input[type="checkbox"]');
                                var label = li.querySelector('label');
                                var subList = li.querySelector('ul.group-list, .group-list');

                                if (!label || !subList) return;

                                if (input && input.hasAttribute('hidden')) {
                                    input.removeAttribute('hidden');
                                    input.style.cssText = 'position:absolute!important;opacity:0!important;pointer-events:none!important;width:1px!important;height:1px!important;';
                                }

                                if (label.__sessAccordionAttached) return;
                                label.__sessAccordionAttached = true;

                                var clickHandler = function(e) {
                                    e.preventDefault();
                                    e.stopPropagation();

                                    var isCurrentlyOpen = li.getAttribute('data-open') === 'true';
                                    setGroupState(li, !isCurrentlyOpen);
                                };

                                label.addEventListener('click', clickHandler, true);
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
                    setTimeout(function() { allDocs.forEach(applyAll); }, 800);
                    setTimeout(function() { allDocs.forEach(applyAll); }, 1800);

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

                            var onclickAttr = interactive.getAttribute('onclick') || '';
                            var hrefAttr = interactive.getAttribute('href') || '';
                            
                            sendLog('CLICK', 'کلیک روی <' + tag + (id ? '#' + id : '') + '> ' + (text ? '"' + text + '"' : ''), onclickAttr ? 'onclick: ' + onclickAttr : (hrefAttr ? 'href: ' + hrefAttr : ''));

                            if (onclickAttr && (onclickAttr.indexOf('Perform') !== -1 || onclickAttr.indexOf('Connect2') !== -1)) {
                                sendLog('USER_ACTION', onclickAttr, 'Tag: ' + tag + ', Text: ' + text);
                            }
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
                    // SESS official heartbeat endpoint used in Gnr.js
                    var target = '/sess/Script/AjaxEnvironment.aspx?Act=Date';
                    fetch(target, { method: 'POST', credentials: 'include', cache: 'no-cache' })
                        .then(function(res) {
                            var isOk = res && (res.status === 200 || res.ok);
                            if (window.AndroidBridge && window.AndroidBridge.onHeartbeatAck) {
                                window.AndroidBridge.onHeartbeatAck(isOk);
                            }
                        })
                        .catch(function(err) {
                            fetch(window.location.href, { method: 'HEAD', credentials: 'include', cache: 'no-cache' })
                                .then(function(r) {
                                    if (window.AndroidBridge && window.AndroidBridge.onHeartbeatAck) {
                                        window.AndroidBridge.onHeartbeatAck(r.status === 200 || r.ok);
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
