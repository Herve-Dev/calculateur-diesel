package com.example.dieselcalculateur.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieselcalculateur.BuildConfig
import com.example.dieselcalculateur.data.remote.UpdateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class UpdateAvailable(val latestVersion: String, val downloadUrl: String) : UpdateState()
    object UpToDate : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class UpdateViewModel : ViewModel() {
    private val updateService = UpdateService()
    
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    fun verifierMiseAJour() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            try {
                val latestRelease = updateService.getLatestRelease()
                val latestVersion = latestRelease.tagName
                val currentVersion = BuildConfig.VERSION_NAME
                
                if (isNewerVersion(currentVersion, latestVersion)) {
                    val apkAsset = latestRelease.assets.find { it.name.endsWith(".apk") }
                    if (apkAsset != null) {
                        _updateState.value = UpdateState.UpdateAvailable(latestVersion, apkAsset.downloadUrl)
                    } else {
                        _updateState.value = UpdateState.UpToDate
                    }
                } else {
                    _updateState.value = UpdateState.UpToDate
                }
            } catch (e: Exception) {
                // Si 404 (pas de release), on considère qu'on est à jour ou qu'il n'y a pas d'update
                if (e.message?.contains("404") == true) {
                    _updateState.value = UpdateState.UpToDate
                } else {
                    _updateState.value = UpdateState.Error("Erreur lors de la vérification : ${e.localizedMessage}")
                }
            }
        }
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        val currentParts = current.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = latest.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        
        for (i in 0 until minOf(currentParts.size, latestParts.size)) {
            if (latestParts[i] > currentParts[i]) return true
            if (latestParts[i] < currentParts[i]) return false
        }
        return latestParts.size > currentParts.size
    }
}
