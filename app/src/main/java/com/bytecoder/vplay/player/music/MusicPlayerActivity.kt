package com.bytecoder.vplay.player.music

import android.content.Context
import android.content.Intent

object MusicPlayerActivity {
    fun launch(
        context: Context,
        url: String,
        title: String? = null
    ) {
        val i = Intent(context, MusicPlayer::class.java).apply {
            putExtra("url", url)
            putExtra("title", title)
        }
        context.startActivity(i)
    }
}
