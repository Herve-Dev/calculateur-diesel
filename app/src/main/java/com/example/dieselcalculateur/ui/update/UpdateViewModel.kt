package com.example.dieselcalculateur.ui.update

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieselcalculateur.BuildConfig
import com.example.dieselcalculateur.data.remote.UpdateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class UpdateAvailable(val latestVersion: String, val downloadUrl: String) : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    object ReadyToInstall : UpdateState()
    object UpToDate : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class UpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val updateService = UpdateService()
    
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private var downloadedApk: File? = null

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
                if (e.message?.contains("404") == true) {
                    _updateState.value = UpdateState.UpToDate
                } else {
                    _updateState.value = UpdateState.Error("Erreur de vérification : ${e.localizedMessage}")
                }
            }
        }
    }

    fun telechargerMiseAJour(url: String) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Downloading(0)
            try {
                val file = withContext(Dispatchers.IO) {
                    val client = OkHttpClient()
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    
                    if (!response.isSuccessful) throw Exception("Échec du téléchargement")
                    
                    val body = response.body ?: throw Exception("Corps de réponse vide")
                    val totalSize = body.contentLength()
                    
                    val destinationFolder = File(getApplication<Application>().cacheDir, "updates")
                    if (!destinationFolder.exists()) destinationFolder.mkdirs()
                    val apkFile = File(destinationFolder, "update.apk")
                    
                    body.byteStream().use { input ->
                        FileOutputStream(apkFile).use { output ->
                            val buffer = ByteArray(8192)
                            var read: Int
                            var totalRead = 0L
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                totalRead += read
                                if (totalSize > 0) {
                                    val progress = ((totalRead * 100) / totalSize).toInt()
                                    if (progress % 5 == 0) { // On évite de spammer trop le StateFlow
                                        _updateState.value = UpdateState.Downloading(progress)
                                    }
                                } else {
                                    // Taille inconnue, on affiche une progression indéterminée via un flag spécial ou 0
                                    _updateState.value = UpdateState.Downloading(-1)
                                }
                            }
                        }
                    }
                    apkFile
                }
                downloadedApk = file
                _updateState.value = UpdateState.ReadyToInstall
                // Petit délai pour laisser l'UI passer en ReadyToInstall avant de lancer l'Intent
                kotlinx.coroutines.delay(500)
                installerApk()
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("Erreur de téléchargement : ${e.localizedMessage}")
            }
        }
    }

    fun installerApk() {
        val apkFile = downloadedApk ?: return
        try {
            val context = getApplication<Application>()
            val uri = FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                apkFile
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("Impossible de lancer l'installation : ${e.localizedMessage}")
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
