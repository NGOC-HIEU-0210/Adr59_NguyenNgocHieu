package com.hieu.app.ui

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.app.data.repository.FormulaRecognitionRepository
import com.hieu.app.ui.i18n.AppLanguage
import com.hieu.app.util.renderStrokesToBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class InputMode { DRAW, UPLOAD }


sealed interface RecognitionError {
    data object NoFormula : RecognitionError
    data class Server(val detail: String?) : RecognitionError
}


data class FormulaUiState(
    val language: AppLanguage = AppLanguage.VI,

    // Đầu vào
    val mode: InputMode = InputMode.DRAW,
    val strokes: List<List<Offset>> = emptyList(),
    val currentStroke: List<Offset> = emptyList(),
    val imageBitmap: Bitmap? = null,
    val imageName: String? = null,

    // Kết quả
    val formulas: List<String> = emptyList(),
    val isRecognizing: Boolean = false,
    val error: RecognitionError? = null
) {
    val canRecognize: Boolean
        get() = !isRecognizing &&
            (if (mode == InputMode.DRAW) strokes.isNotEmpty() else imageBitmap != null)
}

class FormulaViewModel : ViewModel() {

    private val repository = FormulaRecognitionRepository()


    private val _uiState = MutableStateFlow(FormulaUiState())
    val uiState: StateFlow<FormulaUiState> = _uiState.asStateFlow()

    // Cài đặt
    fun setLanguage(language: AppLanguage) {
        _uiState.update { it.copy(language = language) }
    }

    // Đầu vào: chế độ
    fun setMode(mode: InputMode) {
        _uiState.update { it.copy(mode = mode) }
        resetResults()
    }

    // Đầu vào: vẽ tay
    fun startStroke(point: Offset) {
        _uiState.update { it.copy(currentStroke = listOf(point)) }
        resetResults()
    }

    fun addPoint(point: Offset) {
        _uiState.update { it.copy(currentStroke = it.currentStroke + point) }
    }

    fun endStroke() {
        _uiState.update {
            val finished = it.currentStroke
            it.copy(
                strokes = if (finished.size > 1) it.strokes + listOf(finished) else it.strokes,
                currentStroke = emptyList()
            )
        }
    }

    fun undoStroke() {
        _uiState.update { it.copy(strokes = it.strokes.dropLast(1)) }
        resetResults()
    }

    fun clearStrokes() {
        _uiState.update { it.copy(strokes = emptyList(), currentStroke = emptyList()) }
        resetResults()
    }

    //  Đầu vào: upload ảnh
    fun setImage(bitmap: Bitmap?, name: String?) {
        resetResults()
        if (bitmap != null || name != null) {
            _uiState.update { it.copy(imageBitmap = bitmap, imageName = name) }
        }
    }

    // Kết quả

    fun updateFormulas(formulas: List<String>) {
        _uiState.update { it.copy(formulas = formulas) }
    }


    private fun resetResults() {
        _uiState.update { it.copy(formulas = emptyList(), error = null) }
    }

    // Gọi server
    fun recognize(padWidth: Int, padHeight: Int) {
        val state = _uiState.value
        if (state.isRecognizing) return

        val bitmap = if (state.mode == InputMode.DRAW) {
            renderStrokesToBitmap(state.strokes, padWidth, padHeight)
        } else {
            state.imageBitmap
        } ?: return

        _uiState.update { it.copy(isRecognizing = true, error = null) }


        viewModelScope.launch {
            repository.recognize(bitmap).fold(
                onSuccess = { response ->
                    val recognized = response.equations.orEmpty().filter { it.isNotBlank() }
                    _uiState.update {
                        if (recognized.isEmpty()) {
                            it.copy(isRecognizing = false, formulas = emptyList(), error = RecognitionError.NoFormula)
                        } else {
                            it.copy(isRecognizing = false, formulas = recognized)
                        }
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isRecognizing = false, error = RecognitionError.Server(e.message))
                    }
                }
            )
        }
    }
}
