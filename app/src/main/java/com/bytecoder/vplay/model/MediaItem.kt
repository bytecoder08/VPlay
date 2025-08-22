package com.bytecoder.vplay.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val folderName: String,
    val durationMs: Long = 0L
)
