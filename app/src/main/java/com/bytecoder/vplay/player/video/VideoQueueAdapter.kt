package com.bytecoder.vplay.player.video

import android.content.ContentResolver
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bytecoder.vplay.R
import com.google.android.exoplayer2.MediaItem
import kotlinx.coroutines.*

class VideoQueueAdapter(
    private val items: MutableList<MediaItem>,
    private val contentResolver: ContentResolver,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<VideoQueueAdapter.VH>() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentIndex: Int = -1

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.itemTitle)
        val subtitle: TextView = itemView.findViewById(R.id.itemSubtitle)
        val thumb: ImageView = itemView.findViewById(R.id.itemThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media_list, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.title.text = item.mediaMetadata.title ?: "Video"
        holder.subtitle.text = item.mediaMetadata.artist ?: ""
        holder.thumb.setImageResource(R.drawable.movie_24px)

        val currentPos = holder.bindingAdapterPosition
        scope.launch {
            val bmp = withContext(Dispatchers.IO) { loadVideoThumb(item) }
            if (holder.bindingAdapterPosition == currentPos && bmp != null) {
                holder.thumb.setImageBitmap(bmp)
            }
        }

        holder.itemView.setBackgroundColor(
            if (position == currentIndex) 0x3300FF00 else 0x00000000
        )

        holder.itemView.setOnClickListener {
            val position = holder.bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return@setOnClickListener
            currentIndex = position
            notifyDataSetChanged()
            onClick(position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateQueue(newItems: List<MediaItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun loadVideoThumb(item: MediaItem): Bitmap? {
        val mediaUri = item.localConfiguration?.uri ?: return null
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.loadThumbnail(mediaUri, Size(320, 320), null)
            } else {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(
                    contentResolver.openFileDescriptor(mediaUri, "r")?.fileDescriptor
                )
                val bmp = retriever.frameAtTime
                retriever.release()
                bmp
            }
        } catch (_: Exception) { null }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        scope.cancel()
    }
}
