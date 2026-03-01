package com.example.stickynotes.data.local.dao

import androidx.room.*
import com.example.stickynotes.data.local.entity.WidgetEntity

@Dao
interface WidgetDao {
    @Query("SELECT * FROM widgets WHERE id = :id")
    suspend fun getWidgetById(id: Int): WidgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWidget(widget: WidgetEntity)

    @Query("DELETE FROM widgets WHERE id = :id")
    suspend fun deleteWidget(id: Int)

    @Query("SELECT * FROM widgets")
    suspend fun getAllWidgets(): List<WidgetEntity>
}