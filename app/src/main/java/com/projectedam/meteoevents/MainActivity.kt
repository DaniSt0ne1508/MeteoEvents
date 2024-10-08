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
import com.projectedam.meteoevents.ui.theme.LoginScreen
import com.projectedam.meteoevents.ui.theme.MainScreen
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
    }

@Composable
fun MeteoEventsApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLoginSuccess = { userType ->
                navController.navigate("main/$userType") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("main/{userType}") { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: "User"
            MainScreen(userType = userType, onLogout = {
                navController.navigate("login") {
                    popUpTo("main/$userType") { inclusive = true }
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