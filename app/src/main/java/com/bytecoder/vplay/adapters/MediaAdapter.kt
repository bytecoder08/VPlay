package com.bytecoder.vplay.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bytecoder.vplay.R

class MediaAdapter(
    private val items: List<String>,
    private var isGrid: Boolean
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.itemImage)
        val title: TextView = itemView.findViewById(R.id.itemTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val layout = if (isGrid) R.layout.item_media_grid else R.layout.item_media_list
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.title.text = items[position]
        holder.img.setImageResource(R.drawable.ic_launcher_foreground)
    }

    override fun getItemCount(): Int = items.size

    fun setViewType(grid: Boolean) {
        isGrid = grid
        notifyDataSetChanged()
    }
}
