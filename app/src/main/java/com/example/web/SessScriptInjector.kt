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
     * Compatibility and Menu List Fix:
     * - Fixes hamburger menu ("سه خط و ضربدر") in SfxWeb and responsive pages
     * - Polyfills window.showModalDialog for older ASP.NET university pages
     * - Fixes ASP.NET treeview and expandable right-menu in SESS
     * - Ensures dropdowns and collapsible lists are fully visible and not hidden by zero height or overflow
     */
    fun getCompatibilityAndMenuFixScript(): String {
        return """
            (function() {
                try {
                    if (!window.__sessPolyfillsInjected) {
                        window.__sessPolyfillsInjected = true;

                        // 1. Polyfill window.showModalDialog (Crucial for ASP.NET WebForms lists & reports)
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

                    // Helper to safely append elements without throwing null errors when head/body is null
                    function safeAppend(child, parentNode) {
                        var p = parentNode || document.head || document.documentElement || document.body;
                        if (p && child) {
                            try { p.appendChild(child); } catch(e) {}
                        }
                    }

                    // Responsive Mobile Viewport Adaptor
                    function fixGlobalViewportAndResponsiveFrame() {
                        var isSessPortal = !!document.getElementById('maintbl') || 
                                           (window.location.hostname.includes('sess.shirazu.ac.ir') && (!!document.getElementById('edSRightMenu') || !!document.getElementById('edMiddle')));
                        var vp = document.querySelector('meta[name="viewport"]');
                        if (!vp) {
                            vp = document.createElement('meta');
                            vp.name = 'viewport';
                            safeAppend(vp, document.head);
                        }
                        if (isSessPortal) {
                            // SESS is a legacy 3-column desktop portal (Right Menu 22%, Course Table 54%, Photo/Calendar 24%).
                            // Setting viewport width to 1100 allows all 3 columns to remain side-by-side naturally without breaking layout.
                            vp.setAttribute('content', 'width=1100, initial-scale=0.36, minimum-scale=0.25, maximum-scale=5.0, user-scalable=yes');
                        } else {
                            vp.setAttribute('content', 'width=device-width, initial-scale=1.0, minimum-scale=0.25, maximum-scale=5.0, user-scalable=yes');
                        }

                        if (!document.getElementById('sess-global-frame-css')) {
                            var style = document.createElement('style');
                            style.id = 'sess-global-frame-css';
                            style.innerHTML = [
                                'html, body {',
                                '    font-family: Tahoma, Vazir, Vazirmatn, Arial, sans-serif !important;',
                                '    -webkit-text-size-adjust: 100% !important;',
                                '}',
                                '/* Allow popups, modals, tables, frames to expand naturally */',
                                'iframe, table, form, div[id*="Modal"], div[id*="Popup"], div[class*="modal"], div[class*="popup"] {',
                                '    max-width: none !important;',
                                '    max-height: none !important;',
                                '}'
                            ].join('\n');
                            safeAppend(style, document.head);
                        }
                    }

                    // 3. Fix Hamburger Menu ("سه خط و ضربدر") in SfxWeb (sups.shirazu.ac.ir) and Bootstrap
                    function fixHamburgerAndNavbars() {
                        var toggles = document.querySelectorAll('.navbar-toggle, .navbar-toggler, [data-toggle="collapse"], [data-bs-toggle="collapse"], .sidebar-toggle, .menu-toggle, [class*="nav-toggle"], #icon');
                        toggles.forEach(function(btn) {
                            if (btn.__sessFixed) return;
                            btn.__sessFixed = true;

                            btn.addEventListener('click', function() {
                                var sidenav = document.getElementById('mySidenav') || document.querySelector('.sidenav');
                                if (sidenav) {
                                    if (typeof window.openNav === 'function') {
                                        window.openNav();
                                    }
                                }

                                setTimeout(function() {
                                    var targetSel = btn.getAttribute('data-target') || btn.getAttribute('data-bs-target') || btn.getAttribute('href');
                                    var menu = null;
                                    if (targetSel && targetSel.startsWith('#')) {
                                        menu = document.querySelector(targetSel);
                                    }
                                    if (!menu) {
                                        menu = document.querySelector('.navbar-collapse, #navbar-collapse, .sidebar-menu, #sidebar, .mobile-menu, nav.collapse, ul.collapse');
                                    }
                                    if (!menu) {
                                        var p = btn.closest('.navbar, nav, header, .main-header') || btn.parentElement;
                                        if (p) {
                                            menu = p.querySelector('.collapse, .navbar-collapse, ul, .menu');
                                        }
                                    }

                                    if (menu) {
                                        var isCross = btn.innerHTML.includes('fa-times') || 
                                                      btn.innerHTML.includes('fa-close') || 
                                                      btn.classList.contains('active') ||
                                                      btn.getAttribute('aria-expanded') === 'true' ||
                                                      !btn.classList.contains('collapsed');

                                        var isCurrentlyHidden = menu.style.display === 'none' || 
                                                                menu.offsetHeight === 0 || 
                                                                window.getComputedStyle(menu).display === 'none';

                                        if (isCross || isCurrentlyHidden) {
                                            menu.style.setProperty('display', 'block', 'important');
                                            menu.style.setProperty('height', 'auto', 'important');
                                            menu.style.setProperty('max-height', '85vh', 'important');
                                            menu.style.setProperty('overflow-y', 'auto', 'important');
                                            menu.style.setProperty('visibility', 'visible', 'important');
                                            menu.style.setProperty('opacity', '1', 'important');
                                            menu.style.setProperty('z-index', '999999', 'important');
                                            menu.classList.add('in');
                                            menu.classList.add('show');
                                        } else {
                                            menu.style.setProperty('display', 'none', 'important');
                                            menu.classList.remove('in');
                                            menu.classList.remove('show');
                                        }
                                    }
                                }, 120);
                            });
                        });
                    }

                    // 4. Fix Sidenav and Drawer Lists (such as #mySidenav in SfxWeb / sups.shirazu.ac.ir)
                    function fixSidenavAndDrawers() {
                        var sidenav = document.getElementById('mySidenav') || document.querySelector('.sidenav, #sidebar-wrapper, [class*="sidenav"]');
                        if (!sidenav) return;

                        // Ensure global openNav & closeNav exist and work reliably on the real site drawer
                        window.openNav = function() {
                            var s = document.getElementById('mySidenav') || document.querySelector('.sidenav, #sidebar-wrapper, [class*="sidenav"]');
                            if (s) {
                                s.style.setProperty('width', 'min(300px, 85vw)', 'important');
                                s.style.setProperty('display', 'block', 'important');
                                s.style.setProperty('visibility', 'visible', 'important');
                                s.style.setProperty('opacity', '1', 'important');
                                s.style.setProperty('z-index', '9999999', 'important');
                                s.style.setProperty('position', 'fixed', 'important');
                                s.style.setProperty('top', '0', 'important');
                                s.style.setProperty('right', '0', 'important');
                                s.style.setProperty('height', '100%', 'important');
                                s.setAttribute('data-state', 'open');
                                var backdrop = document.getElementById('sess-sidenav-backdrop');
                                if (backdrop) backdrop.style.setProperty('display', 'block', 'important');
                            }
                        };

                        window.closeNav = function() {
                            var s = document.getElementById('mySidenav') || document.querySelector('.sidenav, #sidebar-wrapper, [class*="sidenav"]');
                            if (s) {
                                s.style.setProperty('width', '0', 'important');
                                s.setAttribute('data-state', 'closed');
                                var backdrop = document.getElementById('sess-sidenav-backdrop');
                                if (backdrop) backdrop.style.setProperty('display', 'none', 'important');
                            }
                        };

                        // Inject enhanced CSS for sidenav drawers
                        if (!document.getElementById('sess-sidenav-css')) {
                            var style = document.createElement('style');
                            style.id = 'sess-sidenav-css';
                            style.innerHTML = [
                                '.sidenav, #mySidenav {',
                                '    position: fixed !important;',
                                '    top: 0 !important;',
                                '    right: 0 !important;',
                                '    height: 100% !important;',
                                '    z-index: 9999999 !important;',
                                '    background: #1e293b !important;',
                                '    box-shadow: -4px 0 25px rgba(0,0,0,0.5) !important;',
                                '    overflow-x: hidden !important;',
                                '    overflow-y: auto !important;',
                                '    -webkit-overflow-scrolling: touch !important;',
                                '    transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;',
                                '    direction: rtl !important;',
                                '    text-align: right !important;',
                                '}',
                                '.sidenav a, #mySidenav a {',
                                '    display: flex !important;',
                                '    align-items: center !important;',
                                '    min-height: 48px !important;',
                                '    padding: 12px 18px !important;',
                                '    font-size: 15px !important;',
                                '    font-weight: 600 !important;',
                                '    color: #f1f5f9 !important;',
                                '    text-decoration: none !important;',
                                '    border-bottom: 1px solid rgba(255,255,255,0.08) !important;',
                                '    cursor: pointer !important;',
                                '    transition: background-color 0.15s ease !important;',
                                '    user-select: none !important;',
                                '    -webkit-user-select: none !important;',
                                '}',
                                '.sidenav a:active, #mySidenav a:active {',
                                '    background-color: #334155 !important;',
                                '    color: #38bdf8 !important;',
                                '}',
                                '.sidenav a.closebtn, #mySidenav a.closebtn {',
                                '    font-size: 30px !important;',
                                '    color: #ef4444 !important;',
                                '    justify-content: flex-end !important;',
                                '    padding: 10px 18px !important;',
                                '    min-height: 44px !important;',
                                '    border-bottom: none !important;',
                                '}',
                                '.sidenav img.menuIcon, #mySidenav img.menuIcon {',
                                '    width: 24px !important;',
                                '    height: 24px !important;',
                                '    margin-left: 12px !important;',
                                '    object-fit: contain !important;',
                                '}',
                                '.sidenav hr, #mySidenav hr {',
                                '    border: none !important;',
                                '    border-top: 1px solid rgba(255,255,255,0.12) !important;',
                                '    margin: 6px 0 !important;',
                                '}',
                                '#sess-sidenav-backdrop {',
                                '    position: fixed;',
                                '    top: 0;',
                                '    left: 0;',
                                '    width: 100vw;',
                                '    height: 100vh;',
                                '    background: rgba(15, 23, 42, 0.6);',
                                '    backdrop-filter: blur(2px);',
                                '    z-index: 9999998;',
                                '    display: none;',
                                '}'
                            ].join('\n');
                            safeAppend(style, document.head);
                        }

                        // Create backdrop if needed
                        var backdrop = document.getElementById('sess-sidenav-backdrop');
                        if (!backdrop) {
                            backdrop = document.createElement('div');
                            backdrop.id = 'sess-sidenav-backdrop';
                            backdrop.addEventListener('click', function() {
                                window.closeNav();
                            });
                            safeAppend(backdrop, document.body);
                        }

                        // Attach close button handler
                        var closeBtns = sidenav.querySelectorAll('.closebtn, [class*="closebtn"], #navOpen');
                        closeBtns.forEach(function(cb) {
                            if (cb.__sessCloseAttached) return;
                            cb.__sessCloseAttached = true;
                            cb.addEventListener('click', function(e) {
                                e.preventDefault();
                                e.stopPropagation();
                                window.closeNav();
                            });
                        });

                        // Wire up action links inside sidenav (like pbcw, pbcL, pbc, pr, MainPage)
                        var actionLinks = sidenav.querySelectorAll('a');
                        actionLinks.forEach(function(a) {
                            if (a.classList.contains('closebtn')) return;
                            if (a.__sessActionAttached) return;
                            a.__sessActionAttached = true;
                            a.addEventListener('click', function() {
                                setTimeout(function() {
                                    window.closeNav();
                                }, 350);
                            });
                        });

                        // Wire up any page elements that trigger opening the drawer
                        var openTriggers = document.querySelectorAll('#navOpen:not(.closebtn), [onclick*="openNav"], .openbtn, [class*="openbtn"], [id*="openNav"], #icon');
                        openTriggers.forEach(function(trigger) {
                            if (trigger.__sessOpenAttached) return;
                            trigger.__sessOpenAttached = true;
                            trigger.addEventListener('click', function(e) {
                                e.preventDefault();
                                window.openNav();
                            });
                        });
                    }

                    // 5. Fix SESS Right-Menu (#edSRightMenu, treeviews, submenus)
                    function fixSessRightMenuAndDropdowns() {
                        var rightMenu = document.querySelector('#edSRightMenu');
                        if (rightMenu) {
                            rightMenu.style.setProperty('overflow', 'visible', 'important');
                            rightMenu.style.setProperty('display', 'table-cell', 'important');
                            rightMenu.style.setProperty('visibility', 'visible', 'important');
                            rightMenu.style.setProperty('opacity', '1', 'important');
                            rightMenu.style.setProperty('height', 'auto', 'important');
                            rightMenu.style.setProperty('max-height', 'none', 'important');
                            rightMenu.style.setProperty('vertical-align', 'top', 'important');

                            var parent = rightMenu.parentElement;
                            while (parent && parent !== document.body) {
                                if (parent.tagName === 'TR') {
                                    parent.style.setProperty('display', 'table-row', 'important');
                                } else if (parent.tagName === 'TABLE' || parent.tagName === 'TBODY') {
                                    parent.style.setProperty('display', parent.tagName === 'TBODY' ? 'row-group' : 'table', 'important');
                                } else if (parent.tagName === 'TD' || parent.tagName === 'TH') {
                                    parent.style.setProperty('display', 'table-cell', 'important');
                                } else {
                                    parent.style.setProperty('display', 'block', 'important');
                                }
                                parent.style.setProperty('height', 'auto', 'important');
                                parent.style.setProperty('max-height', 'none', 'important');
                                parent.style.setProperty('overflow', 'visible', 'important');
                                if (parent.hasAttribute('height')) parent.removeAttribute('height');
                                parent = parent.parentElement;
                            }
                        }

                        // Fix any dropdowns toggled by click
                        var ddtoggles = document.querySelectorAll('.dropdown-toggle, [data-toggle="dropdown"], [data-bs-toggle="dropdown"]');
                        ddtoggles.forEach(function(dt) {
                            if (dt.__sessFixed) return;
                            dt.__sessFixed = true;
                            dt.addEventListener('click', function() {
                                setTimeout(function() {
                                    var parent = dt.parentElement;
                                    if (parent) {
                                        var dm = parent.querySelector('.dropdown-menu');
                                        if (dm) {
                                            dm.style.setProperty('display', 'block', 'important');
                                            dm.style.setProperty('visibility', 'visible', 'important');
                                            dm.style.setProperty('opacity', '1', 'important');
                                            dm.style.setProperty('z-index', '999999', 'important');
                                            dm.style.setProperty('height', 'auto', 'important');
                                            dm.style.setProperty('max-height', 'none', 'important');
                                        }
                                    }
                                }, 80);
                            });
                        });
                    }

                    // 6. Fix SESS Main Navigation List across all documents/frames
                    function fixSessNavList() {
                        var docs = [document];
                        try {
                            var frames = document.querySelectorAll('iframe, frame');
                            for (var i = 0; i < frames.length; i++) {
                                try {
                                    var fDoc = frames[i].contentDocument || (frames[i].contentWindow && frames[i].contentWindow.document);
                                    if (fDoc && docs.indexOf(fDoc) === -1) {
                                        docs.push(fDoc);
                                    }
                                } catch(e) {}
                            }
                        } catch(e) {}

                        docs.forEach(function(doc) {
                            try {
                                if (!doc.getElementById('sess-navlist-css-v10')) {
                                    var style = doc.createElement('style');
                                    style.id = 'sess-navlist-css-v10';
                                    style.innerHTML = [
                                        '/* Ensure SESS Right Menu column is clean, visible and properly sized */',
                                        '#edSRightMenu, td#edSRightMenu {',
                                        '    display: table-cell !important;',
                                        '    visibility: visible !important;',
                                        '    opacity: 1 !important;',
                                        '    width: 22% !important;',
                                        '    min-width: 220px !important;',
                                        '    vertical-align: top !important;',
                                        '}',
                                        '/* Ensure menu containers are never collapsed or clipped */',
                                        '.baghdi, .baghdi.scroll, nav.nav, nav.header, ul.nav__list {',
                                        '    display: block !important;',
                                        '    visibility: visible !important;',
                                        '    opacity: 1 !important;',
                                        '    width: 100% !important;',
                                        '    height: auto !important;',
                                        '    max-height: none !important;',
                                        '    overflow: visible !important;',
                                        '    list-style: none !important;',
                                        '    margin: 0 !important;',
                                        '    padding: 0 !important;',
                                        '}',
                                        '/* Top-level menu categories (اطلاعات کاربری، پیامها، آموزشی، ...) */',
                                        'ul.nav__list > li {',
                                        '    display: block !important;',
                                        '    visibility: visible !important;',
                                        '    opacity: 1 !important;',
                                        '    margin-bottom: 6px !important;',
                                        '    clear: both !important;',
                                        '    width: 100% !important;',
                                        '    position: relative !important;',
                                        '}',
                                        'ul.nav__list label {',
                                        '    display: flex !important;',
                                        '    align-items: center !important;',
                                        '    justify-content: space-between !important;',
                                        '    width: 100% !important;',
                                        '    cursor: pointer !important;',
                                        '    padding: 10px 12px !important;',
                                        '    background-color: #f0f9ff !important;',
                                        '    border: 1px solid #bae6fd !important;',
                                        '    border-radius: 8px !important;',
                                        '    font-weight: bold !important;',
                                        '    user-select: none !important;',
                                        '    -webkit-user-select: none !important;',
                                        '    color: #0284c7 !important;',
                                        '    font-size: 13px !important;',
                                        '    box-sizing: border-box !important;',
                                        '}',
                                        'ul.nav__list label.has-sublist::after {',
                                        '    content: "▼" !important;',
                                        '    font-size: 10px !important;',
                                        '    margin-left: 6px !important;',
                                        '    opacity: 0.7 !important;',
                                        '}',
                                        'ul.nav__list label.has-sublist.is-expanded::after {',
                                        '    content: "▲" !important;',
                                        '}',
                                        '/* Keep input stateful for CSS sibling selector without native checkbox visual artifact */',
                                        'ul.nav__list input[type="checkbox"] {',
                                        '    position: absolute !important;',
                                        '    opacity: 0 !important;',
                                        '    width: 1px !important;',
                                        '    height: 1px !important;',
                                        '    pointer-events: none !important;',
                                        '}',
                                        '/* Sub-lists: default hidden, visible when checkbox is checked OR class is-expanded */',
                                        'ul.nav__list ul.group-list, ul.nav__list .group-list {',
                                        '    display: none;',
                                        '    list-style: none !important;',
                                        '    padding: 4px 6px 4px 12px !important;',
                                        '    margin: 4px 0 !important;',
                                        '}',
                                        'ul.nav__list input[type="checkbox"]:checked ~ ul.group-list,',
                                        'ul.nav__list input[type="checkbox"]:checked ~ .group-list,',
                                        'ul.nav__list ul.group-list.is-expanded,',
                                        'ul.nav__list .group-list.is-expanded {',
                                        '    display: block !important;',
                                        '    visibility: visible !important;',
                                        '    opacity: 1 !important;',
                                        '}',
                                        'ul.nav__list ul.group-list li {',
                                        '    margin: 4px 0 !important;',
                                        '    display: block !important;',
                                        '    visibility: visible !important;',
                                        '    opacity: 1 !important;',
                                        '}',
                                        'ul.nav__list ul.group-list a.link, ul.nav__list a.link {',
                                        '    display: block !important;',
                                        '    visibility: visible !important;',
                                        '    min-height: 38px !important;',
                                        '    line-height: 20px !important;',
                                        '    padding: 8px 12px !important;',
                                        '    font-size: 12px !important;',
                                        '    color: #0f172a !important;',
                                        '    text-decoration: none !important;',
                                        '    background: #ffffff !important;',
                                        '    border-radius: 6px !important;',
                                        '    border: 1px solid #cbd5e1 !important;',
                                        '    font-weight: 500 !important;',
                                        '    box-sizing: border-box !important;',
                                        '    cursor: pointer !important;',
                                        '}',
                                        'ul.nav__list ul.group-list a.link:active {',
                                        '    background: #e0f2fe !important;',
                                        '}'
                                    ].join('\n');
                                    safeAppend(style, doc.head);
                                }

                                var navLists = doc.querySelectorAll('.nav__list, ul.nav__list');
                                navLists.forEach(function(navList) {
                                    var groupLis = navList.querySelectorAll('> li, li');
                                    groupLis.forEach(function(li) {
                                        var input = li.querySelector('input[type="checkbox"], input[id^="group-"]');
                                        var label = li.querySelector('label');
                                        var subList = li.querySelector('ul.group-list, .group-list');

                                        if (input) {
                                            input.removeAttribute('hidden');
                                        }

                                        if (label && subList) {
                                            label.classList.add('has-sublist');

                                            if (!label.__sessNavListAttachedV10) {
                                                label.__sessNavListAttachedV10 = true;

                                                label.addEventListener('click', function(e) {
                                                    var isCurrentlyExpanded = subList.classList.contains('is-expanded') || 
                                                                             subList.style.display === 'block' || 
                                                                             (input && input.checked);

                                                    if (isCurrentlyExpanded) {
                                                        subList.classList.remove('is-expanded');
                                                        subList.style.setProperty('display', 'none', 'important');
                                                        label.classList.remove('is-expanded');
                                                        if (input) input.checked = false;
                                                    } else {
                                                        subList.classList.add('is-expanded');
                                                        subList.style.setProperty('display', 'block', 'important');
                                                        subList.style.setProperty('visibility', 'visible', 'important');
                                                        subList.style.setProperty('opacity', '1', 'important');
                                                        label.classList.add('is-expanded');
                                                        if (input) input.checked = true;
                                                    }
                                                }, false);
                                            }
                                        }
                                    });
                                });

                                // Fallback: If right-menu cell exists but has no navigation structure, inject complete static navigation
                                var rightMenu = doc.querySelector('#edSRightMenu');
                                if (rightMenu && !rightMenu.querySelector('.nav__list, ul.nav__list, #sess-fallback-nav')) {
                                    var fallbackContainer = doc.createElement('div');
                                    fallbackContainer.id = 'sess-fallback-nav';
                                    fallbackContainer.style.cssText = 'padding: 8px; direction: rtl; text-align: right; width: 100%;';
                                    fallbackContainer.innerHTML = buildStaticSessNavHtml();
                                    rightMenu.appendChild(fallbackContainer);
                                }
                            } catch(e) {}
                        });
                    }

                    window.runSessCmd = function(cmdStr) {
                        var modal = document.getElementById('sess-floating-menu-modal');
                        if (modal) modal.style.display = 'none';

                        try { eval(cmdStr); return; } catch(e) {}

                        try {
                            var frames = document.querySelectorAll('iframe, frame');
                            for (var i = 0; i < frames.length; i++) {
                                try {
                                    var win = frames[i].contentWindow;
                                    if (win) { win.eval(cmdStr); return; }
                                } catch(e) {}
                            }
                        } catch(e) {}
                    };

                    function buildStaticSessNavHtml() {
                        var categories = [
                            {
                                title: "👤 اطلاعات کاربری",
                                links: [
                                    { text: "تغییر کلمه رمز", script: "PerformStd('Cgp')" },
                                    { text: "اطلاعات پایه", script: "Perform('SignUp')" },
                                    { text: "اطلاعات افزوده", script: "PerformStd('Ovs')" },
                                    { text: "ارسال مدارک", script: "PerformStd('Sdc')" },
                                    { text: "مشخصات دانشجویی", script: "Perform('BasicInfo')" },
                                    { text: "خطاهای سیستم", script: "PerformStd('ErrRep')" },
                                    { text: "پرونده دیجیتال", script: "PerformStd('SDGF')" },
                                    { text: "اطلاعات کنکور", script: "PerformStd('Ssd')" },
                                    { text: "آیین نامه‌ها", script: "PerformStd('Reg')" },
                                    { text: "تابلو احکام جدید", script: "PerformStd('Amt')" },
                                    { text: "دریافت رمز اولیه", script: "PerformStd('KeyG')" },
                                    { text: "تاریخچه ورود", script: "PerformStd('Vlg')" },
                                    { text: "انتخاب کاربر پیش‌فرض SSO", script: "PerformStd('SDEF')" }
                                ]
                            },
                            {
                                title: "✉️ پیام‌ها",
                                links: [
                                    { text: "پیام‌ها", script: "PerformStd('Msg')" },
                                    { text: "پیام به استاد مشاور", script: "Perform('Msg2Tch')" },
                                    { text: "پیام به کارشناس بخش", script: "Perform('Msg2Exp')" }
                                ]
                            },
                            {
                                title: "🎓 آموزشی",
                                links: [
                                    { text: "برنامه درسی", script: "Perform('Major')" },
                                    { text: "دروس جبرانی", script: "Perform('Compensate')" },
                                    { text: "لیست دروس گرفته", script: "Perform('Som')" },
                                    { text: "نمودار پیشرفت تحصیلی", script: "PerformStd('StdProgress')" },
                                    { text: "خلاصه کارنامه", script: "PerformStd('Sum')" },
                                    { text: "چک‌لیست ثبت نام مقدماتی", script: "PerformStd('Prc')" },
                                    { text: "چک‌لیست انتخاب واحد", script: "PerformStd('Rcf')" },
                                    { text: "عملیات‌های ثبت نام", script: "Perform('RegLog')" },
                                    { text: "فارغ‌التحصیلی", script: "PerformStd('Sgr')" },
                                    { text: "برنامه کلاسی نیمسال", script: "PerformStd('Pcl')" },
                                    { text: "تقویم آموزشی", script: "PerformStd('Ssr')" },
                                    { text: "سوابق تحصیلی انتقالی", script: "PerformStd('See')" },
                                    { text: "آزمون‌های معافی", script: "PerformStd('Exl')" },
                                    { text: "امور دستیار استاد", script: "Perform('ExamTA')" },
                                    { text: "جلسات مشاوره", script: "PerformStd('StdCons')" }
                                ]
                            },
                            {
                                title: "✅ ارزیابی",
                                links: [
                                    { text: "تکمیل فرم‌های ارزیابی", script: "PerformStd('ActiveEvl')" }
                                ]
                            },
                            {
                                title: "🏨 امور دانشجویی",
                                links: [
                                    { text: "خوابگاه", script: "PerformStd('Dst')" },
                                    { text: "فرم هم‌اتاقی", script: "Perform('DormAgent')" },
                                    { text: "خوابگاه ورودی‌های جدید", script: "Perform('DormitoryZero')" },
                                    { text: "درخواست وام", script: "PerformStd('Erl')" },
                                    { text: "انتخابات دانشجویی", script: "PerformStd('Evt')" },
                                    { text: "خرید ژتون و رفاهی", script: "Perform('SfxChip')" },
                                    { text: "ثبت نام مراسم فارغ‌التحصیلی", script: "PerformStd('GRDSTD')" },
                                    { text: "شبکه آزمایشگاهی دانشگاه", script: "PerformStd('LabsView')" },
                                    { text: "درخواست‌های اسکان متفرقه", script: "PerformStd('NewRoomerReqs')" },
                                    { text: "درخواست‌های نوبت‌دهی آزمایشگاه", script: "Perform('LabServReqs')" }
                                ]
                            },
                            {
                                title: "💳 امور مالی",
                                links: [
                                    { text: "پرداخت شهریه اینترنتی", script: "Perform('IntPy')" },
                                    { text: "لیست پرداخت‌ها", script: "PerformStd('Scr')" },
                                    { text: "پرداخت‌های اینترنتی", script: "PerformStd('Psp')" },
                                    { text: "چک‌های تقسیطی", script: "PerformStd('Psc')" },
                                    { text: "چک‌لیست مالی نیمسال", script: "PerformStd('Sfc')" },
                                    { text: "جدول شهریه", script: "Perform('AccTable')" },
                                    { text: "شهریه", script: "Perform('Pst')" },
                                    { text: "شهریه رایگان", script: "PerformStd('Frt')" },
                                    { text: "بدهی‌های موضوعی", script: "PerformStd('StdDebPay')" },
                                    { text: "حق‌التدریس دستیار آموزشی", script: "PerformStd('HSC')" }
                                ]
                            },
                            {
                                title: "🔄 فرآیندها",
                                links: [
                                    { text: "فرآیند ثبت نام", script: "Perform('Spr')" },
                                    { text: "منابع و برنامه‌ها", script: "PerformStd('Rss')" },
                                    { text: "فرآیندهای دانشجویی", script: "PerformStd('Ssi')" },
                                    { text: "نوبت‌های مراجعه", script: "PerformStd('Ats')" },
                                    { text: "فرآیند ثبت نام (جدید)", script: "Perform('RegStdProc')" }
                                ]
                            },
                            {
                                title: "🏛️ امور فرهنگی",
                                links: [
                                    { text: "امور فرهنگی", script: "PerformStd('CLE')" }
                                ]
                            },
                            {
                                title: "💻 امور واحدهای مجازی",
                                links: [
                                    { text: "کتابخانه دیجیتال", script: "PerformStd('DgtL')" },
                                    { text: "گفتگو با کارشناس بخش", script: "if (window.Connect2EduExpert) Connect2EduExpert();" },
                                    { text: "گفتگو با کارشناس حسابداری", script: "if (window.Connect2CalExpert) Connect2CalExpert();" }
                                ]
                            }
                        ];

                        var html = '';
                        categories.forEach(function(cat) {
                            html += '<div style="margin-bottom:12px;">';
                            html += '<div style="font-weight:bold; padding:8px 12px; background:#f0f9ff; color:#0369a1; border-radius:8px; margin-bottom:6px; font-size:14px;">' + cat.title + '</div>';
                            html += '<div style="padding-right:8px;">';
                            cat.links.forEach(function(link) {
                                var safeScript = link.script.replace(/'/g, "\\'");
                                html += '<a href="javascript:void(0)" onclick="runSessCmd(\'' + safeScript + '\')" style="display:block; padding:10px 14px; margin:4px 0; background:#ffffff; border:1px solid #cbd5e1; border-radius:8px; color:#0284c7; text-decoration:none; font-size:13px; font-weight:bold;">' + link.text + '</a>';
                            });
                            html += '</div></div>';
                        });

                        return html;
                    }

                    // Initial execution
                    fixGlobalViewportAndResponsiveFrame();
                    fixHamburgerAndNavbars();
                    fixSidenavAndDrawers();
                    fixSessRightMenuAndDropdowns();
                    fixSessNavList();

                    // Periodic checks to catch delayed ASP.NET WebForms DOM elements or postbacks
                    var checkCount = 0;
                    var intervalTimer = setInterval(function() {
                        checkCount++;
                        fixGlobalViewportAndResponsiveFrame();
                        fixSessRightMenuAndDropdowns();
                        fixSessNavList();
                        if (checkCount >= 6) clearInterval(intervalTimer);
                    }, 500);

                    // Debounced DOM Observer to prevent infinite loops while adapting to AJAX changes
                    var debounceTimer = null;
                    var observer = new MutationObserver(function() {
                        if (debounceTimer) clearTimeout(debounceTimer);
                        debounceTimer = setTimeout(function() {
                            fixGlobalViewportAndResponsiveFrame();
                            fixHamburgerAndNavbars();
                            fixSidenavAndDrawers();
                            fixSessRightMenuAndDropdowns();
                            fixSessNavList();
                        }, 250);
                    });
                    if (document.body) {
                        observer.observe(document.body, { childList: true, subtree: true });
                    }
                } catch(e) {
                    console.error("Compat script error", e);
                }
            })();
        """.trimIndent()
    }

    /**
     * Safe styling: NO layout alterations, NO hiding elements, NO restructuring.
     * Keeps 100% of website content, tables, and buttons exactly as designed.
     * Only applies dark mode filter when enabled.
     */
    fun getMobileOptimizationScript(darkMode: Boolean): String {
        if (!darkMode) {
            return """
                (function() {
                    var existingStyle = document.getElementById('sess-plus-app-style');
                    if (existingStyle && existingStyle.parentNode) {
                        existingStyle.parentNode.removeChild(existingStyle);
                    }
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
     * Comprehensive Debug & Action Logger
     * Captures Clicks (top & iframes), ASP.NET __doPostBack, Forms, and URL changes.
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
                            referrer: document.referrer,
                            hasDoPostBack: typeof window.__doPostBack === 'function',
                            totalForms: document.forms.length,
                            totalLinks: document.links.length,
                            totalFrames: document.querySelectorAll('iframe, frame').length
                        }
                    );

                    // 2. Intercept ASP.NET __doPostBack (CRITICAL for Iranian University Portals)
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
                    setInterval(function() { hookPostBack(window); }, 1500);

                    // 3. Document-wide Click Interceptor (Non-blocking)
                    function attachClickTracker(doc, contextPrefix) {
                        doc.addEventListener('click', function(e) {
                            try {
                                var el = e.target;
                                if (!el) return;
                                
                                var interactive = el.closest('a, button, input, [onclick], tr[onclick], td[onclick], div.small-box, .menu-item, li, [data-toggle], [data-bs-toggle]') || el;
                                
                                var tag = (interactive.tagName || '').toLowerCase();
                                var id = interactive.id || '';
                                var name = interactive.getAttribute('name') || '';
                                var className = typeof interactive.className === 'string' ? interactive.className.trim() : (interactive.className?.baseVal || '');
                                var text = (interactive.innerText || interactive.value || interactive.getAttribute('title') || interactive.getAttribute('aria-label') || '').trim();
                                if (text.length > 80) text = text.substring(0, 80) + '...';
                                var href = interactive.getAttribute('href') || '';
                                var onclick = interactive.getAttribute('onclick') || '';
                                
                                var info = {
                                    context: contextPrefix || 'TOP',
                                    element: tag + (id ? '#' + id : '') + (name ? '[name="' + name + '"]' : ''),
                                    tag: tag,
                                    id: id,
                                    name: name,
                                    className: className,
                                    text: text,
                                    href: href,
                                    onclick: onclick,
                                    pageUrl: window.location.href
                                };

                                if (interactive.form) {
                                    info.formAction = interactive.form.action;
                                    info.formMethod = interactive.form.method;
                                }

                                var summary = (contextPrefix ? '[' + contextPrefix + '] ' : '') + 'کلیک روی <' + tag + (id ? '#' + id : '') + '> ' + (text ? '"' + text + '"' : '');
                                if (href) summary += ' (href: ' + href.substring(0, 50) + ')';
                                else if (onclick) summary += ' (onclick: ' + onclick.substring(0, 50) + ')';

                                sendLog('CLICK', summary, info);
                            } catch(err) {}
                        }, false);

                        doc.addEventListener('submit', function(e) {
                            try {
                                var form = e.target;
                                if (!form) return;
                                
                                var fieldSummary = [];
                                var inputs = form.querySelectorAll('input, select, textarea');
                                for (var i = 0; i < inputs.length; i++) {
                                    var inp = inputs[i];
                                    var n = inp.name || inp.id;
                                    if (n && !n.includes('VIEWSTATE') && !n.includes('EVENTVALIDATION')) {
                                        fieldSummary.push(n + '=' + (inp.value || ''));
                                    }
                                }

                                sendLog(
                                    'FORM_SUBMIT',
                                    'ارسال فرم: ' + (form.id || form.name || form.action || 'بی‌نام'),
                                    {
                                        context: contextPrefix || 'TOP',
                                        action: form.action,
                                        method: form.method || 'GET',
                                        fields: fieldSummary.slice(0, 15),
                                        pageUrl: window.location.href
                                    }
                                );
                            } catch(err) {}
                        }, false);
                    }

                    attachClickTracker(document, 'MAIN');

                    // 4. Also track child frames & iframes (SESS often uses frames!)
                    function checkChildFrames() {
                        try {
                            var frames = document.querySelectorAll('iframe, frame');
                            for (var i = 0; i < frames.length; i++) {
                                try {
                                    var frame = frames[i];
                                    var fDoc = frame.contentDocument || (frame.contentWindow && frame.contentWindow.document);
                                    if (fDoc && !fDoc.__sessTrackerAttached) {
                                        fDoc.__sessTrackerAttached = true;
                                        var frameName = frame.id || frame.name || ('Frame_' + i);
                                        attachClickTracker(fDoc, frameName);
                                        hookPostBack(frame.contentWindow);
                                    }
                                } catch(crossErr) {}
                            }
                        } catch(e) {}
                    }
                    checkChildFrames();
                    setInterval(checkChildFrames, 2000);

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
