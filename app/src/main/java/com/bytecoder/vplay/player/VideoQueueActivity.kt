package com.bytecoder.vplay.player

import android.content.ContentResolver
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bytecoder.vplay.databinding.ActivityQueueBinding

class VideoQueueActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQueueBinding
    private lateinit var adapter: VideoQueueAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQueueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar back navigation
        binding.toolbar.setNavigationOnClickListener { finish() }

        // ------------------- INITIALIZE ADAPTER -------------------
        val queue = VideoPlayerManager.getQueue()
        adapter = VideoQueueAdapter(queue.toMutableList(), contentResolver) { index ->
            VideoPlayerManager.jumpTo(index) // Play selected video
            finish() // Close queue and return to player
        }

        binding.recyclerQueue.layoutManager = LinearLayoutManager(this)
        binding.recyclerQueue.adapter = adapter
        // -----------------------------------------------------------

        // ------------------- DYNAMIC QUEUE UPDATES -------------------
        VideoPlayerManager.setQueueListener { newQueue ->
            adapter.updateQueue(newQueue)
        }
        // -----------------------------------------------------------
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove listener to prevent memory leaks
        VideoPlayerManager.setQueueListener(null)
    }
}
