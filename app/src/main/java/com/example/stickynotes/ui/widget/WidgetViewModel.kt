package com.example.stickynotes.ui.widget

import android.app.Application
import android.content.Context
import android.graphics.Color
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class WidgetViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)

    private val _widgetState = MutableLiveData<WidgetData>()
    val widgetState: LiveData<WidgetData> = _widgetState

    data class WidgetData(
        val text: String,
        val bgColor: Int,
        val imageUri: String?,
        val size: String,
        val textAlignment: WidgetAlignment,
        val imageAlignment: WidgetAlignment
    )

    init {
        loadData()
    }

    fun loadData() {
        val size = prefs.getString("widget_size", "4x1") ?: "4x1"

        try {
            _widgetState.value = WidgetData(
                text = prefs.getString("user_text", "Seu texto") ?: "",
                bgColor = prefs.getInt("widget_bg_color", Color.WHITE),
                imageUri = prefs.getString("widget_image_uri", null),
                size = size,
                textAlignment = WidgetAlignment.valueOf(
                    prefs.getString("txt_align_$size", "LEFT_CENTER")!!
                ),
                imageAlignment = WidgetAlignment.valueOf(
                    prefs.getString("img_align_$size", "RIGHT_CENTER")!!
                )
            )
        } catch (e: Exception) {
            _widgetState.value = WidgetData(
                text = prefs.getString("user_text", "Seu texto") ?: "",
                bgColor = prefs.getInt("widget_bg_color", Color.WHITE),
                imageUri = prefs.getString("widget_image_uri", null),
                size = size,
                textAlignment = WidgetAlignment.LEFT_CENTER,
                imageAlignment = WidgetAlignment.RIGHT_CENTER
            )
        }
    }

    fun updateSize(newSize: String) {
        prefs.edit { putString("widget_size", newSize) }
        loadData()
    }

    fun toggleAlignments() {
        val currentState = _widgetState.value ?: return
        val size = currentState.size

        val (nextImg, nextTxt) = if (size == "4x1") {
            if (currentState.imageAlignment == WidgetAlignment.RIGHT_CENTER) {
                Pair(WidgetAlignment.LEFT_CENTER, WidgetAlignment.RIGHT_CENTER)
            } else {
                Pair(WidgetAlignment.RIGHT_CENTER, WidgetAlignment.LEFT_CENTER)
            }
        } else {
            when (currentState.imageAlignment) {
                WidgetAlignment.RIGHT_CENTER -> Pair(
                    WidgetAlignment.LEFT_CENTER,
                    WidgetAlignment.RIGHT_CENTER
                )

                WidgetAlignment.LEFT_CENTER -> Pair(
                    WidgetAlignment.TOP_CENTER,
                    WidgetAlignment.BOTTOM_CENTER
                )

                else -> Pair(WidgetAlignment.RIGHT_CENTER, WidgetAlignment.LEFT_CENTER)
            }
        }

        prefs.edit {
            putString("img_align_$size", nextImg.name)
            putString("txt_align_$size", nextTxt.name)
        }
        loadData()
    }
}