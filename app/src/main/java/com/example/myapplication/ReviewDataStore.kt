package com.example.myapplication

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Una sola instancia de DataStore por archivo: se declara como delegado a nivel de archivo
private val Context.reviewDataStore: DataStore<Preferences> by preferencesDataStore(name = "review_prefs")

class ReviewDataStore(private val context: Context) {

    private val draftKey = stringPreferencesKey("draft_content")

    // Flujo de lectura: emite el borrador guardado (o "" si todavía no hay ninguno)
    val draftContent: Flow<String> = context.reviewDataStore.data.map { prefs ->
        prefs[draftKey] ?: ""
    }

    // Escritura en disco, se llama desde una corrutina
    suspend fun saveDraft(text: String) {
        context.reviewDataStore.edit { prefs ->
            prefs[draftKey] = text
        }
    }
}
