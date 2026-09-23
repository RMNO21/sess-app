package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppSettings
import com.example.model.CustomShortcut
import com.example.model.UserCredentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sess_shirazu_prefs", Context.MODE_PRIVATE)

    private val _credentials = MutableStateFlow(loadCredentials())
    val credentials: StateFlow<UserCredentials> = _credentials.asStateFlow()

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadCredentials(): UserCredentials {
        val user = prefs.getString(KEY_USERNAME, "") ?: ""
        val pass = prefs.getString(KEY_PASSWORD, "") ?: ""
        val autoLogin = prefs.getBoolean(KEY_AUTO_LOGIN, true)
        val autoSubmit = prefs.getBoolean(KEY_AUTO_SUBMIT, true)
        val lastLogin = prefs.getLong(KEY_LAST_LOGIN, 0L)
        val isSaved = user.isNotEmpty() && pass.isNotEmpty()
        return UserCredentials(
            username = user,
            password = pass,
            isAutoLoginEnabled = autoLogin,
            isAutoSubmitEnabled = autoSubmit,
            isSaved = isSaved,
            lastLoginTime = lastLogin
        )
    }

    private fun loadSettings(): AppSettings {
        return AppSettings(
            isMobileOptimizationEnabled = prefs.getBoolean(KEY_MOBILE_OPT, true),
            isDesktopMode = prefs.getBoolean(KEY_DESKTOP_MODE, false),
            isDarkModeReading = prefs.getBoolean(KEY_DARK_MODE, false),
            isKeepAliveEnabled = prefs.getBoolean(KEY_KEEP_ALIVE, true),
            keepAliveIntervalMinutes = prefs.getInt(KEY_KEEP_ALIVE_INTERVAL, 2),
            autoBypassCaptchaOnDetect = prefs.getBoolean(KEY_AUTO_BYPASS_CAPTCHA, false),
            textZoomPercent = prefs.getInt(KEY_TEXT_ZOOM, 100)
        )
    }

    fun saveCredentials(
        username: String,
        pass: String,
        autoLogin: Boolean = true,
        autoSubmit: Boolean = true
    ) {
        prefs.edit().apply {
            putString(KEY_USERNAME, username.trim())
            putString(KEY_PASSWORD, pass)
            putBoolean(KEY_AUTO_LOGIN, autoLogin)
            putBoolean(KEY_AUTO_SUBMIT, autoSubmit)
            putLong(KEY_LAST_LOGIN, System.currentTimeMillis())
            apply()
        }
        _credentials.value = loadCredentials()
    }

    fun clearCredentials() {
        prefs.edit().apply {
            remove(KEY_USERNAME)
            remove(KEY_PASSWORD)
            remove(KEY_LAST_LOGIN)
            apply()
        }
        _credentials.value = loadCredentials()
    }

    fun updateAutoLogin(enabled: Boolean, autoSubmit: Boolean) {
        prefs.edit().apply {
            putBoolean(KEY_AUTO_LOGIN, enabled)
            putBoolean(KEY_AUTO_SUBMIT, autoSubmit)
            apply()
        }
        _credentials.value = loadCredentials()
    }

    fun saveSettings(newSettings: AppSettings) {
        prefs.edit().apply {
            putBoolean(KEY_MOBILE_OPT, newSettings.isMobileOptimizationEnabled)
            putBoolean(KEY_DESKTOP_MODE, newSettings.isDesktopMode)
            putBoolean(KEY_DARK_MODE, newSettings.isDarkModeReading)
            putBoolean(KEY_KEEP_ALIVE, newSettings.isKeepAliveEnabled)
            putInt(KEY_KEEP_ALIVE_INTERVAL, newSettings.keepAliveIntervalMinutes)
            putBoolean(KEY_AUTO_BYPASS_CAPTCHA, newSettings.autoBypassCaptchaOnDetect)
            putInt(KEY_TEXT_ZOOM, newSettings.textZoomPercent)
            apply()
        }
        _settings.value = newSettings
    }

    fun loadCustomShortcuts(): List<CustomShortcut> {
        val json = prefs.getString(KEY_CUSTOM_SHORTCUTS, null)
        if (json == null) {
            val initial = listOf(
                CustomShortcut(
                    id = "default_sess_home",
                    name = "سامانه سس (صفحه اصلی)",
                    targetUrl = "https://sess.shirazu.ac.ir",
                    actionScript = ""
                )
            )
            saveCustomShortcuts(initial)
            return initial
        }
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<CustomShortcut>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CustomShortcut(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = obj.getString("name"),
                        targetUrl = obj.getString("targetUrl"),
                        actionScript = obj.optString("actionScript", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            if (list.isEmpty()) {
                val initial = listOf(
                    CustomShortcut(
                        id = "default_sess_home",
                        name = "سامانه سس (صفحه اصلی)",
                        targetUrl = "https://sess.shirazu.ac.ir",
                        actionScript = ""
                    )
                )
                saveCustomShortcuts(initial)
                initial
            } else {
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCustomShortcuts(shortcuts: List<CustomShortcut>) {
        val array = JSONArray()
        for (s in shortcuts) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("targetUrl", s.targetUrl)
                put("actionScript", s.actionScript)
                put("createdAt", s.createdAt)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_CUSTOM_SHORTCUTS, array.toString()).apply()
    }

    fun updateCustomShortcut(updated: CustomShortcut) {
        val current = loadCustomShortcuts().map {
            if (it.id == updated.id) updated else it
        }
        saveCustomShortcuts(current)
    }

    companion object {
        private const val KEY_USERNAME = "sess_username"
        private const val KEY_PASSWORD = "sess_password"
        private const val KEY_AUTO_LOGIN = "sess_auto_login"
        private const val KEY_AUTO_SUBMIT = "sess_auto_submit"
        private const val KEY_LAST_LOGIN = "sess_last_login"

        private const val KEY_MOBILE_OPT = "setting_mobile_opt"
        private const val KEY_DESKTOP_MODE = "setting_desktop_mode"
        private const val KEY_DARK_MODE = "setting_dark_mode"
        private const val KEY_KEEP_ALIVE = "setting_keep_alive"
        private const val KEY_KEEP_ALIVE_INTERVAL = "setting_keep_alive_interval"
        private const val KEY_AUTO_BYPASS_CAPTCHA = "setting_auto_bypass_captcha"
        private const val KEY_TEXT_ZOOM = "setting_text_zoom"
        private const val KEY_CUSTOM_SHORTCUTS = "setting_custom_shortcuts"
    }
}
