package com.bytecoder.vplay.player

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bytecoder.vplay.R
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.MimeTypes
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class VideoPlayer : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var btnFullscreen: ImageButton
    private lateinit var btnRotateMode: ImageButton
    private lateinit var btnLock: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var tvTime: TextView
    private lateinit var overlayHint: TextView
    private lateinit var overlayIcon: ImageView
    private lateinit var brightnessBar: SeekBar
    private lateinit var volumeBar: SeekBar
    private lateinit var bottomBar: View

    private var locked = false
    private var showingRemaining = false
    private var rotateMode = RotateMode.SENSOR
    private var isFullscreen = false
    private var longPressBoosting = false

    private lateinit var audioManager: AudioManager
    private var maxVolume = 0

    private val uiHandler = Handler(Looper.getMainLooper())
    private val timeUpdate = object : Runnable {
        override fun run() {
            updateTimeText()
            uiHandler.postDelayed(this, 500)
        }
    }

    private enum class RotateMode { SENSOR, PORTRAIT, LANDSCAPE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_player)

        // Keep screen on while playing video
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        bindViews()
        setupButtons()
        setupPlayerAndPlay()
        setupGestures()
    }

    private fun bindViews() {
        playerView = findViewById(R.id.playerView)

        btnFullscreen = findViewById(R.id.btnFullscreen)
        btnRotateMode = findViewById(R.id.btnRotateMode)
        btnLock = findViewById(R.id.btnLock)

        tvTitle = findViewById(R.id.tvTitle)
        tvTime = findViewById(R.id.tvTime)
        overlayHint = findViewById(R.id.overlayHint)
        overlayIcon = findViewById(R.id.overlayIcon)

        brightnessBar = findViewById(R.id.brightnessBar)
        volumeBar = findViewById(R.id.volumeBar)
        bottomBar = findViewById(R.id.bottomBar)

        // Setup bars
        brightnessBar.max = 100
        volumeBar.max = maxVolume
        volumeBar.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        // Initial title
        tvTitle.text = intent.getStringExtra("title") ?: getString(R.string.app_name)
    }

    private fun setupButtons() {
        btnFullscreen.setOnClickListener { toggleFullscreen() }
        btnRotateMode.setOnClickListener { cycleRotateMode() }
        btnLock.setOnClickListener { toggleLock() }

        tvTime.setOnClickListener {
            showingRemaining = !showingRemaining
            updateTimeText()
        }
    }

    private fun setupPlayerAndPlay() {
        VideoPlayerManager.ensureInitialized(this)

        playerView.player = VideoPlayerManager.player
        playerView.controllerShowTimeoutMs = 3000
        playerView.controllerHideOnTouch = true

        // Load MediaItem
        val url = intent.getStringExtra("url") ?: run {
            Toast.makeText(this, "No video URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val title = intent.getStringExtra("title") ?: Uri.parse(url).lastPathSegment ?: "Video"

        val subtitleUrl = intent.getStringExtra("subtitle_url") // optional
        val mediaItemBuilder = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .build()
            )

        if (!subtitleUrl.isNullOrBlank()) {
            val subs = MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitleUrl))
                .setMimeType(MimeTypes.TEXT_VTT)
                .setLanguage("en")
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()
            mediaItemBuilder.setSubtitleConfigurations(listOf(subs))
        }

        val mediaItem = mediaItemBuilder.build()
        VideoPlayerManager.setMediaItem(mediaItem, playWhenReady = true)

        // Start the service for ongoing playback notification
        VideoPlaybackService.startForeground(this, title)

        uiHandler.post(timeUpdate)
    }

    override fun onStart() {
        super.onStart()
        VideoPlayerManager.play()
    }

    override fun onStop() {
        super.onStop()
        uiHandler.removeCallbacks(timeUpdate)
        // We DO NOT release the player here so playback can continue in background
    }

    override fun onDestroy() {
        super.onDestroy()
        // Do not release here if you want background; VideoPlaybackService owns lifecycle
    }

    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        if (isFullscreen) {
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen_exit)
            enterFullscreen()
        } else {
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen)
            exitFullscreen()
        }
    }

    private fun enterFullscreen() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    private fun exitFullscreen() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    private fun cycleRotateMode() {
        rotateMode = when (rotateMode) {
            RotateMode.SENSOR -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                btnRotateMode.setImageResource(R.drawable.ic_rotate) // same icon; could vary if you want
                RotateMode.PORTRAIT
            }
            RotateMode.PORTRAIT -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                btnRotateMode.setImageResource(R.drawable.ic_rotate)
                RotateMode.LANDSCAPE
            }
            RotateMode.LANDSCAPE -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                btnRotateMode.setImageResource(R.drawable.ic_rotate)
                RotateMode.SENSOR
            }
        }
        toastShort("Rotation: $rotateMode")
    }

    private fun toggleLock() {
        locked = !locked
        if (locked) {
            btnLock.setImageResource(R.drawable.ic_lock)
            playerView.useController = false
            toastShort("Controls locked")
        } else {
            btnLock.setImageResource(R.drawable.ic_unlock)
            playerView.useController = true
            toastShort("Controls unlocked")
        }
    }

    private fun updateTimeText() {
        val p = VideoPlayerManager.player ?: return
        val current = max(0L, p.currentPosition)
        val total = max(0L, p.duration)
        val text = if (showingRemaining && total > 0) {
            "−${formatTime(total - current)} / ${formatTime(total)}"
        } else {
            "${formatTime(current)} / ${if (total > 0) formatTime(total) else "--:--"}"
        }
        tvTime.text = text
    }

    private fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
        else String.format("%02d:%02d", m, s)
    }

    // -------------------------
    // Gestures & Overlays
    // -------------------------
    private fun setupGestures() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (locked) return true
                if (playerView.isControllerVisible) playerView.hideController()
                else playerView.showController()
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (locked) return true
                val width = playerView.width
                val x = e.x
                if (x < width / 2f) {
                    seekBy(-10_000)
                    showSeekOverlay("−10s")
                } else {
                    seekBy(+10_000)
                    showSeekOverlay("+10s")
                }
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                if (locked) return
                setTempPlaybackSpeed(1.5f)
            }
        })

        val touchView = findViewById<View>(R.id.touchSurface)
        var startX = 0f
        var startY = 0f
        var adjustingBrightness = false
        var adjustingVolume = false

        touchView.setOnTouchListener { _, event ->
            if (locked) return@setOnTouchListener true

            // Pass to detector first
            gestureDetector.onTouchEvent(event)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    adjustingBrightness = false
                    adjustingVolume = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - startX
                    val dy = event.y - startY

                    if (!adjustingBrightness && !adjustingVolume) {
                        if (abs(dy) > abs(dx)) {
                            // vertical swipe
                            if (startX < touchView.width / 2f) {
                                adjustingBrightness = true
                            } else {
                                adjustingVolume = true
                            }
                        }
                    }
                    if (adjustingBrightness) {
                        val delta = -dy / touchView.height // up increases
                        adjustBrightness(delta)
                    } else if (adjustingVolume) {
                        val delta = -dy / touchView.height // up increases
                        adjustVolume(delta)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (longPressBoosting) {
                        resetPlaybackSpeed()
                    }
                    hideOverlaySoon()
                }
            }
            true
        }
    }

    private fun adjustBrightness(delta: Float) {
        val lp = window.attributes
        var b = if (lp.screenBrightness < 0f) 0.5f else lp.screenBrightness // default mid if unknown
        b += delta
        b = min(1f, max(0f, b))
        lp.screenBrightness = b
        window.attributes = lp

        brightnessBar.visibility = View.VISIBLE
        brightnessBar.progress = (b * 100).toInt()
        overlayIcon.setImageResource(R.drawable.ic_unlock) // simple white icon; use sun icon if you have one
        overlayHint.text = "Brightness: ${(b * 100).toInt()}%"
        showOverlay()
    }

    private fun adjustVolume(delta: Float) {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        var newVol = current + (delta * maxVolume).toInt()
        newVol = min(maxVolume, max(0, newVol))
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)

        volumeBar.visibility = View.VISIBLE
        volumeBar.progress = newVol
        overlayIcon.setImageResource(R.drawable.ic_lock) // simple white icon; use volume icon if you have one
        overlayHint.text = "Volume: ${((newVol.toFloat() / maxVolume) * 100).toInt()}%"
        showOverlay()
    }

    private fun seekBy(ms: Long) {
        val p = VideoPlayerManager.player ?: return
        var newPos = p.currentPosition + ms
        newPos = max(0L, min(newPos, if (p.duration > 0) p.duration else Long.MAX_VALUE))
        p.seekTo(newPos)
    }

    private fun setTempPlaybackSpeed(speed: Float) {
        val p = VideoPlayerManager.player ?: return
        longPressBoosting = true
        p.setPlaybackSpeed(speed)
        overlayHint.text = "Speed ${speed}×"
        showOverlay()
    }

    private fun resetPlaybackSpeed() {
        val p = VideoPlayerManager.player ?: return
        longPressBoosting = false
        p.setPlaybackSpeed(1.0f)
        hideOverlaySoon()
    }

    private fun showSeekOverlay(text: String) {
        overlayHint.text = text
        showOverlay()
    }

    private fun showOverlay() {
        overlayHint.visibility = View.VISIBLE
        overlayIcon.visibility = View.VISIBLE
        uiHandler.removeCallbacks(hideOverlayRunnable)
        uiHandler.postDelayed(hideOverlayRunnable, 800)
    }

    private val hideOverlayRunnable = Runnable {
        overlayHint.visibility = View.GONE
        overlayIcon.visibility = View.GONE
        brightnessBar.visibility = View.GONE
        volumeBar.visibility = View.GONE
    }

    private fun hideOverlaySoon() {
        uiHandler.removeCallbacks(hideOverlayRunnable)
        uiHandler.postDelayed(hideOverlayRunnable, 500)
    }

    private fun toastShort(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
