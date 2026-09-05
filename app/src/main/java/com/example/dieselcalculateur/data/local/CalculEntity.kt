package com.example.dieselcalculateur.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculs")
data class CalculEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val montantSouhaite: Double,
    val prixPlafonne: Double,
    val prixAffiche: Double,
    val litres: Double,
    val montantAAnnoncer: Double,
    val economie: Double
)
