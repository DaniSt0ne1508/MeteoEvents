package com.projectedam.meteoevents.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectedam.meteoevents.network.MeteoDetails

@Composable
fun SeguretatScreen(
    userViewModel: UserViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var eventId by remember { mutableStateOf("") }
    var isButtonEnabled by remember { mutableStateOf(false) }
    var usuarisList by remember { mutableStateOf<List<String>>(emptyList()) }
    var meteoData by remember { mutableStateOf<MeteoDetails?>(null) }
    var accionsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var alertaMessage by remember { mutableStateOf<String?>(null) }
    var alertaColor by remember { mutableStateOf(Color.Black) }

    /**
     * Verifica els nivells d'alerta dels dades meteorològics.
     *
     * @param meteo Les dades meteorològiques per verificar els nivells d'alerta.
     */
    fun checkAlertLevels(meteo: MeteoDetails) {
        val alertas = listOf(
            meteo.alertaVentMitja,
            meteo.alertaRatxaMaxima,
            meteo.alertaPluja,
            meteo.alertaNeu,
            meteo.alertaAltaTemperatura,
            meteo.alertaBaixaTemperatura
        )

        val maxAlerta = alertas.filterNotNull().maxOrNull() ?: 0

        alertaMessage = when (maxAlerta) {
            1 -> "Segur"
            2 -> "Precaució"
            3 -> "Vigilant"
            4 -> "Alerta"
            5 -> "Cancel·lat"
            else -> "Sense alerta"
        }

        alertaColor = when (maxAlerta) {
            1 -> Color.Green
            2 -> Color.Yellow
            3 -> Color.Blue
            4 -> Color.Red
            5 -> Color(0xFF9C27B0)
            else -> Color.Black
        }
    }

    LaunchedEffect(eventId) {
        isButtonEnabled = eventId.isNotEmpty()
    }

    /**
     * Obté les dades meteorològiques segons la ID de l'esdeveniment.
     *
     * @param eventId La ID de l'esdeveniment per obtenir les dades meteorològiques.
     */
    fun fetchMeteoData() {
        if (eventId.isNotEmpty()) {
            isLoading = true
            errorMessage = null

            userViewModel.getMeteo(eventId.toInt(), { users, meteo, actions ->
                usuarisList = users
                meteoData = meteo
                accionsList = extractActionsFromSubmap(actions)
                isLoading = false
                checkAlertLevels(meteo)
            }, { error ->
                errorMessage = error
                isLoading = false
            })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pantalla de Seguretat", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Introduïu la ID de l'esdeveniment:", fontWeight = FontWeight.Bold)
        TextField(
            value = eventId,
            onValueChange = { eventId = it },
            label = { Text("ID de l'Esdeveniment") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            keyboardActions = KeyboardActions.Default
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { fetchMeteoData() },
                enabled = isButtonEnabled,
                colors = ButtonDefaults.buttonColors(Color(0xFF81C784))
            ) {
                Text("Confirmar ID", color = Color.White)
            }

            Button(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.Top)
            ) {
                Text("Tornar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (alertaMessage != null) {
            AlertMessage(alertaMessage!!, alertaColor)
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Error: $errorMessage", color = Color.Red)
        }

        if (usuarisList.isNotEmpty() || meteoData != null) {
            Spacer(modifier = Modifier.height(16.dp))

            UsuarisList(usuarisList)
            Spacer(modifier = Modifier.height(16.dp))

            if (meteoData != null) {
                MeteoDetailsSection(meteoData!!)

                if (accionsList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Accions de seguretat:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    AccionsList(accionsList)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AlertMessage(message: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "Estat esdeveniment: ",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )
        Text(
            message,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = color,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.6f),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            )
        )
    }
}

@Composable
fun UsuarisList(usuaris: List<String>) {
    Text("Usuaris participants:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.small)
            .padding(8.dp)
    ) {
        usuaris.forEach { usuario ->
            Text(usuario)
        }
    }
}

@Composable
fun MeteoDetailsSection(meteo: MeteoDetails) {
    Text("Dades meteorològiques:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Column {
        Text("Velocitat Mitja Vent: ${meteo.velocitatMitjaVent} km/h")
        Text("Alerta Vent Mitja: ${meteo.alertaVentMitja}", fontWeight = FontWeight.Bold)
        Text("Ratxa Màxima Vent: ${meteo.ratxaMaximaVent} km/h")
        Text("Alerta Ratxa Maxima: ${meteo.alertaRatxaMaxima}", fontWeight = FontWeight.Bold)
        Text("Probabilitat Pluja: ${meteo.probabilitatPluja}%")
        Text("Precipitació: ${meteo.precipitacio} mm")
        Text("Alerta Pluja: ${meteo.alertaPluja}", fontWeight = FontWeight.Bold)
        Text("Probabilitat Tempesta: ${meteo.probabilitatTempesta}%")
        Text("Neu: ${meteo.neu} cm")
        Text("Alerta Neu: ${meteo.alertaNeu}", fontWeight = FontWeight.Bold)
        Text("Probabilitat Nevada: ${meteo.probabilitatNevada}%")
        Text("Temperatura: ${meteo.temperatura} °C")
        Text("Alerta Alta Temperatura: ${meteo.alertaAltaTemperatura}", fontWeight = FontWeight.Bold)
        Text("Alerta Baixa Temperatura: ${meteo.alertaBaixaTemperatura}", fontWeight = FontWeight.Bold)
        Text("Humitat Relativa: ${meteo.humitatRelativa}%")
    }
}

@Composable
fun AccionsList(accions: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.small)
            .padding(8.dp)
    ) {
        accions.forEach { accio ->
            Text("- $accio")
        }
    }
}

/**
 * Extreu les accions de la llista d'strings netejant i eliminant duplicats.
 *
 * @param actions La llista d'accions a netejar.
 * @return Una llista d'accions única formatejada.
 */
fun extractActionsFromSubmap(actions: List<String>): List<String> {
    return actions.map { action ->
        val cleanedAction = action.trim('{', '}')
        val actionValue = cleanedAction.substringAfter("=")
        actionValue
    }.distinct()
}
