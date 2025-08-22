package com.bytecoder.vplay.model

import android.net.Uri

data class MusicItem(
    val title: String,
    val artist: String,
    val path: String,
    val albumArtUri: Uri
)