package com.projectedam.meteoevents.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectedam.meteoevents.network.MeteoDetails

@Composable
fun SeguretatScreen(
    userViewModel: UserViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var eventId by remember { mutableStateOf("") }
    var isButtonEnabled by remember { mutableStateOf(false) }
    var usuariosList by remember { mutableStateOf<List<String>>(emptyList()) }
    var meteoData by remember { mutableStateOf<MeteoDetails?>(null) }  // Cambiado a un solo MeteoDetails
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(eventId) {
        isButtonEnabled = eventId.isNotEmpty()
    }

    fun fetchMeteoData() {
        if (eventId.isNotEmpty()) {
            isLoading = true
            errorMessage = null

            userViewModel.getMeteo(eventId.toInt(), { users, meteo ->
                usuariosList = users
                meteoData = meteo
                isLoading = false
            }, { error ->
                errorMessage = error
                isLoading = false
            })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pantalla de Seguretat", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Introduïu la ID de l'esdeveniment:")

        TextField(
            value = eventId,
            onValueChange = { eventId = it },
            label = { Text("ID de l'Esdeveniment") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            keyboardActions = KeyboardActions.Default
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { fetchMeteoData() },
            enabled = isButtonEnabled,
            colors = ButtonDefaults.buttonColors(Color(0xFF81C784))
        ) {
            Text("Confirmar ID", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNavigateBack) {
            Text("Tornar")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        // Mostrar mensaje de error
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Error: $errorMessage", color = Color.Red)
        }

        if (usuariosList.isNotEmpty() || meteoData != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Text("Usuaris participants:")
            usuariosList.forEach { usuario ->
                Text(usuario)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (meteoData != null) {
                Text("Dades meteorològiques:")

                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Velocitat Mitja Vent: ${meteoData?.velocitatMitjaVent} km/h")
                    Text("Alerta Vent Mitja: ${meteoData?.alertaVentMitja}")
                    Text("Ratxa Màxima Vent: ${meteoData?.ratxaMaximaVent} km/h")
                    Text("Alerta Ratxa Màxima: ${meteoData?.alertaRatxaMaxima}")
                    Text("Temperatura: ${meteoData?.temperatura} °C")
                    Text("Humitat Relativa: ${meteoData?.humitatRelativa}%")
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
