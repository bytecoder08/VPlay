package com.bytecoder.vplay.player

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bytecoder.vplay.databinding.MusicPlayerBinding
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.launch
import com.bytecoder.vplay.R

class MusicPlayer : AppCompatActivity() {

    private lateinit var binding: MusicPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        // Read incoming playlist (if any)
        val uris = intent.getStringArrayListExtra(EXTRA_URIS) ?: arrayListOf()
        val titles = intent.getStringArrayListExtra(EXTRA_TITLES) ?: arrayListOf()
        val index = intent.getIntExtra(EXTRA_INDEX, 0)

        // Start foreground service with playlist
        val svc = Intent(this, MusicPlaybackService::class.java).apply {
            putStringArrayListExtra(MusicPlaybackService.EXTRA_URIS, uris)
            putStringArrayListExtra(MusicPlaybackService.EXTRA_TITLES, titles)
            putExtra(MusicPlaybackService.EXTRA_INDEX, index)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(svc) else startService(svc)

        // Ensure PlayerManager initialized and attach player
        lifecycleScope.launch {
            PlayerManager.ensureInitialized(applicationContext)
            binding.playerView.player = PlayerManager.player
            wireControls()
            refreshUiFromPlayer()
        }
    }

    override fun onStart() {
        super.onStart()
        PlayerManager.player?.addListener(playerListener)
        binding.playerView.player = PlayerManager.player
        refreshUiFromPlayer()
    }

    override fun onStop() {
        PlayerManager.player?.removeListener(playerListener)
        super.onStop()
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlayPause()
        }
        override fun onMediaItemTransition(item: com.google.android.exoplayer2.MediaItem?, reason: Int) {
            refreshUiFromPlayer()
        }
    }

    private fun wireControls() {
        binding.btnPlayPause.setOnClickListener {
            PlayerManager.player?.let { p -> p.playWhenReady = !p.isPlaying }
            updatePlayPause()
        }
        binding.btnNext.setOnClickListener { PlayerManager.player?.seekToNextMediaItem() }
        binding.btnPrev.setOnClickListener { PlayerManager.player?.seekToPreviousMediaItem() }
        binding.btnShuffle.setOnClickListener {
            PlayerManager.player?.let { p ->
                p.shuffleModeEnabled = !(p.shuffleModeEnabled)
                binding.btnShuffle.alpha = if (p.shuffleModeEnabled) 1f else 0.5f
            }
        }
        binding.btnRepeat.setOnClickListener {
            PlayerManager.player?.let { p ->
                p.repeatMode = when (p.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                    else -> Player.REPEAT_MODE_OFF
                }
                // update icon alpha
                binding.btnRepeat.alpha = if (p.repeatMode == Player.REPEAT_MODE_OFF) 0.5f else 1f
            }
        }
        binding.btnQueue.setOnClickListener {
            startActivity(Intent(this, QueueActivity::class.java))
        }
    }

    private fun refreshUiFromPlayer() {
        val p = PlayerManager.player ?: return
        val idx = p.currentMediaItemIndex.coerceAtLeast(0)
        binding.tvTitle.text = PlayerManager.titles.getOrNull(idx) ?: PlayerManager.uris.getOrNull(idx)?.lastPathSegment ?: getString(R.string.app_name)
        binding.tvSubtitle.text = PlayerManager.uris.getOrNull(idx)?.path ?: ""
        // album art update is handled by PlayerManager notification; we set placeholder
        binding.albumArt.setImageResource(R.drawable.ic_album_placeholder)
        updatePlayPause()
    }

    private fun updatePlayPause() {
        val playing = PlayerManager.player?.isPlaying == true
        binding.btnPlayPause.setImageResource(if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
    }

    companion object {
        const val EXTRA_URIS = "uris"
        const val EXTRA_TITLES = "titles"
        const val EXTRA_INDEX = "index"
    }
}
