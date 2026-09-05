package com.example.dieselcalculateur.data.repository

import com.example.dieselcalculateur.data.local.CalculDao
import com.example.dieselcalculateur.data.local.CalculEntity

class CalculRepository(private val dao: CalculDao) {
    suspend fun insererCalcul(calcul: CalculEntity) {
        dao.insert(calcul)
    }
}
