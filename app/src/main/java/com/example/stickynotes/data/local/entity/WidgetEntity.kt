package com.example.stickynotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "widgets")
data class WidgetEntity(
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