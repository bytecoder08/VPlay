package com.bytecoder.vplay

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.ui.PlayerView

class VideoPlayer : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private var isFullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_player)

        title = intent.getStringExtra("title") ?: "Video"
        playerView = findViewById(R.id.playerView)

        findViewById<ImageButton>(R.id.btnFullscreen).setOnClickListener { toggleFullscreen() }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            playerView.player = exoPlayer

            val url = intent.getStringExtra("url") ?: return@also
            var mediaItem = MediaItem.fromUri(Uri.parse(url))

            // Optional sample subtitles (VTT)
            val subs = MediaItem.Subtitle(
                Uri.parse("https://bitdash-a.akamaihd.net/content/sintel/subtitles/subtitles_en.vtt"),
                MimeTypes.TEXT_VTT,
                "en"
            )
            mediaItem = mediaItem.buildUpon().setSubtitles(listOf(subs)).build()

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    private fun releasePlayer() { player?.release(); player = null }

    override fun onStart() { super.onStart(); initializePlayer() }
    override fun onStop() { super.onStop(); releasePlayer() }

    private fun toggleFullscreen() {
        if (isFullscreen) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            showSystemUI()
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            hideSystemUI()
        }
        isFullscreen = !isFullscreen
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
    }
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}