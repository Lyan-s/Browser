package com.example.data

import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val dao: BrowserDao) {
    val bookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    val history: Flow<List<HistoryEntity>> = dao.getAllHistory()

    suspend fun addBookmark(title: String, url: String): Long {
        return dao.insertBookmark(BookmarkEntity(title = title, url = url))
    }

    suspend fun removeBookmark(bookmark: BookmarkEntity) {
        dao.deleteBookmark(bookmark)
    }

    suspend fun removeBookmarkByUrl(url: String) {
        dao.deleteBookmarkByUrl(url)
    }

    suspend fun isBookmarked(url: String): Boolean {
        return dao.isBookmarked(url)
    }

    suspend fun addHistory(title: String, url: String) {
        if (url.isEmpty() || url == "about:blank") return
        dao.insertHistory(HistoryEntity(title = title.ifBlank { url }, url = url))
    }

    suspend fun removeHistory(history: HistoryEntity) {
        dao.deleteHistory(history)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }
}
