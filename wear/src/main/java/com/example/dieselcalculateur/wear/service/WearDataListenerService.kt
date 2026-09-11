package com.example.dieselcalculateur.wear.service

import android.util.Log
import com.example.dieselcalculateur.data.local.SyncedCalculResult
import com.example.dieselcalculateur.data.local.WearDataStore
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WearDataListenerService : WearableListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("WearDataListenerService", "Événement DataLayer reçu sur la montre: ${dataEvents.count} événement(s)")

        val wearDataStore = WearDataStore(applicationContext)

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/latest_calcul") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                val timestamp = dataMap.getLong("timestamp", System.currentTimeMillis())
                val montantAAnnoncer = dataMap.getDouble("montantAAnnoncer", 0.0)
                val litres = dataMap.getDouble("litres", 0.0)
                val economie = dataMap.getDouble("economie", 0.0)
                val montantSouhaite = dataMap.getDouble("montantSouhaite", 0.0)
                val enseigne = dataMap.getString("enseigne", "")

                Log.d("WearDataListenerService", "Calcul synchronisé reçu: $montantAAnnoncer € ($litres L)")

                val syncedResult = SyncedCalculResult(
                    timestamp = timestamp,
                    montantAAnnoncer = montantAAnnoncer,
                    litres = litres,
                    economie = economie,
                    montantSouhaite = montantSouhaite,
                    enseigne = enseigne
                )

                serviceScope.launch {
                    wearDataStore.saveLatestResult(syncedResult)
                }
            }
        }
    }
}
