package com.bytecoder.vplay.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bytecoder.vplay.R
import java.io.File

class ExplorerAdapter(
    private var items: List<File>,
    private val onClick: (File) -> Unit
) : RecyclerView.Adapter<ExplorerAdapter.ExplorerViewHolder>() {

    inner class ExplorerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.fileIcon)
        val name: TextView = itemView.findViewById(R.id.fileName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExplorerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return ExplorerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExplorerViewHolder, position: Int) {
        val file = items[position]
        holder.name.text = file.name
        holder.icon.setImageResource(
            if (file.isDirectory) R.drawable.folder_24px else R.drawable.file_24px
        )
        holder.itemView.setOnClickListener { onClick(file) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<File>) {
        items = newItems
        notifyDataSetChanged()
    }
}
