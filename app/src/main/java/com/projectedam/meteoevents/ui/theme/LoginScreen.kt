package com.projectedam.meteoevents.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        Image(
//            painter = painterResource(id = R.drawable.ic_logo),
//            contentDescription = "Logo",
//            modifier = Modifier.size(120.dp)
//        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Benvingut a MeteoEvents", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nom d'usuari") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contrasenya") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (username.isBlank() || password.isBlank()) {
                errorMessage = "Siusplau, completa tots els camps."
            } else {
                val userType = if (username.equals("Admin", ignoreCase = true)) "Admin" else "User"
                onLoginSuccess(userType)
            }
        }) {
            Text("Login")
        }

        errorMessage?.let { message ->
            LaunchedEffect(snackbarHostState) {
                snackbarHostState.showSnackbar(message)
                errorMessage = null
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
        .padding(vertical = 180.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
        ) { snackbarData ->
            Snackbar(snackbarData)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(onLoginSuccess = {})
}