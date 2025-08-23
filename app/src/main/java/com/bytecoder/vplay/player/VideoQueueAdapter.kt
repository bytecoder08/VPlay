package com.bytecoder.vplay.player

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
import com.bytecoder.vplay.model.MediaItem
import kotlinx.coroutines.*

class VideoQueueAdapter(
    private val items: MutableList<MediaItem>,
    private val contentResolver: ContentResolver,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<VideoQueueAdapter.VH>() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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
        holder.title.text = item.displayName
        holder.subtitle.text = item.folderName
        holder.thumb.setImageResource(R.drawable.ic_video_placeholder)

        val currentPos = holder.bindingAdapterPosition
        scope.launch {
            val bmp = withContext(Dispatchers.IO) { loadVideoThumb(item) }
            if (holder.bindingAdapterPosition == currentPos && bmp != null) {
                holder.thumb.setImageBitmap(bmp)
            }
        }

        holder.itemView.setOnClickListener { onClick(position) }
    }

    override fun getItemCount(): Int = items.size

    fun updateQueue(newItems: List<MediaItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun loadVideoThumb(item: MediaItem): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.loadThumbnail(item.uri, Size(320, 320), null)
            } else {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(
                    contentResolver.openFileDescriptor(item.uri, "r")?.fileDescriptor
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
