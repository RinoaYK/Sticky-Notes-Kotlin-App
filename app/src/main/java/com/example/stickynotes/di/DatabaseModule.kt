package com.example.stickynotes.di

import android.content.Context
import androidx.room.Room
import com.example.stickynotes.data.local.WidgetDatabase
import com.example.stickynotes.data.local.dao.WidgetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WidgetDatabase {
        return Room.databaseBuilder(
            context,
            WidgetDatabase::class.java,
            "sticky_notes.db"
        ).build()
    }

    @Provides
    fun provideWidgetDao(db: WidgetDatabase): WidgetDao = db.widgetDao()
}