package com.example.data

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

data class BlockedItem(
    val id: Long = System.currentTimeMillis() + (0..9999).random(),
    val host: String,
    val url: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)

object AdBlocker {
    private val _isShieldEnabled = MutableStateFlow(true)
    val isShieldEnabled: StateFlow<Boolean> = _isShieldEnabled.asStateFlow()

    private val _totalBlockedCount = MutableStateFlow(0)
    val totalBlockedCount: StateFlow<Int> = _totalBlockedCount.asStateFlow()

    // TabId -> Count of blocked items
    private val tabBlockedCounters = ConcurrentHashMap<String, AtomicInteger>()

    // Global recent blocked history (keep last 50)
    private val _recentBlocked = MutableStateFlow<List<BlockedItem>>(emptyList())
    val recentBlocked: StateFlow<List<BlockedItem>> = _recentBlocked.asStateFlow()

    // Categorized Blocklists
    private val adDomains = setOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "amazon-adsystem.com",
        "adnxs.com",
        "advertising.com",
        "adcolony.com",
        "applovin.com",
        "unityads.unity3d.com",
        "vungle.com",
        "criteo.com",
        "criteo.net",
        "adroll.com",
        "taboola.com",
        "outbrain.com",
        "popads.net",
        "propellerads.com",
        "pubmatic.com",
        "rubiconproject.com",
        "openx.net",
        "smartadserver.com",
        "casalemedia.com",
        "bidswitch.net",
        "admob.com",
        "moatads.com",
        "adsafeprotected.com",
        "inmobi.com",
        "adsystem.com",
        "advertserve.com",
        "revcontent.com",
        "mgid.com",
        "zedo.com",
        "zergnet.com",
        "adblade.com"
    )

    private val trackerDomains = setOf(
        "google-analytics.com",
        "googletagmanager.com",
        "connect.facebook.net",
        "analytics.facebook.com",
        "pixel.facebook.com",
        "hotjar.com",
        "quantserve.com",
        "scorecardresearch.com",
        "newrelic.com",
        "nr-data.net",
        "segment.io",
        "segment.com",
        "mixpanel.com",
        "amplitude.com",
        "optimizely.com",
        "chartbeat.com",
        "mouseflow.com",
        "crazyegg.com",
        "statcounter.com",
        "branch.io",
        "adjust.com",
        "appsflyer.com",
        "clarity.ms",
        "fullstory.com",
        "yandex.ru"
    )

    private val adPathKeywords = listOf(
        "/ads.js",
        "/pagead/",
        "/adsense/",
        "/adservice/",
        "/advert/",
        "/advertisement/",
        "/ad-banner/",
        "/tracking.js",
        "/gtm.js",
        "/analytics.js",
        "/fbevents.js",
        "/pixel.gif",
        "/telemetry/"
    )

    fun toggleShield(enabled: Boolean? = null) {
        _isShieldEnabled.value = enabled ?: !_isShieldEnabled.value
    }

    fun isBlocked(uri: Uri, tabId: String? = null): Boolean {
        if (!_isShieldEnabled.value) return false

        val host = uri.host?.lowercase() ?: return false
        val fullUrl = uri.toString().lowercase()

        // Match against domain sets (including subdomains)
        val matchedAd = adDomains.any { host == it || host.endsWith(".$it") }
        if (matchedAd) {
            recordBlock(host, fullUrl, "AD", tabId)
            return true
        }

        val matchedTracker = trackerDomains.any { host == it || host.endsWith(".$it") }
        if (matchedTracker) {
            recordBlock(host, fullUrl, "TRACKER", tabId)
            return true
        }

        // Match against suspicious path keywords
        val matchedPath = adPathKeywords.any { fullUrl.contains(it) }
        if (matchedPath) {
            recordBlock(host, fullUrl, "SCRIPT", tabId)
            return true
        }

        return false
    }

    private fun recordBlock(host: String, url: String, category: String, tabId: String?) {
        _totalBlockedCount.value += 1

        if (tabId != null) {
            val counter = tabBlockedCounters.getOrPut(tabId) { AtomicInteger(0) }
            counter.incrementAndGet()
        }

        val item = BlockedItem(host = host, url = url, category = category)
        val current = _recentBlocked.value.toMutableList()
        current.add(0, item)
        if (current.size > 50) {
            current.removeAt(current.lastIndex)
        }
        _recentBlocked.value = current
    }

    fun getTabBlockedCount(tabId: String): Int {
        return tabBlockedCounters[tabId]?.get() ?: 0
    }

    fun resetTabCounter(tabId: String) {
        tabBlockedCounters[tabId]?.set(0)
    }

    fun clearHistory() {
        _recentBlocked.value = emptyList()
    }
}
