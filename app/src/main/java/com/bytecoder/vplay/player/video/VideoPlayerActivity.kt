package com.bytecoder.vplay.player.video

import android.content.Context
import android.content.Intent

object VideoPlayerActivity {
    fun launch(
        context: Context,
        url: String,
        title: String? = null,
        subtitleUrl: String? = null
    ) {
        val i = Intent(context, VideoPlayer::class.java).apply {
            putExtra("url", url)
            putExtra("title", title)
            putExtra("subtitle_url", subtitleUrl)
        }
        context.startActivity(i)
    }
}
