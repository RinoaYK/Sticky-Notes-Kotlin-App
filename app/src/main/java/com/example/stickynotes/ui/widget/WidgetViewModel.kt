package com.example.stickynotes.ui.widget

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stickynotes.R
import com.example.stickynotes.domain.model.WidgetNote
import com.example.stickynotes.domain.repository.WidgetRepository
import com.example.stickynotes.util.WidgetConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(
    private val repository: WidgetRepository
) : ViewModel() {

    private val _widgetState = MutableLiveData<WidgetNote>()
    val widgetState: LiveData<WidgetNote> = _widgetState

    private val _showLimitToast = MutableLiveData<Int?>()
    val showLimitToast: LiveData<Int?> = _showLimitToast

    private val _allWidgets = MutableLiveData<List<WidgetNote>>()
    val allWidgets: LiveData<List<WidgetNote>> = _allWidgets

    private var activeId: Int = -1

    fun loadData(widgetId: Int) {
        activeId = widgetId
        viewModelScope.launch {
            val note = repository.getWidgetById(widgetId)
            if (note != null) {
                _widgetState.value = note
            } else {
                val defaultNote = createDefaultNote(widgetId)
                repository.saveWidget(defaultNote)
                _widgetState.value = defaultNote
            }
        }
    }

    private fun createDefaultNote(id: Int) = WidgetNote(
        id = id,
        text = "Sua nota...",
        bgColor = -1,
        textColor = -16777216,
        fontSize = 13f,
        stickerSize = 80,
        imageUri = null,
        layoutSize = WidgetConstants.SIZE_4X1,
        imageAlignment = "RIGHT_CENTER"
    )

    fun updateText(newText: String) {
        val current = _widgetState.value ?: return
        saveAndRefresh(current.copy(text = newText))
    }

    fun updateColors(bgColor: Int? = null, textColor: Int? = null) {
        val current = _widgetState.value ?: return
        saveAndRefresh(
            current.copy(
                bgColor = bgColor ?: current.bgColor,
                textColor = textColor ?: current.textColor
            )
        )
    }

    fun updateImage(uri: String) {
        val current = _widgetState.value ?: return
        saveAndRefresh(current.copy(imageUri = uri))
    }

    fun updateFontSize(increment: Boolean) {
        val current = _widgetState.value ?: return
        val minFontSize = WidgetConstants.MIN_FONT_SIZE
        val maxFontSize = WidgetConstants.MAX_FONT_SIZE

        if (increment) {
            if (current.fontSize < maxFontSize) {
                saveAndRefresh(current.copy(fontSize = current.fontSize + 1f))
            } else {
                _showLimitToast.value = R.string.toast_font_max
            }
        } else {
            if (current.fontSize > minFontSize) {
                saveAndRefresh(current.copy(fontSize = current.fontSize - 1f))
            } else {
                _showLimitToast.value = R.string.toast_font_min
            }
        }
    }

    fun updateStickerSize(increment: Boolean) {
        val current = _widgetState.value ?: return
        val minStickerSize = WidgetConstants.MIN_STICKER_SIZE
        val maxLimit =
            if (current.layoutSize == WidgetConstants.SIZE_4X1) WidgetConstants.MAX_STICKER_SIZE_4X1 else WidgetConstants.MAX_STICKER_SIZE_5X2

        if (increment) {
            if (current.stickerSize < maxLimit) {
                saveAndRefresh(current.copy(stickerSize = current.stickerSize + WidgetConstants.STICKER_RESIZE_STEP))
            } else {
                _showLimitToast.value = R.string.toast_sticker_max
            }
        } else {
            if (current.stickerSize > minStickerSize) {
                saveAndRefresh(current.copy(stickerSize = current.stickerSize - WidgetConstants.STICKER_RESIZE_STEP))
            } else {
                _showLimitToast.value = R.string.toast_sticker_min
            }
        }
    }

    fun updateSize(newSize: String) {
        val current = _widgetState.value ?: return
        saveAndRefresh(current.copy(layoutSize = newSize))
    }

    fun toggleAlignments() {
        val current = _widgetState.value ?: return
        val nextImg = if (current.layoutSize == WidgetConstants.SIZE_4X1) {
            if (current.imageAlignment == "RIGHT_CENTER") "LEFT_CENTER" else "RIGHT_CENTER"
        } else {
            if (current.imageAlignment == "RIGHT_CENTER") "LEFT_CENTER" else "RIGHT_CENTER"
        }
        saveAndRefresh(current.copy(imageAlignment = nextImg))
    }

    private fun saveAndRefresh(updatedNote: WidgetNote) {
        viewModelScope.launch {
            repository.saveWidget(updatedNote)
            _widgetState.value = updatedNote
        }
    }

    fun toastShown() {
        _showLimitToast.value = null
    }

    fun loadAllWidgets() {
        viewModelScope.launch {
            _allWidgets.value = repository.getAllWidgets()
        }
    }

    fun deleteWidget(id: Int) {
        viewModelScope.launch {
            repository.deleteWidget(id)
            loadAllWidgets()
        }
    }
}