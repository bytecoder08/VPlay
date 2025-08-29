package com.bytecoder.vplay.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.bytecoder.vplay.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.bytecoder.vplay.player.music.MusicPlayerManager
import com.bytecoder.vplay.player.video.VideoPlayerManager
import com.bytecoder.vplay.player.PlayerLauncher

class MiniPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val root: View
    private val titleView: TextView
    private val thumbImage: ImageView
    private val playerView: PlayerView
    private val playPauseBtn: ImageButton
    private val closeBtn: ImageButton
    private val progressBar: ProgressBar

    private var exoPlayer: ExoPlayer? = null
    private var isVideoMode = false

    private val animDuration = 300L
    private val animInterpolator = DecelerateInterpolator(2.0f)
    private var isAnimatingIn = false
    private var isAnimatingOut = false
    private var currentlyShown = false


    interface Host {
        fun isPlaybackActive(): Boolean
        fun getCurrentTitle(): String?
        fun getCurrentMediaUri(): String?
        fun isCurrentMediaVideo(): Boolean
        fun getPlaybackPositionMs(): Long
        fun getPlaybackDurationMs(): Long
        fun isPlaying(): Boolean
        fun togglePlayPause()
        fun stopAndClearQueue()
        fun openFullPlayer(context: Context)
        fun subscribePlaybackUpdates(listener: () -> Unit)
        fun unsubscribePlaybackUpdates(listener: () -> Unit)
    }

    private var host: Host? = null
    private var updateListener: (() -> Unit)? = null

    init {
        root = LayoutInflater.from(context).inflate(R.layout.mini_player, this, true)
        titleView = root.findViewById(R.id.mini_title)
        thumbImage = root.findViewById(R.id.mini_thumb)
        playerView = root.findViewById(R.id.mini_player_view)
        playPauseBtn = root.findViewById(R.id.mini_play_pause)
        closeBtn = root.findViewById(R.id.mini_close)
        progressBar = root.findViewById(R.id.mini_progress)

        playerView.visibility = View.GONE

        root.setOnClickListener {
            host?.openFullPlayer(context)
        }

        playPauseBtn.setOnClickListener {
            host?.togglePlayPause()
            refreshPlayPauseIcon()
        }
        closeBtn.setOnClickListener {
            host?.stopAndClearQueue()
            hideMini()
        }

        post {
            val h = height.takeIf { it > 0 } ?: measuredHeight
            translationY = h.toFloat()
            alpha = 0f
            visibility = View.GONE
        }
    }

    fun attach(host: Host, lifecycleOwner: LifecycleOwner?) {
        this.host = host
        updateListener = { refreshUi() }
        host.subscribePlaybackUpdates(updateListener!!)
        refreshUi()
    }

    fun detach() {
        updateListener?.let { host?.unsubscribePlaybackUpdates(it)
        }
        updateListener = null
        host = null
        releaseExo()
    }

    private fun refreshUi() {
        val h = host ?: return
        if (!h.isPlaybackActive() || PlayerLauncher.isFullPlayerActive()) {
            //visibility = View.GONE
            hideMini()
            return
        }
        //visibility = View.VISIBLE
        showMini()

        titleView.text = h.getCurrentTitle() ?: context.getString(R.string.media_title)

        isVideoMode = h.isCurrentMediaVideo()

        if (isVideoMode) {
            thumbImage.visibility = View.GONE
            playerView.visibility = View.VISIBLE
            ensureExoPlayer()
            exoPlayer?.let { exo ->
                val uri = h.getCurrentMediaUri()
                if (uri != null) {
                    val cur = exo.currentMediaItem
                    if (cur == null || cur.localConfiguration?.uri.toString() != uri) {
                        exo.setMediaItem(MediaItem.fromUri(uri))
                        exo.prepare()
                        if (h.isPlaying()) exo.play() else exo.pause()
                    } else {
                        if (h.isPlaying()) exo.play() else exo.pause()
                    }
                }
            }
        } else {
            playerView.visibility = View.GONE
            releaseExo()
            thumbImage.visibility = View.VISIBLE
            thumbImage.contentDescription = h.getCurrentTitle()
        }

        val duration = h.getPlaybackDurationMs()
        val pos = h.getPlaybackPositionMs()
        if (duration > 0) {
            progressBar.max = duration.toInt()
            progressBar.progress = pos.toInt()
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.INVISIBLE
        }

        refreshPlayPauseIcon()
    }

    private fun refreshPlayPauseIcon() {
        val playing = host?.isPlaying() ?: false
        playPauseBtn.setImageResource(if (playing) R.drawable.pause_24px else R.drawable.play_arrow_24px)
    }

    private fun ensureExoPlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
            playerView.player = exoPlayer
            playerView.useController = false
        }
    }

    private fun releaseExo() {
        exoPlayer?.let {
            it.stop()
            it.release()
        }
        exoPlayer = null
        playerView.player = null
    }

    private fun showMini() {
        if (currentlyShown || isAnimatingIn) return
        isAnimatingOut = false
        isAnimatingIn = true

        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
            post {
                if (translationY <= 0f) {
                    translationY = (height.takeIf { it > 0 } ?: measuredHeight).toFloat()
                }
                animate()
                    .translationY(0f)     // slide up
                    .alpha(1f)            // fade in
                    .setDuration(animDuration)
                    .setInterpolator(animInterpolator)
                    .withEndAction {
                        isAnimatingIn = false
                        currentlyShown = true
                    }
                    .start()
            }
        } else {
            animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(animDuration)
                .setInterpolator(animInterpolator)
                .withEndAction {
                    isAnimatingIn = false
                    currentlyShown = true
                }
                .start()
        }
    }

    private fun hideMini() {
        if (!currentlyShown || isAnimatingOut) {
            if (visibility == View.VISIBLE && !isAnimatingOut && !isAnimatingIn) {
                performHideAnimation()
            }
            return
        }
        performHideAnimation()
    }

    private fun performHideAnimation() {
        isAnimatingOut = true
        isAnimatingIn = false
        val endY = (height.takeIf { it > 0 } ?: measuredHeight).toFloat()
        animate()
            .translationY(endY)   // slide down
            .alpha(0f)            // fade out
            .setDuration(animDuration)
            .setInterpolator(animInterpolator)
            .withEndAction {
                visibility = View.GONE
                isAnimatingOut = false
                currentlyShown = false
                translationY = endY
            }
            .start()
    }
}
