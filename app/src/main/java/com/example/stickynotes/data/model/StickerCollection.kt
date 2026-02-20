package com.example.stickynotes.data.model

data class StickerCollection(
    val name: String,
    val thumbnailRes: Int,
    val stickers: List<Int>
)