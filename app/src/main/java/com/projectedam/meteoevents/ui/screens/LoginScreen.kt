package com.projectedam.meteoevents.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.projectedam.meteoevents.R

@Composable
fun LoginScreen(viewModel: UserViewModel, onLoginSuccess: (String, String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(300.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Benvingut a MeteoEvents",
            style = MaterialTheme.typography.headlineMedium
        )
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
            label = { Text("Contrasenya") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    errorMessage = "Siusplau, completa tots els camps."
                } else {
                    isLoading = true
                    viewModel.login(
                        username,
                        password,
                        onSuccess = { token, funcionalId ->
                            isLoading = false
                            onLoginSuccess(token, funcionalId)
                        },
                        onFailure = { error ->
                            isLoading = false
                            errorMessage = error
                        }
                    )
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Loading..." else "Login")
        }

        errorMessage?.let { message ->
            LaunchedEffect(snackbarHostState) {
                snackbarHostState.showSnackbar(message)
                errorMessage = null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 56.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
        ) { snackbarData ->
            Snackbar(snackbarData)
        }
    }
}