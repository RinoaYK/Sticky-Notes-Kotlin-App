package com.example.stickynotes.domain.repository

import com.example.stickynotes.domain.model.WidgetNote

interface WidgetRepository {
    suspend fun getWidgetById(id: Int): WidgetNote?
    suspend fun saveWidget(widget: WidgetNote)
    suspend fun deleteWidget(id: Int)

    suspend fun getAllWidgets(): List<WidgetNote>
}