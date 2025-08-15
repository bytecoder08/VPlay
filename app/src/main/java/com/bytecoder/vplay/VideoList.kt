package com.bytecoder.vplay

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class VideoList : AppCompatActivity() {
    private val videos = listOf(
        // title to url map; replace with your data source later
        "Big Buck Bunny" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "Sintel (HLS)" to "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_list)

        val list = findViewById<ListView>(R.id.videoListView)
        list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, videos.map { it.first })

        list.setOnItemClickListener { _, _, position, _ ->
            val (title, url) = videos[position]
            startActivity(Intent(this, VideoPlayer::class.java).apply {
                putExtra("title", title)
                putExtra("url", url)
            })
        }
    }
}