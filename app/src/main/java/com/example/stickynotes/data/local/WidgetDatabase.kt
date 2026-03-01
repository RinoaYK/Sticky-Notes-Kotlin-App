package com.example.stickynotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stickynotes.data.local.dao.WidgetDao
import com.example.stickynotes.data.local.entity.WidgetEntity

@Database(entities = [WidgetEntity::class], version = 1, exportSchema = false)
abstract class WidgetDatabase : RoomDatabase() {
    abstract fun widgetDao(): WidgetDao
}