package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

private const val TAG = "LIFECYCLE_DEBUG"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d(TAG, "onCreate: La Activity se está creando")
        setContent {
            MyApplicationTheme {
                ReviewScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: La Activity es visible")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: La Activity tiene el foco (interactuable)")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: La Activity pierde el foco")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: La Activity ya no es visible")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: La Activity va a ser destruida")
    }
}

@Composable
fun ReviewScreen(viewModel: ReviewViewModel = viewModel()) {
    val rating by viewModel.rating.observeAsState(0)
    val comment by viewModel.comment.collectAsState()
    val draftSaved by viewModel.draftSaved.collectAsState()
    var backBlocked by remember { mutableStateOf(false) }

    // El aviso desaparece en cuanto el usuario vuelve a modificar el comentario
    LaunchedEffect(comment) { backBlocked = false }

    // Solo intercepta el gesto Atrás cuando hay texto escrito
    BackHandler(enabled = comment.isNotEmpty()) {
        Log.d(TAG, "BackHandler: gesto Atrás bloqueado, hay un comentario en curso")
        backBlocked = true
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text("Reseña del producto", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            Text(
                text = if (rating == 0) "Puntaje: sin calificar" else "Puntaje: $rating de 5",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (value in 1..5) {
                    val modifier = Modifier.weight(1f)
                    if (value == rating) {
                        Button(
                            onClick = { viewModel.updateRating(value) },
                            modifier = modifier,
                            contentPadding = PaddingValues(0.dp)
                        ) { Text("$value") }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.updateRating(value) },
                            modifier = modifier,
                            contentPadding = PaddingValues(0.dp)
                        ) { Text("$value") }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { viewModel.updateComment(it) },
                label = { Text("Comentario") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Button(onClick = { viewModel.saveDraft() }) { Text("Guardar Borrador") }
            if (draftSaved) {
                Spacer(Modifier.height(8.dp))
                Text("Borrador guardado en el dispositivo", color = MaterialTheme.colorScheme.primary)
            }
            if (backBlocked) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tienes un comentario en curso. Guárdalo como borrador o bórralo para poder salir.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
