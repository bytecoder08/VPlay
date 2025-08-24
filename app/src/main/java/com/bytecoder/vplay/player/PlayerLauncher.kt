package com.bytecoder.vplay.player

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File

object PlayerLauncher {

    fun play(
        context: Context,
        path: String,
        title: String? = null,
        subtitleUrl: String? = null
    ) {
        if (path.startsWith("http", ignoreCase = true) ||
            path.startsWith("rtsp", ignoreCase = true)) {

            val ext = Uri.parse(path).lastPathSegment?.substringAfterLast('.', "")?.lowercase()
            val mimeType = ext?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it) } ?: ""

            when {
                mimeType.startsWith("video") || path.contains(".mp4") || path.contains(".mkv") -> {
                    val exoItem = com.google.android.exoplayer2.MediaItem.Builder()
                        .setUri(path)
                        .setMediaMetadata(
                            com.google.android.exoplayer2.MediaMetadata.Builder()
                                .setTitle(title ?: path)
                                .build()
                        )
                        .build()
                    VideoPlayerManager.setMediaItem(exoItem, playWhenReady = true)
                }

                mimeType.startsWith("audio") || path.contains(".mp3") || path.contains(".aac") -> {
                    val exoItem = com.google.android.exoplayer2.MediaItem.Builder()
                        .setUri(path)
                        .setMediaMetadata(
                            com.google.android.exoplayer2.MediaMetadata.Builder()
                                .setTitle(title ?: path)
                                .build()
                        )
                        .build()
                    MusicPlayerManager.setMediaItem(exoItem, playWhenReady = true)
                }

                else -> {
                    val exoItem = com.google.android.exoplayer2.MediaItem.Builder()
                        .setUri(path)
                        .setMediaMetadata(
                            com.google.android.exoplayer2.MediaMetadata.Builder()
                                .setTitle(title ?: path)
                                .build()
                        )
                        .build()
                    VideoPlayerManager.setMediaItem(exoItem, playWhenReady = true)
                }
            }

        } else {
            val file = File(path)
            val extension = file.extension.lowercase()
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: ""

            when {
                mimeType.startsWith("video") -> {
                    val exoItem = com.google.android.exoplayer2.MediaItem.Builder()
                        .setUri(path)
                        .setMediaMetadata(
                            com.google.android.exoplayer2.MediaMetadata.Builder()
                                .setTitle(title ?: file.name)
                                .build()
                        )
                        .build()
                    VideoPlayerManager.setMediaItem(exoItem, playWhenReady = true)
                }

                mimeType.startsWith("audio") -> {
                    val exoItem = com.google.android.exoplayer2.MediaItem.Builder()
                        .setUri(path)
                        .setMediaMetadata(
                            com.google.android.exoplayer2.MediaMetadata.Builder()
                                .setTitle(title ?: file.name)
                                .build()
                        )
                        .build()
                    MusicPlayerManager.setMediaItem(exoItem, playWhenReady = true)
                }

                else -> {
                    val exoItem = com.google.android.exoplayer2.MediaItem.Builder()
                        .setUri(path)
                        .setMediaMetadata(
                            com.google.android.exoplayer2.MediaMetadata.Builder()
                                .setTitle(title ?: file.name)
                                .build()
                        )
                        .build()
                    VideoPlayerManager.setMediaItem(exoItem, playWhenReady = true)
                }
            }
        }
    }
}
