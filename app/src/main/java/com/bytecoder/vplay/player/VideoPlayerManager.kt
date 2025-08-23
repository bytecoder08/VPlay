package com.bytecoder.vplay.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.bytecoder.vplay.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import android.support.v4.media.session.MediaSessionCompat

object VideoPlayerManager {

    private const val NOTIF_CHANNEL_ID = "vplay_video_channel"
    private const val NOTIF_CHANNEL_NAME = "Video Playback"
    private const val NOTIF_ID = 5234

    var player: ExoPlayer? = null
        private set
    private var mediaSession: MediaSessionCompat? = null
    private var connector: MediaSessionConnector? = null
    private var notification: PlayerNotificationManager? = null

    fun ensureInitialized(ctx: Context) {
        if (player != null) return

        createChannel(ctx)

        val p = ExoPlayer.Builder(ctx).build().apply {
            val audioAttr = AudioAttributes.Builder()
                .setUsage(com.google.android.exoplayer2.C.USAGE_MEDIA)
                .setContentType(com.google.android.exoplayer2.C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()
            setAudioAttributes(audioAttr, true)
            playWhenReady = true
        }
        player = p

        mediaSession = MediaSessionCompat(ctx, "vplay_video_session").apply { isActive = true }
        connector = MediaSessionConnector(mediaSession!!).also { it.setPlayer(p) }

        notification = PlayerNotificationManager.Builder(ctx, NOTIF_ID, NOTIF_CHANNEL_ID)
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: com.google.android.exoplayer2.Player): CharSequence {
                    val title = player.mediaMetadata.title
                    return title ?: ctx.getString(R.string.app_name)
                }
                override fun createCurrentContentIntent(player: com.google.android.exoplayer2.Player) = null
                override fun getCurrentContentText(player: com.google.android.exoplayer2.Player): CharSequence? = null
                override fun getCurrentLargeIcon(
                    player: com.google.android.exoplayer2.Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ) = null
            })
            .setChannelImportance(NotificationManager.IMPORTANCE_LOW)
            .build().apply {
                setSmallIcon(R.drawable.ic_notification)
                mediaSession?.sessionToken?.let { setMediaSessionToken(it) }
                setUsePlayPauseActions(true)
                setUseNextAction(false)
                setUsePreviousAction(false)
            }

        notification?.setPlayer(p)
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
    }

    private fun createChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService<NotificationManager>() ?: return
            val ch = NotificationChannel(NOTIF_CHANNEL_ID, NOTIF_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
    }
}
