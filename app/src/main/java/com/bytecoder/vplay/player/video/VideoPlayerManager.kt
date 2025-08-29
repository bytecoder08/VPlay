package com.bytecoder.vplay.player.video

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.getSystemService
import com.bytecoder.vplay.R
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.bytecoder.vplay.player.MiniPlayerView
import java.util.concurrent.CopyOnWriteArraySet

object VideoPlayerManager : MiniPlayerView.Host{

    private const val NOTIF_CHANNEL_ID = "vplay_video_channel"
    private const val NOTIF_CHANNEL_NAME = "Video Playback"
    private const val NOTIF_ID = 5234

    var player: ExoPlayer? = null

    val queueUris: MutableList<Uri> = mutableListOf()

    private val listeners = CopyOnWriteArraySet<() -> Unit>()
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

    override fun isPlaybackActive(): Boolean = try{
        player != null && player?.currentMediaItem != null || queueUris.isNotEmpty();
    } catch (_: Throwable) { false }

    override fun getCurrentTitle(): String? = try{
        player?.currentMediaItem?.mediaMetadata?.title?.toString()
    } catch (_: Throwable) { null }

    override fun getCurrentMediaUri(): String? = try{
        player?.currentMediaItem?.localConfiguration?.uri?.toString()
            ?: queueUris.getOrNull(player?.currentMediaItemIndex ?: 0)?.toString()
    } catch (_: Throwable) { null }

    override fun isCurrentMediaVideo(): Boolean = true
    override fun getPlaybackPositionMs(): Long = try{
        player?.currentPosition ?: 0L
    } catch (_: Throwable) { 0L }
    override fun getPlaybackDurationMs(): Long = try { player?.duration ?: 0L } catch (_: Throwable) { 0L }
    override fun isPlaying(): Boolean = try { player?.isPlaying == true } catch (_: Throwable) { false }
    override fun togglePlayPause() {
        try {
            player?.let { p -> p.playWhenReady = !p.isPlaying }
            notifyListeners()
        } catch (_: Throwable) {}
    }
    override fun stopAndClearQueue() {
        try {
            player?.stop()
            queueUris.clear()
            notifyListeners()
        } catch (_: Throwable) {}
    }

    override fun openFullPlayer(context: Context) {
        com.bytecoder.vplay.player.PlayerLauncher.launchVideoPlayer(context)
    }

//    private val listeners = mutableSetOf<() -> Unit>()
    override fun subscribePlaybackUpdates(listener: () -> Unit) { listeners.add(listener) }
    override fun unsubscribePlaybackUpdates(listener: () -> Unit) { listeners.remove(listener) }
    fun notifyListeners() { listeners.forEach { safeNotify(it) } }
    private fun safeNotify(fn: () -> Unit) { try { fn() } catch (_: Throwable) {} }

    fun attachPlayer(exo: ExoPlayer) {
        if (player === exo) return
        player = exo

        exo.addListener(object : Player.Listener {
//            override fun onEvents(player: Player, events: Player.Events) {
//                notifyListeners()
//            }
            override fun onIsPlayingChanged(isPlaying: Boolean) { notifyListeners() }
            override fun onPlaybackStateChanged(state: Int) { notifyListeners() }
            override fun onMediaItemTransition(item: MediaItem?, reason: Int) { notifyListeners() }
            override fun onPlayerError(error: PlaybackException) { notifyListeners() }
        })
    }

    fun playUrl(context: Context, url: String, title: String? = null, startNow: Boolean = true) {
        try {
            val exo = player ?: return
            exo.stop()
            exo.clearMediaItems()
            val item = MediaItem.fromUri(Uri.parse(url))
            exo.setMediaItem(item)
            exo.prepare()
            exo.playWhenReady = startNow
            notifyListeners()
        } catch (_: Throwable) {}
    }
}
