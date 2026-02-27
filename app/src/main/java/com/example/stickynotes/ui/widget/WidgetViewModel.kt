package com.example.stickynotes.ui.widget

import android.app.Application
import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.stickynotes.R

class WidgetViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)

    private val _widgetState = MutableLiveData<WidgetData>()
    val widgetState: LiveData<WidgetData> = _widgetState

    private val _showLimitToast = MutableLiveData<String?>()
    val showLimitToast: LiveData<String?> = _showLimitToast

    data class WidgetData(
        val text: String,
        val bgColor: Int,
        val textColor: Int,
        val fontSize: Float,
        val stickerSize: Int,
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
        var stickerSize = prefs.getInt("widget_sticker_size", 110)
        val defaultTextColor = ContextCompat.getColor(getApplication(), R.color.sticky_text)
        val fontSize = prefs.getFloat("widget_font_size", 13f)
        if (size == "4x1" && stickerSize > 80) {
            stickerSize = 80
        }

        try {
            _widgetState.value = WidgetData(
                text = prefs.getString("user_text", "Seu texto") ?: "",
                bgColor = prefs.getInt("widget_bg_color", Color.WHITE),
                textColor = prefs.getInt("widget_text_color", defaultTextColor),
                fontSize = fontSize,
                stickerSize = stickerSize,
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
                textColor = prefs.getInt("widget_text_color", defaultTextColor),
                fontSize = fontSize,
                stickerSize = stickerSize,
                bgColor = prefs.getInt("widget_bg_color", Color.WHITE),
                imageUri = prefs.getString("widget_image_uri", null),
                size = size,
                textAlignment = WidgetAlignment.LEFT_CENTER,
                imageAlignment = WidgetAlignment.RIGHT_CENTER
            )
        }
    }

    fun updateFontSize(increment: Boolean) {
        val current = _widgetState.value?.fontSize ?: 13f
        val next = if (increment) current + 1f else current - 1f
        if (next in 8f..30f) {
            prefs.edit { putFloat("widget_font_size", next) }
            loadData()
        }
    }

    fun toastShown() {
        _showLimitToast.value = null
    }

    fun updateStickerSize(increment: Boolean) {
        val currentState = _widgetState.value ?: return
        val currentSize = currentState.stickerSize
        val widgetLayout = currentState.size

        val minLimit = 40
        val maxLimit = if (widgetLayout == "4x1") 80 else 160

        if (increment) {
            val nextSize = currentSize + 10
            if (nextSize <= maxLimit) {
                prefs.edit { putInt("widget_sticker_size", nextSize) }
                loadData()
            } else {
                _showLimitToast.value = "Limite máximo atingido para o layout $widgetLayout"
            }
        } else {
            val nextSize = currentSize - 10
            if (nextSize >= minLimit) {
                prefs.edit { putInt("widget_sticker_size", nextSize) }
                loadData()
            } else {
                _showLimitToast.value = "Tamanho mínimo atingido"
            }
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