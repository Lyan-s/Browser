package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.AdBlocker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CyberNet", appName)
    }

    @Test
    fun `ad blocker intercepts ad and tracker domains`() {
        // DoubleClick ad network should be blocked
        val adUri = Uri.parse("https://pagead2.googlesyndication.com/pagead/ads?client=ca-pub-1234")
        assertTrue(AdBlocker.isBlocked(adUri))

        // Google Analytics tracker should be blocked
        val trackerUri = Uri.parse("https://www.google-analytics.com/analytics.js")
        assertTrue(AdBlocker.isBlocked(trackerUri))

        // Normal legitimate website should NOT be blocked
        val normalUri = Uri.parse("https://en.wikipedia.org/wiki/Kotlin")
        assertFalse(AdBlocker.isBlocked(normalUri))
    }
}
