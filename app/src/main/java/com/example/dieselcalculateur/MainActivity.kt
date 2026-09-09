package com.example.dieselcalculateur

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.dieselcalculateur.ui.calculateur.CalculateurScreen
import com.example.dieselcalculateur.ui.calculateur.CalculateurViewModel
import com.example.dieselcalculateur.ui.components.ComponentPreviewScreen
import com.example.dieselcalculateur.ui.components.PillSelectableButton
import com.example.dieselcalculateur.ui.reglages.ReglagesScreen
import com.example.dieselcalculateur.ui.theme.DieselCalculateurTheme
import com.example.dieselcalculateur.ui.update.UpdateViewModel
import com.example.dieselcalculateur.worker.UpdateCheckWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission de notification accordée ou refusée
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Demande de permission pour les notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Planification de la vérification des mises à jour en arrière-plan
        scheduleUpdateCheck()

        // Détermination de l'écran de démarrage selon l'Intent
        val startDest = when (intent?.getStringExtra("navigate_to")) {
            "reglages" -> "reglages"
            "preview" -> "component_preview"
            else -> "calculateur"
        }

        setContent {
            DieselCalculateurTheme {
                val navController = rememberNavController()
                val viewModel: CalculateurViewModel = viewModel()
                val updateViewModel: UpdateViewModel = viewModel()
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry.value?.destination?.route

                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(navController = navController, startDestination = startDest) {
                        composable("calculateur") {
                            CalculateurScreen(
                                viewModel = viewModel,
                                onNavigateToReglages = { navController.navigate("reglages") }
                            )
                        }
                        composable("reglages") {
                            ReglagesScreen(
                                viewModel = viewModel,
                                updateViewModel = updateViewModel,
                                onBack = { 
                                    if (!navController.popBackStack()) {
                                        navController.navigate("calculateur") {
                                            popUpTo("reglages") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        composable("component_preview") {
                            ComponentPreviewScreen(
                                onBack = {
                                    if (!navController.popBackStack()) {
                                        navController.navigate("calculateur") {
                                            popUpTo("component_preview") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Bouton de dev flottant pour accéder facilement à la prévisualisation des composants
                    if (currentRoute != "component_preview") {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(end = 12.dp, top = 8.dp)
                        ) {
                            PillSelectableButton(
                                text = "🎨 Preview",
                                selected = false,
                                onClick = { navController.navigate("component_preview") }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun scheduleUpdateCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val updateWorkRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "UpdateCheckWork",
            ExistingPeriodicWorkPolicy.KEEP, // Garder le cycle existant s'il existe déjà
            updateWorkRequest
        )
    }
}
