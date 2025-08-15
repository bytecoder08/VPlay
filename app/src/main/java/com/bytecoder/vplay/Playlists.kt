package com.bytecoder.vplay

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Playlists : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlists)

        val fakePlaylists = listOf("Favorites", "Chill Vibes", "Workout Mix")
        val list = findViewById<ListView>(R.id.playlistListView)
        list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fakePlaylists)

        list.setOnItemClickListener { _, _, pos, _ ->
            Toast.makeText(this, "Open playlist: ${fakePlaylists[pos]}", Toast.LENGTH_SHORT).show()
        }
    }
}
