package com.example.dieselcalculateur.util

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

object WearDataSyncHelper {

    private const val PATH_LATEST_CALCUL = "/latest_calcul"

    fun syncLatestCalcul(
        context: Context,
        montantAAnnoncer: Double,
        litres: Double,
        economie: Double,
        montantSouhaite: Double,
        enseigne: String = ""
    ) {
        try {
            val putDataMapRequest = PutDataMapRequest.create(PATH_LATEST_CALCUL).apply {
                dataMap.putLong("timestamp", System.currentTimeMillis())
                dataMap.putDouble("montantAAnnoncer", montantAAnnoncer)
                dataMap.putDouble("litres", litres)
                dataMap.putDouble("economie", economie)
                dataMap.putDouble("montantSouhaite", montantSouhaite)
                dataMap.putString("enseigne", enseigne)
            }

            val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()

            Wearable.getDataClient(context.applicationContext)
                .putDataItem(putDataRequest)
                .addOnSuccessListener {
                    Log.d("WearDataSyncHelper", "Résultat de calcul envoyé avec succès vers la montre.")
                }
                .addOnFailureListener { e ->
                    Log.w("WearDataSyncHelper", "Échec silencieux de l'envoi Wear: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w("WearDataSyncHelper", "Exception lors de l'envoi Wear DataLayer: ${e.message}")
        }
    }
}
