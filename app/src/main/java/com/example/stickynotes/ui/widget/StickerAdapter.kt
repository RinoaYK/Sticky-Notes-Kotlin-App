package com.example.stickynotes.ui.widget

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stickynotes.databinding.ItemStickerBinding

class StickerAdapter(
    private val thumbIds: List<Int>,
    private val onClick: (Int) -> Unit
) : ListAdapter<Int, StickerAdapter.ViewHolder>(StickerDiffCallback) {

    class ViewHolder(val binding: ItemStickerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStickerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resId = getItem(position)
        val context = holder.itemView.context

        holder.binding.itemImage.apply {
            setImageResource(resId)

            val isThumbnail = thumbIds.contains(resId)

            if (isThumbnail) {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setPadding(0, 0, 0, 0)
            } else {
                scaleType = ImageView.ScaleType.FIT_CENTER
                val padding = (12 * context.resources.displayMetrics.density).toInt()
                setPadding(padding, padding, padding, padding)
            }
        }

        holder.itemView.setOnClickListener { onClick(resId) }
    }

    fun updateData(newItems: List<Int>) {
        submitList(newItems)
    }

    object StickerDiffCallback : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
        override fun areContentsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
    }
}