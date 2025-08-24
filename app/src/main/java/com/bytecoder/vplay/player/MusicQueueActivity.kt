package com.bytecoder.vplay.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bytecoder.vplay.databinding.ActivityQueueBinding
import com.google.android.exoplayer2.MediaItem

class MusicQueueActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQueueBinding
    private lateinit var adapter: MusicQueueAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQueueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        val currentIndex = intent.getIntExtra("currentIndex", 0)
        val playlistItems = MusicPlayerManager.uris.mapIndexed { i, uri ->
            val title = MusicPlayerManager.titles.getOrNull(i) ?: uri.lastPathSegment ?: "Item $i"
            MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(
                    com.google.android.exoplayer2.MediaMetadata.Builder()
                        .setTitle(title)
                        .build()
                )
                .build()
        }.toMutableList()
        adapter = MusicQueueAdapter(
            items = playlistItems
        ) { index ->
            MusicPlayerManager.jumpTo(index)
            finish()
        }

        adapter.updateQueue(playlistItems, currentIndex)

        binding.recyclerQueue.layoutManager = LinearLayoutManager(this)
        binding.recyclerQueue.adapter = adapter
    }
}
