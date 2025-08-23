package com.bytecoder.vplay.player

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bytecoder.vplay.R

class VideoPlaybackService : Service() {

    override fun onCreate() {
        super.onCreate()
        VideoPlayerManager.ensureInitialized(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: getString(R.string.app_name)
        startForegroundWith(title)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundWith(title: String) {
        val notif: Notification = NotificationCompat.Builder(this, "vplay_video_channel")
            .setSmallIcon(R.drawable.ic_notification)
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
}
