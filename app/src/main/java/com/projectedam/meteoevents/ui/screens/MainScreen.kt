package com.projectedam.meteoevents.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.projectedam.meteoevents.R
import kotlinx.coroutines.delay

/**
 * Funció composable que representa la pantalla principal de l'aplicació.
 *
 * @param funcionalId Identificador funcional de l'usuari.
 * @param viewModel Model de dades que gestiona l'estat de l'usuari.
 * @param onLogout Funció que s'executa quan l'usuari es desconnecta.
 */
@Composable
fun MainScreen(funcionalId: String, viewModel: UserViewModel, onLogout: () -> Unit, onUserManagement: () -> Unit ) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var isLogoutSuccess by remember { mutableStateOf(false) } // Estat d'èxit o fallida

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                modifier = Modifier.padding(16.dp),
                onClick = {
                    viewModel.logout(
                        onSuccess = {
                            snackbarMessage = "Logout amb èxit"
                            showSnackbar = true
                            isLogoutSuccess = true
                        },
                        onFailure = { errorMessage ->
                            snackbarMessage = errorMessage
                            showSnackbar = true
                            isLogoutSuccess = false
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(Color.Red)
            ) {
                Text("Logout", color = Color.White)
            }
        }

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(240.dp)
        )

        Text("Benvingut")
        Spacer(modifier = Modifier.height(16.dp))

        if (funcionalId == "ADM") {
            Text("Pots realitzar les següents accions:")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Crear esdeveniment */ }) { Text("Crear Esdeveniment") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Modificar esdeveniment */ }) { Text("Modificar Esdeveniment") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Eliminar esdeveniment */ }) { Text("Eliminar Esdeveniment") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Veure esdeveniment */ }) { Text("Veure Esdeveniments") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Gestió d'usuari */ }) { Text("Gestió d'Usuari") }
        } else {
            Text("Pots realitzar les següents accions:")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Veure esdeveniment */ }) { Text("Veure Esdeveniments") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Veure perfil */ }) { Text("Veure Perfil") }
        }
    }

    if (showSnackbar) {
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar(snackbarMessage)
            //Implementat amb IA BlackBox amb prompt: "I need delay a screen of Compose app in android studio"
            if (isLogoutSuccess) {
                delay(10)
                onLogout()
            }
            showSnackbar = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .padding(vertical = 26.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        SnackbarHost(hostState = snackbarHostState)
    }
}
