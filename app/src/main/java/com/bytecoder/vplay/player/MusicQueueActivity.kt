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

        // Keep toolbar back navigation intact
        binding.toolbar.setNavigationOnClickListener { finish() }

        // ------------------- GET CURRENT PLAYLIST -------------------
        val currentIndex = intent.getIntExtra("currentIndex", 0)
        val playlistItems = MusicPlayerManager.uris.mapIndexed { i, uri ->
            val title = MusicPlayerManager.titles.getOrNull(i) ?: uri.lastPathSegment ?: "Item $i"
            // Using simplified MediaItem for adapter; extra fields (folderName, duration) are optional
            MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(
                    com.google.android.exoplayer2.MediaMetadata.Builder()
                        .setTitle(title)
                        .build()
                )
                .build()
        }
        // -------------------------------------------------------------

        // ------------------- ADAPTER SETUP -------------------
        adapter = MusicQueueAdapter(
            items = playlistItems.toMutableList(),
            contentResolver = contentResolver
        ) { index ->
            MusicPlayerManager.jumpTo(index) // Play selected item
            finish()
        }

        adapter.updateQueue(playlistItems, currentIndex)

        binding.recyclerQueue.layoutManager = LinearLayoutManager(this)
        binding.recyclerQueue.adapter = adapter
        // -----------------------------------------------------
    }
}
