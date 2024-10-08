package com.projectedam.meteoevents.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projectedam.meteoevents.R

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
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                modifier = Modifier
                    .padding(16.dp),
                onClick = onLogout,
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

        Text("Benvingut, $userType")
        Spacer(modifier = Modifier.height(16.dp))

        if (userType == "Admin") {
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
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MainScreen(userType = "Admin", onLogout = {})
}