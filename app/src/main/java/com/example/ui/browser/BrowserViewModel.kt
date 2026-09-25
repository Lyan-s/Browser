package com.example.ui.browser

import android.app.Application
import android.content.Context
import android.net.Uri
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AdBlocker
import com.example.data.BookmarkEntity
import com.example.data.BrowserRepository
import com.example.data.CyberDatabase
import com.example.data.HistoryEntity
import com.example.model.BrowserTab
import com.example.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class FullscreenCustomViewState(
    val customView: View? = null,
    val callback: WebChromeClient.CustomViewCallback? = null
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BrowserRepository(CyberDatabase.getInstance(application).browserDao())

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val history: StateFlow<List<HistoryEntity>> = repository.history
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val shieldEnabled = AdBlocker.isShieldEnabled
    val totalBlockedCount = AdBlocker.totalBlockedCount
    val recentBlocked = AdBlocker.recentBlocked

    private val _tabs = MutableStateFlow<List<BrowserTab>>(emptyList())
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String>("")
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _isTabManagerVisible = MutableStateFlow(false)
    val isTabManagerVisible: StateFlow<Boolean> = _isTabManagerVisible.asStateFlow()

    private val _isShieldDialogVisible = MutableStateFlow(false)
    val isShieldDialogVisible: StateFlow<Boolean> = _isShieldDialogVisible.asStateFlow()

    private val _isBookmarksVisible = MutableStateFlow(false)
    val isBookmarksVisible: StateFlow<Boolean> = _isBookmarksVisible.asStateFlow()

    private val _isHistoryVisible = MutableStateFlow(false)
    val isHistoryVisible: StateFlow<Boolean> = _isHistoryVisible.asStateFlow()

    private val _isLocalPlayerVisible = MutableStateFlow(false)
    val isLocalPlayerVisible: StateFlow<Boolean> = _isLocalPlayerVisible.asStateFlow()

    private val _selectedLocalVideo = MutableStateFlow<VideoItem?>(null)
    val selectedLocalVideo: StateFlow<VideoItem?> = _selectedLocalVideo.asStateFlow()

    private val _fullscreenState = MutableStateFlow(FullscreenCustomViewState())
    val fullscreenState: StateFlow<FullscreenCustomViewState> = _fullscreenState.asStateFlow()

    private val _isCurrentBookmarked = MutableStateFlow(false)
    val isCurrentBookmarked: StateFlow<Boolean> = _isCurrentBookmarked.asStateFlow()

    init {
        // Create initial default tab with speed dial
        createNewTab("cyber://home")
    }

    val activeTab: BrowserTab?
        get() = _tabs.value.find { it.id == _activeTabId.value }

    fun updateUrlInput(input: String) {
        _urlInput.value = input
    }

    fun createNewTab(url: String = "cyber://home", switchTo: Boolean = true): String {
        val tabId = UUID.randomUUID().toString()
        val newTab = BrowserTab(
            id = tabId,
            initialUrl = url,
            currentUrl = url,
            title = if (url == "cyber://home") "CyberDesk" else "New Tab",
            pendingUrl = if (url != "cyber://home") url else null
        )
        val updated = _tabs.value.toMutableList().apply { add(newTab) }
        _tabs.value = updated

        if (switchTo || _activeTabId.value.isEmpty()) {
            _activeTabId.value = tabId
            _urlInput.value = if (url == "cyber://home") "" else url
            checkIfCurrentIsBookmarked(url)
        }
        _isTabManagerVisible.value = false
        return tabId
    }

    fun switchTab(tabId: String) {
        val target = _tabs.value.find { it.id == tabId } ?: return
        _activeTabId.value = tabId
        _urlInput.value = if (target.currentUrl == "cyber://home") "" else target.currentUrl
        _isTabManagerVisible.value = false
        checkIfCurrentIsBookmarked(target.currentUrl)
    }

    fun closeTab(tabId: String) {
        val currentTabs = _tabs.value.toMutableList()
        val tabToRemove = currentTabs.find { it.id == tabId }
        val index = currentTabs.indexOf(tabToRemove)

        // Destroy tab's webView cleanly
        tabToRemove?.webView?.apply {
            stopLoading()
            loadUrl("about:blank")
            destroy()
        }
        tabToRemove?.webView = null

        currentTabs.removeAll { it.id == tabId }

        if (currentTabs.isEmpty()) {
            _tabs.value = emptyList()
            createNewTab("cyber://home")
        } else {
            _tabs.value = currentTabs
            if (_activeTabId.value == tabId) {
                val nextActiveIndex = (index - 1).coerceAtLeast(0).coerceAtMost(currentTabs.lastIndex)
                val newActive = currentTabs[nextActiveIndex]
                _activeTabId.value = newActive.id
                _urlInput.value = if (newActive.currentUrl == "cyber://home") "" else newActive.currentUrl
                checkIfCurrentIsBookmarked(newActive.currentUrl)
            }
        }
    }

    fun loadUrl(rawUrl: String, tabId: String? = null) {
        val targetTabId = tabId ?: _activeTabId.value
        val tab = _tabs.value.find { it.id == targetTabId } ?: return
        val formatted = formatUrl(rawUrl)

        tab.currentUrl = formatted
        _urlInput.value = if (formatted == "cyber://home") "" else formatted
        checkIfCurrentIsBookmarked(formatted)

        if (formatted == "cyber://home") {
            tab.title = "CyberDesk"
            tab.isLoading = false
            tab.loadingProgress = 0
            tab.webView?.loadUrl("about:blank")
            triggerStateUpdate()
            return
        }

        // Direct, synchronous load on active WebView if present
        val wv = tab.webView
        if (wv != null) {
            wv.loadUrl(formatted)
        } else {
            // Store pendingUrl so when AndroidView creates the WebView it immediately loads it
            tab.pendingUrl = formatted
        }
        triggerStateUpdate()
    }

    private fun formatUrl(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty() || trimmed.equals("cyber://home", ignoreCase = true)) {
            return "cyber://home"
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://")) {
            return trimmed
        }
        // Check if looks like a domain name
        val hasDomain = trimmed.contains(".") && !trimmed.contains(" ")
        return if (hasDomain) {
            "https://$trimmed"
        } else {
            // Search query with DuckDuckGo
            "https://duckduckgo.com/?q=${Uri.encode(trimmed)}"
        }
    }

    fun goBack() {
        val tab = activeTab ?: return
        if (tab.currentUrl == "cyber://home") return
        if (tab.webView?.canGoBack() == true) {
            tab.webView?.goBack()
        } else {
            loadUrl("cyber://home")
        }
    }

    fun goForward() {
        activeTab?.webView?.let {
            if (it.canGoForward()) it.goForward()
        }
    }

    fun reload() {
        val tab = activeTab ?: return
        if (tab.currentUrl == "cyber://home") {
            // refresh
            triggerStateUpdate()
        } else {
            tab.webView?.reload()
        }
    }

    fun stopLoading() {
        activeTab?.webView?.stopLoading()
        activeTab?.isLoading = false
        triggerStateUpdate()
    }

    fun toggleDesktopMode() {
        val tab = activeTab ?: return
        tab.isDesktopMode = !tab.isDesktopMode
        val wv = tab.webView
        if (wv != null) {
            val defaultUserAgent = wv.settings.userAgentString
            val desktopUserAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            wv.settings.userAgentString = if (tab.isDesktopMode) desktopUserAgent else null
            wv.settings.useWideViewPort = tab.isDesktopMode
            wv.settings.loadWithOverviewMode = tab.isDesktopMode
            wv.reload()
        }
        triggerStateUpdate()
    }

    fun toggleShield() {
        AdBlocker.toggleShield()
    }

    fun onPageStarted(tabId: String, url: String) {
        val tab = _tabs.value.find { it.id == tabId } ?: return
        tab.currentUrl = url
        tab.isLoading = true
        tab.loadingProgress = 10
        if (tabId == _activeTabId.value) {
            _urlInput.value = if (url == "cyber://home" || url == "about:blank") "" else url
            checkIfCurrentIsBookmarked(url)
        }
        triggerStateUpdate()
    }

    fun onPageFinished(tabId: String, url: String) {
        val tab = _tabs.value.find { it.id == tabId } ?: return
        tab.currentUrl = url
        tab.isLoading = false
        tab.loadingProgress = 100
        tab.canGoBack = tab.webView?.canGoBack() ?: false
        tab.canGoForward = tab.webView?.canGoForward() ?: false
        tab.blockedCount = AdBlocker.getTabBlockedCount(tabId)

        if (tabId == _activeTabId.value) {
            _urlInput.value = if (url == "cyber://home" || url == "about:blank") "" else url
            checkIfCurrentIsBookmarked(url)
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            viewModelScope.launch {
                repository.addHistory(tab.title, url)
            }
        }
        triggerStateUpdate()
    }

    fun onProgressChanged(tabId: String, progress: Int) {
        val tab = _tabs.value.find { it.id == tabId } ?: return
        tab.loadingProgress = progress
        tab.isLoading = progress < 100
        tab.blockedCount = AdBlocker.getTabBlockedCount(tabId)
        triggerStateUpdate()
    }

    fun onTitleReceived(tabId: String, title: String) {
        val tab = _tabs.value.find { it.id == tabId } ?: return
        if (title.isNotBlank()) {
            tab.title = title
            triggerStateUpdate()
        }
    }

    fun toggleBookmark() {
        val tab = activeTab ?: return
        val url = tab.currentUrl
        if (url.isEmpty() || url == "cyber://home" || url == "about:blank") return

        viewModelScope.launch {
            if (_isCurrentBookmarked.value) {
                repository.removeBookmarkByUrl(url)
                _isCurrentBookmarked.value = false
            } else {
                repository.addBookmark(tab.title.ifBlank { url }, url)
                _isCurrentBookmarked.value = true
            }
        }
    }

    private fun checkIfCurrentIsBookmarked(url: String) {
        if (url.isEmpty() || url == "cyber://home" || url == "about:blank") {
            _isCurrentBookmarked.value = false
            return
        }
        viewModelScope.launch {
            _isCurrentBookmarked.value = repository.isBookmarked(url)
        }
    }

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.removeBookmark(bookmark)
            checkIfCurrentIsBookmarked(activeTab?.currentUrl ?: "")
        }
    }

    fun removeHistoryItem(item: HistoryEntity) {
        viewModelScope.launch {
            repository.removeHistory(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun setFullscreenCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?) {
        _fullscreenState.value = FullscreenCustomViewState(customView = view, callback = callback)
    }

    fun exitFullscreenCustomView() {
        val callback = _fullscreenState.value.callback
        try {
            callback?.onCustomViewHidden()
        } catch (_: Exception) {}
        _fullscreenState.value = FullscreenCustomViewState(customView = null, callback = null)
    }

    fun setTabManagerVisible(visible: Boolean) {
        _isTabManagerVisible.value = visible
    }

    fun setShieldDialogVisible(visible: Boolean) {
        _isShieldDialogVisible.value = visible
    }

    fun setBookmarksVisible(visible: Boolean) {
        _isBookmarksVisible.value = visible
    }

    fun setHistoryVisible(visible: Boolean) {
        _isHistoryVisible.value = visible
    }

    fun openLocalPlayer(video: VideoItem? = null) {
        _selectedLocalVideo.value = video
        _isLocalPlayerVisible.value = true
    }

    fun closeLocalPlayer() {
        _isLocalPlayerVisible.value = false
    }

    private fun triggerStateUpdate() {
        _tabs.value = _tabs.value.toList()
    }

    override fun onCleared() {
        super.onCleared()
        _tabs.value.forEach { tab ->
            tab.webView?.destroy()
            tab.webView = null
        }
    }
}
