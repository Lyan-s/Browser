package com.example.model

import android.net.Uri

data class VideoItem(
    val uri: Uri,
    val title: String,
    val durationMs: Long = 0,
    val sizeBytes: Long = 0,
    val isSample: Boolean = false
)
