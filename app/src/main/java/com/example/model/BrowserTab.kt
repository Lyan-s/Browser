package com.example.model

import android.webkit.WebView
import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val initialUrl: String = "cyber://home",
    var currentUrl: String = initialUrl,
    var title: String = "CyberTab",
    var loadingProgress: Int = 0,
    var isLoading: Boolean = false,
    var canGoBack: Boolean = false,
    var canGoForward: Boolean = false,
    var blockedCount: Int = 0,
    var isDesktopMode: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    var webView: WebView? = null,
    var pendingUrl: String? = null
)
