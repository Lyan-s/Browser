package com.example.ui.browser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.AdBlocker
import com.example.model.BrowserTab
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TabWebView(
    tab: BrowserTab,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            getOrCreateWebViewForTab(context, tab, viewModel)
        },
        update = { webView ->
            // Check if there is an immediate pending URL to load (e.g. from Go button or new tab creation)
            tab.pendingUrl?.let { pending ->
                tab.pendingUrl = null
                if (pending != "cyber://home") {
                    webView.loadUrl(pending)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )

    DisposableEffect(tab.id) {
        onDispose {
            // Keep webView intact across tab switching, but pause timers/media if tab unselected
            tab.webView?.onPause()
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
fun getOrCreateWebViewForTab(
    context: Context,
    tab: BrowserTab,
    viewModel: BrowserViewModel
): WebView {
    val existing = tab.webView
    if (existing != null) {
        existing.onResume()
        // If there's an immediate pending URL, load it
        tab.pendingUrl?.let { pending ->
            tab.pendingUrl = null
            if (pending != "cyber://home") {
                existing.loadUrl(pending)
            }
        }
        return existing
    }

    val webView = WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
            allowFileAccess = true
            allowContentAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

            // Video Playback Requirements:
            mediaPlaybackRequiresUserGesture = false

            if (tab.isDesktopMode) {
                userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            }
        }

        webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url ?: return super.shouldInterceptRequest(view, request)

                // Ad and tracker blocking
                if (AdBlocker.isBlocked(url, tab.id)) {
                    // Return empty 200 OK plain-text response
                    return WebResourceResponse(
                        "text/plain",
                        "UTF-8",
                        ByteArrayInputStream(ByteArray(0))
                    )
                }

                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let { viewModel.onPageStarted(tab.id, it) }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let { viewModel.onPageFinished(tab.id, it) }
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                viewModel.onProgressChanged(tab.id, newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                title?.let { viewModel.onTitleReceived(tab.id, it) }
            }

            // In-page HTML5/YouTube video fullscreen handling
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(view, callback)
                viewModel.setFullscreenCustomView(view, callback)
            }

            override fun onHideCustomView() {
                super.onHideCustomView()
                viewModel.exitFullscreenCustomView()
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                // Grant media permissions if requested (e.g. video conferencing or camera/mic)
                try {
                    request?.grant(request.resources)
                } catch (_: Exception) {
                    super.onPermissionRequest(request)
                }
            }
        }
    }

    tab.webView = webView

    // Immediately load URL without waiting for asynchronous events
    val targetUrl = tab.pendingUrl ?: tab.initialUrl
    tab.pendingUrl = null
    if (targetUrl.isNotEmpty() && targetUrl != "cyber://home" && targetUrl != "about:blank") {
        webView.loadUrl(targetUrl)
    }

    return webView
}
