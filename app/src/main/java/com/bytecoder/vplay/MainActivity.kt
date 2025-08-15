package com.bytecoder.vplay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Optionally route to Homepage immediately
        startActivity(Intent(this, Homepage::class.java))
        finish()
    }
}
