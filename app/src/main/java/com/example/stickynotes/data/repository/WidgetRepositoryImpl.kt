package com.example.stickynotes.data.repository

import com.example.stickynotes.data.local.dao.WidgetDao
import com.example.stickynotes.data.mapper.toDomain
import com.example.stickynotes.data.mapper.toEntity
import com.example.stickynotes.domain.model.WidgetNote
import com.example.stickynotes.domain.repository.WidgetRepository
import javax.inject.Inject

class WidgetRepositoryImpl @Inject constructor(
    private val dao: WidgetDao
) : WidgetRepository {

    override suspend fun getWidgetById(id: Int): WidgetNote? {
        return dao.getWidgetById(id)?.toDomain()
    }

    override suspend fun saveWidget(widget: WidgetNote) {
        dao.saveWidget(widget.toEntity())
    }

    override suspend fun deleteWidget(id: Int) {
        dao.deleteWidget(id)
    }

    override suspend fun getAllWidgets(): List<WidgetNote> {
        return dao.getAllWidgets().map { it.toDomain() }
    }
}