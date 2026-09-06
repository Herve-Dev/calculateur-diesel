package com.example.dieselcalculateur.data.repository

import com.example.dieselcalculateur.data.local.CalculDao
import com.example.dieselcalculateur.data.local.CalculEntity
import kotlinx.coroutines.flow.Flow

class CalculRepository(private val dao: CalculDao) {
    suspend fun insererCalcul(calcul: CalculEntity) {
        dao.insert(calcul)
    }

    fun getHistorique(): Flow<List<CalculEntity>> {
        return dao.getAll()
    }

    suspend fun supprimerTout() {
        dao.deleteAll()
    }
}
