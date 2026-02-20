package com.example.stickynotes.ui.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.stickynotes.R

class StickerAdapter(
    private var items: List<Int>,
    private val thumbIds: List<Int>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<StickerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.item_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sticker, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resId = items[position]
        holder.image.setImageResource(resId)

        val isThumbnail = thumbIds.contains(resId)

        if (isThumbnail) {
            holder.image.scaleType = ImageView.ScaleType.CENTER_CROP
            holder.image.setPadding(0, 0, 0, 0)
        } else {
            holder.image.scaleType = ImageView.ScaleType.CENTER_INSIDE
            val padding = (12 * holder.itemView.context.resources.displayMetrics.density).toInt()
            holder.image.setPadding(padding, padding, padding, padding)
        }

        holder.itemView.setOnClickListener { onClick(resId) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<Int>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}