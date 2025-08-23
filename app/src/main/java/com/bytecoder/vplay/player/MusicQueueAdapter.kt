package com.bytecoder.vplay.player

import android.content.ContentResolver
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bytecoder.vplay.R
import com.bytecoder.vplay.model.MediaItem
import kotlinx.coroutines.*

class
MusicQueueAdapter(
    private val items: MutableList<MediaItem>,
    private val contentResolver: ContentResolver,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<MusicQueueAdapter.VH>() {

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
        holder.title.text = item.displayName
        holder.subtitle.text = item.folderName

        holder.itemView.setBackgroundColor(
            if (position == currentIndex) 0x3300FF00 else 0x00000000
        )

        holder.thumb.setImageResource(R.drawable.ic_music_placeholder)

        val currentPos = holder.bindingAdapterPosition
        scope.launch {
            val bmp = withContext(Dispatchers.IO) { loadAlbumArt(item) }
            if (holder.bindingAdapterPosition == currentPos && bmp != null) {
                holder.thumb.setImageBitmap(bmp)
            }
        }

        holder.itemView.setOnClickListener {
            currentIndex = position
            notifyDataSetChanged()
            onClick(position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateQueue(newItems: List<MediaItem>, current: Int = -1) {
        items.clear()
        items.addAll(newItems)
        currentIndex = current
        notifyDataSetChanged()
    }

    private fun loadAlbumArt(item: MediaItem): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(contentResolver.openFileDescriptor(item.uri, "r")?.fileDescriptor)
            val art = retriever.embeddedPicture
            retriever.release()
            if (art != null) android.graphics.BitmapFactory.decodeByteArray(art, 0, art.size) else null
        } catch (_: Exception) { null }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        scope.cancel()
    }
}
