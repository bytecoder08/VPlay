package com.bytecoder.vplay.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import com.bytecoder.vplay.player.music.MusicPlayerManager
import com.bytecoder.vplay.player.video.VideoPlayerManager
import com.bytecoder.vplay.player.music.MusicPlayer
import com.bytecoder.vplay.player.video.VideoPlayer
import java.io.File

object PlayerLauncher {
    private var fullPlayerActive: Boolean = false
    private var currentPlayer: Class<*>? = null

    fun launchMusicPlayer(context: Context) {
        fullPlayerActive = true
        currentPlayer = MusicPlayer::class.java
        context.startActivity(Intent(context, MusicPlayer::class.java))
    }

    fun launchVideoPlayer(context: Context) {
        fullPlayerActive = true
        currentPlayer = VideoPlayer::class.java
        context.startActivity(Intent(context, VideoPlayer::class.java))
    }

    fun onFullPlayerClosed() {
        fullPlayerActive = false
        currentPlayer = null
    }

    fun isFullPlayerActive(): Boolean = fullPlayerActive
    fun getCurrentPlayerClass(): Class<*>? = currentPlayer

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

    // --- Helpers added to centralize launch + index reads ---
    fun launchMusicPlayer(context: Context, uris: List<Uri>, titles: List<String>, index: Int) {
        if (index in uris.indices) {
            val uri = uris[index]
            val title = titles.getOrNull(index) ?: uri.toString()
            val exoItem = com.google.android.exoplayer2.MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(
                    com.google.android.exoplayer2.MediaMetadata.Builder()
                        .setTitle(title)
                        .build()
                )
                .build()
            MusicPlayerManager.setMediaItem(exoItem, playWhenReady = true)
        }
    }

    fun launchVideoPlayer(context: Context, mediaItems: List<com.google.android.exoplayer2.MediaItem>, index: Int) {
        if (index in mediaItems.indices) {
            val item = mediaItems[index]
            VideoPlayerManager.setMediaItem(item, playWhenReady = true)
        }
    }

    fun getCurrentMusicIndex(): Int {
        return MusicPlayerManager.player?.currentMediaItemIndex ?: 0
    }

    fun getCurrentVideoIndex(): Int {
        return VideoPlayerManager.getCurrentIndex()
    }
}
