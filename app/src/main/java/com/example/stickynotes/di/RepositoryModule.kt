package com.example.stickynotes.di

import com.example.stickynotes.data.repository.WidgetRepositoryImpl
import com.example.stickynotes.domain.repository.WidgetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWidgetRepository(
        widgetRepositoryImpl: WidgetRepositoryImpl
    ): WidgetRepository
}