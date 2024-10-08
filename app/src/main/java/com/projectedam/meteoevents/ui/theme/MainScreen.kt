package com.projectedam.meteoevents.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(userType: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(Color.Red)
            ) {
                Text("Logout", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Bienvenido, $userType")
        Spacer(modifier = Modifier.height(16.dp))

        if (userType == "Admin") {
            Text("Puedes realizar las siguientes acciones:")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Crear evento */ }) { Text("Crear Evento") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Modificar evento */ }) { Text("Modificar Evento") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Eliminar evento */ }) { Text("Eliminar Evento") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Ver eventos */ }) { Text("Ver Eventos") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Gestionar usuarios */ }) { Text("Gestión de Usuarios") }
        } else {
            Text("Puedes realizar las siguientes acciones:")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Ver eventos */ }) { Text("Ver Eventos") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Ver perfil */ }) { Text("Ver Perfil") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MainScreen(userType = "Admin", onLogout = {})
}