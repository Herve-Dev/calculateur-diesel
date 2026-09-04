package com.example.dieselcalculateur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import com.example.dieselcalculateur.ui.calculateur.CalculateurScreen
import com.example.dieselcalculateur.ui.theme.DieselCalculateurTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DieselCalculateurTheme {
                CalculateurScreen()
            }
        }
    }
}
