package com.example.stickynotes.data.mapper

import com.example.stickynotes.data.local.entity.WidgetEntity
import com.example.stickynotes.domain.model.WidgetNote

fun WidgetEntity.toDomain(): WidgetNote {
    return WidgetNote(
        id = this.id,
        text = this.text,
        bgColor = this.bgColor,
        textColor = this.textColor,
        fontSize = this.fontSize,
        stickerSize = this.stickerSize,
        imageUri = this.imageUri,
        layoutSize = this.layoutSize,
        imageAlignment = this.imageAlignment
    )
}

fun WidgetNote.toEntity(): WidgetEntity {
    return WidgetEntity(
        id = this.id,
        text = this.text,
        bgColor = this.bgColor,
        textColor = this.textColor,
        fontSize = this.fontSize,
        stickerSize = this.stickerSize,
        imageUri = this.imageUri,
        layoutSize = this.layoutSize,
        imageAlignment = this.imageAlignment
    )
}