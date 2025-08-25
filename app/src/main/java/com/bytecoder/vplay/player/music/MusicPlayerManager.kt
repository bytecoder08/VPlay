package com.bytecoder.vplay.player.music

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import com.bytecoder.vplay.R
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import android.support.v4.media.session.MediaSessionCompat

object MusicPlayerManager {

    const val NOTIF_CHANNEL_ID = "vplay_playback_channel"
    const val NOTIF_CHANNEL_NAME = "Playback"
    const val NOTIF_ID = 4123

    var player: ExoPlayer? = null
        private set

    var mediaSession: MediaSessionCompat? = null
        private set

    var notificationManager: PlayerNotificationManager? = null
        private set

    private var mediaSessionConnector: MediaSessionConnector? = null

    val uris: MutableList<Uri> = mutableListOf()
    val titles: MutableList<String> = mutableListOf()

    private var queueMode = false
    fun enableQueueMode(enable: Boolean) { queueMode = enable }
    fun isInQueueMode() = queueMode

    fun handleQueueError(player: ExoPlayer, error: PlaybackException) {
        val nextIndex = player.currentMediaItemIndex + 1
        if (nextIndex < player.mediaItemCount) {
            player.seekTo(nextIndex, 0)
            player.play()
        } else {
            player.stop()
            Log.w("MusicPlayerManager", "Queue finished or item cannot play")
        }
    }

    suspend fun ensureInitialized(appContext: Context) {
        if (player != null) return

        createNotificationChannel(appContext)

        val exo = ExoPlayer.Builder(appContext).build()
        val audioAttr = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        exo.setAudioAttributes(audioAttr, true)
        exo.playWhenReady = true
        player = exo

        mediaSession = MediaSessionCompat(appContext, "vplay_session").apply {
            isActive = true
        }

        notificationManager = PlayerNotificationManager.Builder(
            appContext,
            NOTIF_ID,
            NOTIF_CHANNEL_ID
        )
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    val i = player.currentMediaItemIndex
                    return titles.getOrNull(i) ?: uris.getOrNull(i)?.lastPathSegment ?: appContext.getString(R.string.app_name)
                }

                override fun createCurrentContentIntent(player: Player) = null

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return uris.getOrNull(player.currentMediaItemIndex)?.path
                }

                override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
                    val uri = uris.getOrNull(player.currentMediaItemIndex) ?: return null
                    val bmp = tryExtractEmbedded(appContext, uri)
                    if (bmp != null) return bmp
                    Thread {
                        val art = tryExtractEmbedded(appContext, uri)
                        if (art != null) callback.onBitmap(art)
                    }.start()
                    return null
                }
            })
            .setChannelImportance(NotificationManager.IMPORTANCE_LOW)
            .build().apply {
                setSmallIcon(R.drawable.notifications_24px)
                mediaSession?.sessionToken?.let { token ->
                    setMediaSessionToken(token)
                }
                setUseNextAction(true)
                setUsePreviousAction(true)
                setUsePlayPauseActions(true)
            }

        notificationManager?.setPlayer(exo)

        mediaSessionConnector = MediaSessionConnector(mediaSession!!)
        mediaSessionConnector?.setPlayer(exo)

        mediaSession?.sessionToken?.let { token ->
            notificationManager?.setMediaSessionToken(token)
        }

        exo.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                if (queueMode) handleQueueError(exo, error)
                else {
                    Log.w("MusicPlayerManager", "Playback error: ${error.message}")
                }
            }
        })
    }

    fun setPlaylist(newUris: List<Uri>, newTitles: List<String>, startIndex: Int = 0) {
        uris.clear(); uris.addAll(newUris)
        titles.clear(); titles.addAll(newTitles)
        val p = player ?: return
        val items = uris.map { MediaItem.fromUri(it) }
        p.setMediaItems(items, startIndex, 0)
        p.prepare()
        p.playWhenReady = true
    }

    fun setMediaItem(item: MediaItem, playWhenReady: Boolean = true) {
        val p = player ?: return
        p.setMediaItem(item)
        p.prepare()
        p.playWhenReady = playWhenReady

        uris.clear()
        titles.clear()
        uris.add(item.localConfiguration?.uri ?: Uri.EMPTY)
        titles.add(item.mediaMetadata.title?.toString() ?: "Unknown")
    }

    fun jumpTo(index: Int) {
        val p = player ?: return
        if (index in 0 until p.mediaItemCount) {
            p.seekTo(index, 0)
            p.play()
        }
    }

    fun getQueue(): List<Pair<Int, String>> {
        return uris.mapIndexed { idx, uri -> Pair(idx, titles.getOrNull(idx) ?: uri.lastPathSegment ?: "Item $idx") }
    }

    fun release() {
        try { notificationManager?.setPlayer(null) } catch (_: Exception) {}
        try { mediaSessionConnector?.setPlayer(null) } catch (_: Exception) {}
        mediaSessionConnector = null
        notificationManager = null
        player?.release()
        player = null
        try { mediaSession?.release() } catch (_: Exception) {}
        mediaSession = null
        uris.clear()
        titles.clear()
    }

    private fun createNotificationChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService<NotificationManager>() ?: return
            val ch = NotificationChannel(NOTIF_CHANNEL_ID, NOTIF_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
    }

    private fun tryExtractEmbedded(ctx: Context, uri: Uri): Bitmap? {
        return try {
            val mmr = MediaMetadataRetriever()
            if (Build.VERSION.SDK_INT >= 24) {
                ctx.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    mmr.setDataSource(pfd.fileDescriptor)
                }
            } else {
                mmr.setDataSource(ctx, uri)
            }
            val art = mmr.embeddedPicture
            mmr.release()
            if (art != null) BitmapFactory.decodeByteArray(art, 0, art.size) else null
        } catch (e: Exception) {
            Log.w("PlayerManager", "art extraction failed: ${e.message}")
            null
        }
    }
}
