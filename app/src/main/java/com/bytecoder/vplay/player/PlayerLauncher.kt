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
        // Check if it’s a URL (http/https/rtsp etc.)
        if (path.startsWith("http", ignoreCase = true) ||
            path.startsWith("rtsp", ignoreCase = true)) {

            // Guess type from extension (if available in URL)
            val ext = Uri.parse(path).lastPathSegment?.substringAfterLast('.', "")?.lowercase()
            val mimeType = ext?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it) } ?: ""

            when {
                mimeType.startsWith("video") || path.contains(".mp4") || path.contains(".mkv") -> {
                    VideoPlayerActivity.launch(
                        context = context,
                        url = path,
                        title = title ?: path,
                        subtitleUrl = subtitleUrl
                    )
                }

                mimeType.startsWith("audio") || path.contains(".mp3") || path.contains(".aac") -> {
                    MusicPlayerActivity.launch(
                        context = context,
                        url = path,
                        title = title ?: path
                    )
                }

                else -> {
                    // Default: try video player for unknown streams
                    VideoPlayerActivity.launch(
                        context = context,
                        url = path,
                        title = title ?: path,
                        subtitleUrl = subtitleUrl
                    )
                }
            }

        } else {
            // Local file → use File + MimeType
            val file = File(path)
            val extension = file.extension.lowercase()
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: ""

            when {
                mimeType.startsWith("video") -> {
                    VideoPlayerActivity.launch(
                        context = context,
                        url = path,
                        title = title ?: file.name,
                        subtitleUrl = subtitleUrl
                    )
                }

                mimeType.startsWith("audio") -> {
                    MusicPlayerActivity.launch(
                        context = context,
                        url = path,
                        title = title ?: file.name
                    )
                }

                else -> {
                    // Fallback → open in video player
                    VideoPlayerActivity.launch(
                        context = context,
                        url = path,
                        title = title ?: file.name,
                        subtitleUrl = subtitleUrl
                    )
                }
            }
        }
    }
}
