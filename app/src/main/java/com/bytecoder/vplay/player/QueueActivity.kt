package com.bytecoder.vplay.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bytecoder.vplay.databinding.ActivityQueueBinding

class QueueActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQueueBinding
    private lateinit var adapter: QueueAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQueueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        val queue = PlayerManager.getQueue()
        adapter = QueueAdapter(queue.toMutableList()) { index ->
            PlayerManager.jumpTo(index)
            finish()
        }

        binding.recyclerQueue.layoutManager = LinearLayoutManager(this)
        binding.recyclerQueue.adapter = adapter
    }
}
