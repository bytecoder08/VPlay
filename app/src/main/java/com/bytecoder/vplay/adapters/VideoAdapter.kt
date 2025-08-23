package com.bytecoder.vplay.adapters

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
import com.bytecoder.vplay.player.VideoPlayerManager
import kotlinx.coroutines.*
import java.lang.Exception

class VideoAdapter(
    private var items: List<MediaItem>,
    private var isGrid: Boolean,
    private val contentResolver: ContentResolver,
    private val onClick: (MediaItem) -> Unit = {}
) : RecyclerView.Adapter<VideoAdapter.VH>() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun submit(list: List<MediaItem>) {
        items = list
        notifyDataSetChanged()
    }

    fun setGrid(grid: Boolean) {
        isGrid = grid
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layout = if (isGrid) R.layout.item_media_grid else R.layout.item_media_list
        val v = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.title.text = item.displayName
        holder.subtitle.text = item.folderName

        holder.thumb.setImageResource(android.R.drawable.ic_media_play)

        // --- ADDED: click listener with PlayerLauncher ---
        holder.itemView.setOnClickListener {
            VideoPlayerManager.setQueue(items)
            val index = items.indexOf(item)
            if (index >= 0) VideoPlayerManager.jumpTo(index)

            com.bytecoder.vplay.player.PlayerLauncher.play(
                context = it.context,
                fileOrUrl = item.uri.path ?: "",
                title = item.displayName
            )

            onClick(item)
        }

        val currentPos = holder.bindingAdapterPosition
        scope.launch {
            val bmp = withContext(Dispatchers.IO) { loadVideoThumb(item) }
            if (holder.bindingAdapterPosition == currentPos && bmp != null) {
                holder.thumb.setImageBitmap(bmp)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        scope.cancel()
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumb: ImageView = itemView.findViewById(R.id.itemThumbnail)
        val title: TextView = itemView.findViewById(R.id.itemTitle)
        val subtitle: TextView = itemView.findViewById(R.id.itemSubtitle)
    }

    private fun loadVideoThumb(item: MediaItem): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.loadThumbnail(item.uri, Size(320, 320), null)
            } else {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(itemViewCR(), item.uri)
                val bmp = retriever.frameAtTime
                retriever.release()
                bmp
            }
        } catch (_: Exception) { null }
    }

    private fun itemViewCR(): android.content.Context {
        return try {
            val field = ContentResolver::class.java.getDeclaredField("mContext")
            field.isAccessible = true
            (field.get(contentResolver) as? android.content.Context) ?: throw IllegalStateException()
        } catch (_: Exception) {
            throw IllegalStateException("Context not available for legacy thumbnail load")
        }
    }
}
