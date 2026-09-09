package com.example.dieselcalculateur.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.dieselcalculateur.BuildConfig
import com.example.dieselcalculateur.MainActivity
import com.example.dieselcalculateur.R
import com.example.dieselcalculateur.data.remote.UpdateService

class UpdateCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("UpdateCheckWorker", "Vérification automatique de mise à jour...")
        val updateService = UpdateService()

        return try {
            val latestRelease = updateService.getLatestRelease()
            val latestVersion = latestRelease.tagName
            val currentVersion = BuildConfig.VERSION_NAME

            if (isNewerVersion(currentVersion, latestVersion)) {
                Log.d("UpdateCheckWorker", "Nouvelle version trouvée : $latestVersion")
                showNotification(latestVersion)
            } else {
                Log.d("UpdateCheckWorker", "L'application est à jour.")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("UpdateCheckWorker", "Échec de la vérification : ${e.message}")
            Result.retry()
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

    private fun showNotification(version: String) {
        val channelId = "update_channel"
        val notificationId = 1001

        // Créer le canal pour Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mises à jour"
            val descriptionText = "Notifications de nouvelles versions disponibles"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Intent pour ouvrir l'app sur l'écran réglages (via paramètre ou deep link)
        // Pour l'instant, on ouvre simplement l'app.
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // On peut ajouter un extra pour dire à MainActivity de naviguer vers réglages
            putExtra("navigate_to", "reglages")
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // On utilise l'icône de l'app
            .setContentTitle("Mise à jour disponible")
            .setContentText("La version $version est disponible au téléchargement.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(applicationContext)) {
                notify(notificationId, builder.build())
            }
        } catch (e: SecurityException) {
            Log.e("UpdateCheckWorker", "Permission de notification manquante")
        }
    }
}
