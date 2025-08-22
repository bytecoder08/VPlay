package com.bytecoder.vplay.model

import android.net.Uri

data class VideoItem(
    val title: String,
    val path: String,
    val thumbnailUri: Uri
)