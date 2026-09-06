package com.example.dieselcalculateur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dieselcalculateur.ui.calculateur.CalculateurScreen
import com.example.dieselcalculateur.ui.calculateur.CalculateurViewModel
import com.example.dieselcalculateur.ui.reglages.ReglagesScreen
import com.example.dieselcalculateur.ui.theme.DieselCalculateurTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DieselCalculateurTheme {
                val navController = rememberNavController()
                val viewModel: CalculateurViewModel = viewModel()
                
                NavHost(navController = navController, startDestination = "calculateur") {
                    composable("calculateur") {
                        CalculateurScreen(
                            viewModel = viewModel,
                            onNavigateToReglages = { navController.navigate("reglages") }
                        )
                    }
                    composable("reglages") {
                        ReglagesScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
