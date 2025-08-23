package com.bytecoder.vplay.adapters

import android.content.Context
import android.webkit.MimeTypeMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bytecoder.vplay.R
import com.bytecoder.vplay.player.PlayerLauncher
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

        // --- CHANGED: Added PlayerLauncher for audio/video files ---
        holder.itemView.setOnClickListener {
            if (file.isDirectory) {
                onClick(file)
            } else {
                val extension = file.extension.lowercase()
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: ""

                if (mimeType.startsWith("audio") || mimeType.startsWith("video") ||
                    extension in listOf("mp3", "aac", "wav", "mp4", "mkv", "3gp")) {
                    // Play audio/video files
                    PlayerLauncher.play(
                        context = it.context,
                        fileOrUrl = file.path,
                        title = file.name
                    )
                } else {
                    // Other files → keep previous behavior
                    Toast.makeText(it.context, "File: ${file.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<File>) {
        items = newItems
        notifyDataSetChanged()
    }
}
