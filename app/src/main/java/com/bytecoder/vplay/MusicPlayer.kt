package com.bytecoder.vplay

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bytecoder.vplay.databinding.MusicPlayerBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class MusicPlayer : AppCompatActivity() {

    private lateinit var binding: MusicPlayerBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPlayer()
    }

    private fun initPlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.audioView.player = player

        val audioUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        val mediaItem = MediaItem.fromUri(Uri.parse(audioUri))
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}
