package com.example.dieselcalculateur.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.wearDataStore: DataStore<Preferences> by preferencesDataStore(name = "wear_synced_data")

data class SyncedCalculResult(
    val timestamp: Long = 0L,
    val montantAAnnoncer: Double = 0.0,
    val litres: Double = 0.0,
    val economie: Double = 0.0,
    val montantSouhaite: Double = 0.0,
    val enseigne: String = ""
)

class WearDataStore(private val context: Context) {

    private object PreferencesKeys {
        val TIMESTAMP = longPreferencesKey("timestamp")
        val MONTANT_A_ANNONCER = doublePreferencesKey("montant_a_annoncer")
        val LITRES = doublePreferencesKey("litres")
        val ECONOMIE = doublePreferencesKey("economie")
        val MONTANT_SOUHAITE = doublePreferencesKey("montant_souhaite")
        val ENSEIGNE = stringPreferencesKey("enseigne")
    }

    val latestResultFlow: Flow<SyncedCalculResult?> = context.wearDataStore.data
        .map { preferences ->
            val timestamp = preferences[PreferencesKeys.TIMESTAMP] ?: 0L
            if (timestamp == 0L) {
                null
            } else {
                SyncedCalculResult(
                    timestamp = timestamp,
                    montantAAnnoncer = preferences[PreferencesKeys.MONTANT_A_ANNONCER] ?: 0.0,
                    litres = preferences[PreferencesKeys.LITRES] ?: 0.0,
                    economie = preferences[PreferencesKeys.ECONOMIE] ?: 0.0,
                    montantSouhaite = preferences[PreferencesKeys.MONTANT_SOUHAITE] ?: 0.0,
                    enseigne = preferences[PreferencesKeys.ENSEIGNE] ?: ""
                )
            }
        }

    suspend fun saveLatestResult(result: SyncedCalculResult) {
        context.wearDataStore.edit { preferences ->
            preferences[PreferencesKeys.TIMESTAMP] = result.timestamp
            preferences[PreferencesKeys.MONTANT_A_ANNONCER] = result.montantAAnnoncer
            preferences[PreferencesKeys.LITRES] = result.litres
            preferences[PreferencesKeys.ECONOMIE] = result.economie
            preferences[PreferencesKeys.MONTANT_SOUHAITE] = result.montantSouhaite
            preferences[PreferencesKeys.ENSEIGNE] = result.enseigne
        }
    }
}
