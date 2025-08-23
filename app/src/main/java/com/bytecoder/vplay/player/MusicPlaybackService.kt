package com.bytecoder.vplay.player

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bytecoder.vplay.R
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.media.session.MediaButtonReceiver

class MusicPlaybackService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        scope.launch {
            PlayerManager.ensureInitialized(applicationContext)
            startForegroundWithNowPlaying()
            PlayerManager.player?.addListener(object : Player.Listener {
                override fun onMediaItemTransition(item: com.google.android.exoplayer2.MediaItem?, reason: Int) {
                    startForegroundWithNowPlaying()
                }
            })
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uriStrs = intent?.getStringArrayListExtra(EXTRA_URIS) ?: arrayListOf()
        val titles = intent?.getStringArrayListExtra(EXTRA_TITLES) ?: arrayListOf()
        val index = intent?.getIntExtra(EXTRA_INDEX, 0) ?: 0

        if (uriStrs.isNotEmpty()) {
            val uris = uriStrs.map { Uri.parse(it) }
            PlayerManager.setPlaylist(uris, titles, index)
        }

        // Handle media button intents
        MediaButtonReceiver.handleIntent(PlayerManager.mediaSession, intent)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundWithNowPlaying() {
        val p = PlayerManager.player
        val title = PlayerManager.titles.getOrNull(p?.currentMediaItemIndex ?: 0) ?: getString(R.string.app_name)
        val notification: Notification = NotificationCompat.Builder(this, PlayerManager.NOTIF_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(getString(R.string.now_playing))
            .setOngoing(true)
            .build()

        // simple call, cross API levels
        startForeground(PlayerManager.NOTIF_ID, notification)
    }

    companion object {
        const val EXTRA_URIS = "uris"
        const val EXTRA_TITLES = "titles"
        const val EXTRA_INDEX = "index"
    }
}
