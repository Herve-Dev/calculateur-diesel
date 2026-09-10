package com.example.dieselcalculateur.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.dieselcalculateur.data.model.Carburant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val RAYON_RECHERCHE = floatPreferencesKey("rayon_recherche")
        val CARBURANT_ID = intPreferencesKey("carburant_id")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data
        .map { preferences ->
            val rayon = preferences[PreferencesKeys.RAYON_RECHERCHE] ?: 10f
            val fuelId = preferences[PreferencesKeys.CARBURANT_ID] ?: Carburant.GAZOLE.id
            val carburant = Carburant.entries.find { it.id == fuelId } ?: Carburant.GAZOLE
            UserSettings(rayon, carburant)
        }

    suspend fun saveRayon(rayon: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RAYON_RECHERCHE] = rayon
        }
    }

    suspend fun saveCarburant(carburant: Carburant) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CARBURANT_ID] = carburant.id
        }
    }
}

data class UserSettings(
    val rayon: Float,
    val carburant: Carburant
)
