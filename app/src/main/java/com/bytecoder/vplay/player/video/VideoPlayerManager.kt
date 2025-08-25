package com.bytecoder.vplay.player.video

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.bytecoder.vplay.R
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log

object VideoPlayerManager {

    private const val NOTIF_CHANNEL_ID = "vplay_video_channel"
    private const val NOTIF_CHANNEL_NAME = "Video Playback"
    private const val NOTIF_ID = 5234

    var player: ExoPlayer? = null
        private set
    private var mediaSession: MediaSessionCompat? = null
    private var connector: MediaSessionConnector? = null
    private var notification: PlayerNotificationManager? = null

    private val queue = mutableListOf<MediaItem>()
    private var currentIndex = 0
    private var queueListener: ((List<MediaItem>) -> Unit)? = null
    private var queueMode = false
    fun enableQueueMode(enable: Boolean) { queueMode = enable }
    fun isInQueueMode() = queueMode

    fun ensureInitialized(ctx: Context) {
        if (player != null) return

        createChannel(ctx)

        val p = ExoPlayer.Builder(ctx).build().apply {
            val audioAttr = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()
            setAudioAttributes(audioAttr, true)
            playWhenReady = true
        }
        player = p

        mediaSession = MediaSessionCompat(ctx, "vplay_video_session").apply { isActive = true }
        connector = MediaSessionConnector(mediaSession!!).also { it.setPlayer(p) }

        notification = PlayerNotificationManager.Builder(ctx, NOTIF_ID, NOTIF_CHANNEL_ID)
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    val title = player.mediaMetadata.title
                    return title ?: ctx.getString(R.string.app_name)
                }
                override fun createCurrentContentIntent(player: Player) = null
                override fun getCurrentContentText(player: Player): CharSequence? = null
                override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback) = null
            })
            .setChannelImportance(NotificationManager.IMPORTANCE_LOW)
            .build().apply {
                setSmallIcon(R.drawable.notifications_24px)
                mediaSession?.sessionToken?.let { setMediaSessionToken(it) }
                setUsePlayPauseActions(true)
                setUseNextAction(false)
                setUsePreviousAction(false)
            }

        notification?.setPlayer(p)

        p.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                if (queueMode) handleQueueError()
                else {
                    Log.w("VideoPlayerManager", "Playback error: ${error.message}")
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentIndex = player?.currentMediaItemIndex ?: 0
            }
        })
    }

    fun handleQueueError() {
        val nextIndex = currentIndex + 1
        if (nextIndex < queue.size) {
            jumpTo(nextIndex)
        } else {
            player?.stop()
        }
    }

    fun setMediaItem(item: MediaItem, playWhenReady: Boolean) {
        val p = player ?: return
        p.setMediaItem(item)
        p.prepare()
        p.playWhenReady = playWhenReady
    }

    fun play() { player?.playWhenReady = true }
    fun pause() { player?.playWhenReady = false }

    fun release() {
        try { notification?.setPlayer(null) } catch (_: Exception) {}
        try { connector?.setPlayer(null) } catch (_: Exception) {}
        connector = null
        notification = null
        player?.release()
        player = null
        try { mediaSession?.release() } catch (_: Exception) {}
        mediaSession = null
        queue.clear()
        currentIndex = 0
    }

    private fun createChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService<NotificationManager>() ?: return
            val ch = NotificationChannel(NOTIF_CHANNEL_ID, NOTIF_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
    }

    fun setQueue(list: List<MediaItem>) {
        queue.clear()
        queue.addAll(list)
        currentIndex = 0
        queueListener?.invoke(queue)

        val p = player ?: return
        p.setMediaItems(queue)
        p.prepare()
        p.playWhenReady = true
    }

    fun getQueue(): List<MediaItem> = queue.toList()

    fun jumpTo(index: Int) {
        if (index in queue.indices) {
            currentIndex = index
            val p = player ?: return
            p.seekTo(index, 0)
            p.playWhenReady = true
        }
    }

    fun getCurrentIndex(): Int = currentIndex

    fun setQueueListener(listener: ((List<MediaItem>) -> Unit)?) {
        queueListener = listener
    }
}
