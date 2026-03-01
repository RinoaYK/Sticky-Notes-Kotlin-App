package com.example.stickynotes.ui.widget

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stickynotes.domain.model.WidgetNote
import com.example.stickynotes.domain.repository.WidgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(
    private val repository: WidgetRepository
) : ViewModel() {

    private val _widgetState = MutableLiveData<WidgetNote>()
    val widgetState: LiveData<WidgetNote> = _widgetState

    private val _showLimitToast = MutableLiveData<String?>()
    val showLimitToast: LiveData<String?> = _showLimitToast

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
        layoutSize = "4x1",
        imageAlignment = "RIGHT_CENTER"
    )

    fun updateText(newText: String) {
        val current = _widgetState.value ?: return
        saveAndRefresh(current.copy(text = newText))
    }

    fun updateColors(bgColor: Int? = null, textColor: Int? = null) {
        val current = _widgetState.value ?: return
        saveAndRefresh(current.copy(
            bgColor = bgColor ?: current.bgColor,
            textColor = textColor ?: current.textColor
        ))
    }

    fun updateImage(uri: String) {
        val current = _widgetState.value ?: return
        saveAndRefresh(current.copy(imageUri = uri))
    }

    fun updateFontSize(increment: Boolean) {
        val current = _widgetState.value ?: return
        val minFontSize = 8f
        val maxFontSize = 30f

        if (increment) {
            if (current.fontSize < maxFontSize) {
                saveAndRefresh(current.copy(fontSize = current.fontSize + 1f))
            } else {
                _showLimitToast.value = "Tamanho máximo da fonte atingido"
            }
        } else {
            if (current.fontSize > minFontSize) {
                saveAndRefresh(current.copy(fontSize = current.fontSize - 1f))
            } else {
                _showLimitToast.value = "Tamanho mínimo da fonte atingido"
            }
        }
    }
    fun updateStickerSize(increment: Boolean) {
        val current = _widgetState.value ?: return
        val minStickerSize = 40
        val maxLimit = if (current.layoutSize == "4x1") 80 else 160

        if (increment) {
            if (current.stickerSize < maxLimit) {
                saveAndRefresh(current.copy(stickerSize = current.stickerSize + 10))
            } else {
                _showLimitToast.value = "O Sticker não pode ser maior neste layout"
            }
        } else {
            if (current.stickerSize > minStickerSize) {
                saveAndRefresh(current.copy(stickerSize = current.stickerSize - 10))
            } else {
                _showLimitToast.value = "Tamanho mínimo do sticker atingido"
            }
        }
    }

    fun updateSize(newSize: String) {
        val current = _widgetState.value ?: return
        saveAndRefresh(current.copy(layoutSize = newSize))
    }

    fun toggleAlignments() {
        val current = _widgetState.value ?: return
        val nextImg = if (current.layoutSize == "4x1") {
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

    fun toastShown() { _showLimitToast.value = null }

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