package com.bytecoder.vplay

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Homepage : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        findViewById<Button>(R.id.btnVideos).setOnClickListener {
            startActivity(Intent(this, VideoList::class.java))
        }
        findViewById<Button>(R.id.btnMusic).setOnClickListener {
            startActivity(Intent(this, MusicList::class.java))
        }
        findViewById<Button>(R.id.btnOnline).setOnClickListener {
            startActivity(Intent(this, Online::class.java))
        }
        findViewById<Button>(R.id.btnPlaylists).setOnClickListener {
            startActivity(Intent(this, Playlists::class.java))
        }
        findViewById<Button>(R.id.btnOptions).setOnClickListener {
            startActivity(Intent(this, Options::class.java))
        }
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
        }
        findViewById<Button>(R.id.btnPermissions).setOnClickListener {
            startActivity(Intent(this, GetPermissions::class.java))
        }
    }
}