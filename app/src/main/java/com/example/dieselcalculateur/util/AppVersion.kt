package com.example.dieselcalculateur.util

import com.example.dieselcalculateur.BuildConfig

object AppVersion {
    /**
     * Retourne le nom de la version actuelle de l'application (ex: "1.0.0").
     */
    fun getVersionName(): String = BuildConfig.VERSION_NAME

    /**
     * Retourne le code de version actuel de l'application (ex: 1).
     */
    fun getVersionCode(): Int = BuildConfig.VERSION_CODE
}
