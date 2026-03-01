package com.example.stickynotes.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "widget_notes")
data class WidgetNote(
    @PrimaryKey val id: Int,
    val text: String,
    val bgColor: Int,
    val textColor: Int,
    val fontSize: Float,
    val stickerSize: Int,
    val imageUri: String?,
    val layoutSize: String,
    val imageAlignment: String
)
