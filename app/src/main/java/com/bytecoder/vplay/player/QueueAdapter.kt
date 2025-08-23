package com.bytecoder.vplay.player

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bytecoder.vplay.R

class QueueAdapter(
    private val items: MutableList<Pair<Int, String>>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<QueueAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_queue, parent, false)
        return VH(v as android.view.View)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (idx, title) = items[position]
        holder.title.text = "${position + 1}. $title"
        holder.itemView.setOnClickListener { onClick(idx) }
    }

    override fun getItemCount(): Int = items.size

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvQueueTitle)
    }
}
