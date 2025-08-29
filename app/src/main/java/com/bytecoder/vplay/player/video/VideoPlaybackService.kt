package com.bytecoder.vplay.player.video

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bytecoder.vplay.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.MediaItem

class VideoPlaybackService : Service() {
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        val exo = ExoPlayer.Builder(this).build()
        VideoPlayerManager.attachPlayer(exo)
        VideoPlayerManager.player = exo
        VideoPlayerManager.ensureInitialized(this)
        VideoPlayerManager.attachPlayer(exo)
    }
//    scope.launch {
//        startForegroundWithNowPlaying()
//    }
//    exo.addListener(object : Player.Listener {
//        override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
//            startForegroundWithNowPlaying()
//        }
//    })
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: getString(R.string.app_name)
        startForegroundWith(title)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundWith(title: String) {
        val notif: Notification = NotificationCompat.Builder(this, "vplay_video_channel")
            .setSmallIcon(R.drawable.notifications_24px)
            .setContentTitle(title)
            .setContentText(getString(R.string.now_playing_video))
            .setOngoing(true)
            .build()
        startForeground(5234, notif)
    }

    companion object {
        private const val EXTRA_TITLE = "title"

        fun startForeground(ctx: Context, title: String) {
            val i = Intent(ctx, VideoPlaybackService::class.java).apply {
                putExtra(EXTRA_TITLE, title)
            }
            ctx.startForegroundService(i)
        }
    }
    private fun startForegroundWithNowPlaying(title: String) {
        val notif: Notification = NotificationCompat.Builder(this, "vplay_video_channel")
            .setSmallIcon(R.drawable.notifications_24px)
            .setContentTitle(title)
            .setContentText(getString(R.string.now_playing_video))
            .setOngoing(true)
            .build()
        startForeground(5234, notif)
    }
}
