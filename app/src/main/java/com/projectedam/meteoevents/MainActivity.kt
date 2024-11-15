package com.projectedam.meteoevents

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.projectedam.meteoevents.network.ApiClient.apiService
import com.projectedam.meteoevents.ui.screens.EsdevenimentsManagementScreen
import com.projectedam.meteoevents.ui.screens.LoginScreen
import com.projectedam.meteoevents.ui.screens.MainScreen
import com.projectedam.meteoevents.ui.screens.UserManagementScreen
import com.projectedam.meteoevents.ui.screens.UserViewModel
import com.projectedam.meteoevents.ui.theme.MeteoEventsTheme

/**
 * Activitat principal de l'aplicació que gestiona la interfície d'usuari.
 *
 * Aquesta classe s'encarrega d'inicialitzar l'aplicació i configurar el sistema de navegació.
 */
class MainActivity : ComponentActivity() {
    /**
     * Mètode que s'executa quan l'activitat és creada.
     *
     * Aquí s'actualitza la configuració de la pantalla i es defineix el contingut de l'aplicació.
     *
     * @param savedInstanceState Estat anterior de l'activitat (si n'hi ha).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeteoEventsTheme {
                MeteoEventsApp()
            }
        }
    }

    /**
     * Funció composable que configura l'aplicació Meteor Events.
     *
     * Aquesta funció gestiona la navegació entre pantalles.
     */
    @Composable
    fun MeteoEventsApp() {
        val navController = rememberNavController()
        val viewModel = UserViewModel(apiService)

        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(viewModel = viewModel, onLoginSuccess = { token, funcionalId ->
                    navController.navigate("main/$token/$funcionalId") {
                        popUpTo("login") { inclusive = true }
                    }
                })
            }
            composable("main/{token}/{funcionalId}") { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token") ?: ""
                val funcionalId = backStackEntry.arguments?.getString("funcionalId") ?: ""

                MainScreen(
                    funcionalId = funcionalId,
                    viewModel = viewModel,
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo("main/$token/$funcionalId") {
                                inclusive = true
                            }
                        }
                    },
                    onUserManagement = {
                        navController.navigate("user_management")
                    },
                    onEventManagement = {
                        navController.navigate("event_management")
                    }
                )
            }
            composable("user_management") {
                UserManagementScreen(
                    userViewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("event_management") {
                EsdevenimentsManagementScreen(
                    userViewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}