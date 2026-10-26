package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReviewViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val store = ReviewDataStore(application)

    // Puntaje: vive en el ViewModel, así que sobrevive a la rotación (no a la muerte del proceso)
    private val _rating = MutableLiveData(0)
    val rating: LiveData<Int> = _rating

    // Comentario: vive en SavedStateHandle, así que sobrevive a la rotación y a la muerte del proceso
    val comment: StateFlow<String> = savedStateHandle.getStateFlow(KEY_COMMENT, "")

    // Indica si el texto actual ya fue guardado como borrador en disco
    private val _draftSaved = MutableStateFlow(false)
    val draftSaved: StateFlow<Boolean> = _draftSaved.asStateFlow()

    init {
        restoreDraft()
    }

    fun updateRating(value: Int) {
        _rating.value = value
    }

    fun updateComment(text: String) {
        savedStateHandle[KEY_COMMENT] = text
        _draftSaved.value = false
    }

    // Guarda el comentario actual en DataStore usando una corrutina
    fun saveDraft() {
        viewModelScope.launch {
            store.saveDraft(comment.value)
            _draftSaved.value = true
        }
    }

    // Al abrir la app desde cero, si no hay comentario en curso se recupera el borrador del disco
    private fun restoreDraft() {
        viewModelScope.launch {
            val draft = store.draftContent.first()
            if (comment.value.isEmpty() && draft.isNotEmpty()) {
                savedStateHandle[KEY_COMMENT] = draft
                _draftSaved.value = true
            }
        }
    }

    private companion object {
        const val KEY_COMMENT = "comment_key"
    }
}
