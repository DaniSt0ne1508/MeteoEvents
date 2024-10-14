package com.projectedam.meteoevents

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.projectedam.meteoevents.ui.screens.LoginScreen
import com.projectedam.meteoevents.ui.screens.MainScreen
import com.projectedam.meteoevents.ui.screens.UserViewModel
import com.projectedam.meteoevents.ui.theme.MeteoEventsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeteoEventsTheme {
                MeteoEventsApp()
            }
        }
    }

    @Composable
    fun MeteoEventsApp() {
        val navController = rememberNavController()

        val viewModel = UserViewModel()

        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                // Pasar el ViewModel a la LoginScreen
                LoginScreen(viewModel = viewModel, onLoginSuccess = { token, funcionalId ->
                    // Aceptando token y funcionalId
                    navController.navigate("main/$token/$funcionalId") { // Navegando con token y funcionalId
                        popUpTo("login") { inclusive = true }
                    }
                })
            }
            composable("main/{token}/{funcionalId}") { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token") ?: ""
                val funcionalId = backStackEntry.arguments?.getString("funcionalId") ?: ""
                MainScreen(token = token, funcionalId = funcionalId, onLogout = {
                    navController.navigate("login") {
                        popUpTo("main/$token/$funcionalId") { inclusive = true }
                    }
                })
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        MeteoEventsApp()
    }
}