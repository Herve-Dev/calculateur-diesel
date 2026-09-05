package com.example.dieselcalculateur.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculDao {
    @Insert
    suspend fun insert(calcul: CalculEntity)

    @Query("SELECT * FROM calculs ORDER BY date DESC")
    fun getAll(): Flow<List<CalculEntity>>

    @Query("DELETE FROM calculs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calculs")
    suspend fun deleteAll()
}
