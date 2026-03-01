package com.example.stickynotes.ui.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.stickynotes.R
import com.example.stickynotes.domain.model.WidgetNote

class WidgetGalleryAdapter(
    private var items: List<WidgetNote>,
    private val onItemClick: (WidgetNote) -> Unit,
    private val onDeleteClick: (WidgetNote) -> Unit
) : RecyclerView.Adapter<WidgetGalleryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val background: LinearLayout = view.findViewById(R.id.item_background)
        val text: TextView = view.findViewById(R.id.item_text)
        val sticker: ImageView = view.findViewById(R.id.item_sticker)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_note)

        fun bind(note: WidgetNote) {
            background.setBackgroundColor(note.bgColor)
            text.text = note.text
            text.setTextColor(note.textColor)

            note.imageUri?.let {
                sticker.setImageURI(it.toUri())
                sticker.visibility = View.VISIBLE
            } ?: run {
                sticker.visibility = View.GONE
            }

            background.layoutDirection = if (note.imageAlignment == "LEFT_CENTER") {
                View.LAYOUT_DIRECTION_RTL
            } else {
                View.LAYOUT_DIRECTION_LTR
            }

            itemView.setOnClickListener { onItemClick(note) }

            btnDelete.setOnClickListener {
                onDeleteClick(note)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_widget_gallery, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<WidgetNote>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}