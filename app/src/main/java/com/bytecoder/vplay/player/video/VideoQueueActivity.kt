package com.bytecoder.vplay.player.video

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

        binding.toolbar.setNavigationOnClickListener { finish() }

        val queue = VideoPlayerManager.getQueue()
        adapter = VideoQueueAdapter(queue.toMutableList(), contentResolver) { index ->
            VideoPlayerManager.jumpTo(index)
            finish()
        }

        binding.recyclerQueue.layoutManager = LinearLayoutManager(this)
        binding.recyclerQueue.adapter = adapter
        VideoPlayerManager.setQueueListener { newQueue ->
            adapter.updateQueue(newQueue)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        VideoPlayerManager.setQueueListener(null)
    }
}
