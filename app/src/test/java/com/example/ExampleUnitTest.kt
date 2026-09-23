package com.example

import com.example.model.AppSettings
import com.example.model.SessionState
import com.example.model.UserCredentials
import com.example.web.SessScriptInjector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testDomainFilteringLogic() {
        val shirazuDomains = listOf(
            "sess.shirazu.ac.ir",
            "shirazu.ac.ir",
            "vru.shirazu.ac.ir",
            "su.shirazu.ac.ir",
            "sama.shirazu.ac.ir",
            "mail.shirazu.ac.ir"
        )
        val externalDomains = listOf(
            "google.com",
            "instagram.com",
            "shirazu.ac.ir.fake.com",
            "example.com"
        )

        for (host in shirazuDomains) {
            val shouldStayInside = host.endsWith("shirazu.ac.ir") || host == "shirazu.ac.ir"
            assertTrue("Domain $host should stay inside app", shouldStayInside)
        }

        for (host in externalDomains) {
            val shouldStayInside = host.endsWith(".shirazu.ac.ir") || host == "shirazu.ac.ir"
            assertFalse("Domain $host should be treated as external", shouldStayInside)
        }
    }

    @Test
    fun testAutoLoginScriptGeneration() {
        val script = SessScriptInjector.getAutoLoginScript(
            username = "99123456",
            pass = "MySecretPass!@#",
            autoSubmit = true
        )
        assertTrue(script.contains("99123456"))
        assertTrue(script.contains("MySecretPass!@#"))
        assertTrue(script.contains("AndroidBridge.onCaptchaDetected"))
        assertTrue(script.contains("AndroidBridge.onAutoLoginSubmitted"))
    }

    @Test
    fun testKeepAliveScriptGeneration() {
        val script = SessScriptInjector.getKeepAliveScript()
        assertTrue(script.contains("/sess/keepalive"))
        assertTrue(script.contains("AndroidBridge.onHeartbeatAck"))
    }

    @Test
    fun testMobileOptimizationScriptGeneration() {
        val script = SessScriptInjector.getMobileOptimizationScript(darkMode = true)
        assertTrue(script.contains("sess-plus-dark-style"))
    }

    @Test
    fun testCompatibilityScriptGeneration() {
        val script = SessScriptInjector.getCompatibilityAndMenuFixScript()
        assertTrue(script.contains("showModalDialog"))
    }

    @Test
    fun testDefaultSettings() {
        val settings = AppSettings()
        assertTrue(settings.isMobileOptimizationEnabled)
        assertTrue(settings.isKeepAliveEnabled)
        assertEquals(2, settings.keepAliveIntervalMinutes)
        assertEquals(100, settings.textZoomPercent)
    }

    @Test
    fun testCredentialsModel() {
        val creds = UserCredentials(username = "123", password = "abc", isSaved = true)
        assertTrue(creds.isSaved)
        assertTrue(creds.isAutoLoginEnabled)
        assertEquals("123", creds.username)
    }
}
