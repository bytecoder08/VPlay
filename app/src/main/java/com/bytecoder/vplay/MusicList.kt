package com.bytecoder.vplay

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class MusicList : AppCompatActivity() {
    private val tracks = listOf(
        "Acoustic Breeze" to "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        "Corporate Beat" to "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_list)

        val list = findViewById<ListView>(R.id.musicListView)
        list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tracks.map { it.first })
        list.setOnItemClickListener { _, _, position, _ ->
            val (title, url) = tracks[position]
            startActivity(Intent(this, MusicPlayer::class.java).apply {
                putExtra("title", title)
                putExtra("url", url)
            })
        }
    }
}