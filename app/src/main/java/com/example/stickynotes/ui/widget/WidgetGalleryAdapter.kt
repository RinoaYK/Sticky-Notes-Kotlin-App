package com.example.stickynotes.ui.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stickynotes.databinding.ItemWidgetGalleryBinding
import com.example.stickynotes.domain.model.WidgetNote

class WidgetGalleryAdapter(
    private val onItemClick: (WidgetNote) -> Unit,
    private val onDeleteClick: (WidgetNote) -> Unit
) : ListAdapter<WidgetNote, WidgetGalleryAdapter.ViewHolder>(WidgetNoteDiffCallback) {

    inner class ViewHolder(private val binding: ItemWidgetGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: WidgetNote) {
            binding.apply {
                itemBackground.setBackgroundColor(note.bgColor)
                itemText.text = note.text
                itemText.setTextColor(note.textColor)

                note.imageUri?.let { uriString ->
                    itemSticker.setImageURI(uriString.toUri())
                    itemSticker.visibility = View.VISIBLE
                } ?: run {
                    itemSticker.visibility = View.GONE
                }

                itemBackground.layoutDirection = if (note.imageAlignment == "LEFT_CENTER") {
                    View.LAYOUT_DIRECTION_RTL
                } else {
                    View.LAYOUT_DIRECTION_LTR
                }

                root.setOnClickListener { onItemClick(note) }
                btnDeleteNote.setOnClickListener { onDeleteClick(note) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWidgetGalleryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateData(newItems: List<WidgetNote>?) {
        submitList(newItems)
    }

    object WidgetNoteDiffCallback : DiffUtil.ItemCallback<WidgetNote>() {
        override fun areItemsTheSame(oldItem: WidgetNote, newItem: WidgetNote): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WidgetNote, newItem: WidgetNote): Boolean {
            return oldItem == newItem
        }
    }
}