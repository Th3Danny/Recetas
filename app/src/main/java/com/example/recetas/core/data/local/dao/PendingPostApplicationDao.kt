package com.example.recetas.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.recetas.core.data.local.entities.PendingRecetaOperationEntity

@Dao
interface PendingRecetaOperationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingOperation(operation: PendingRecetaOperationEntity): Long

    @Query("SELECT * FROM pending_receta_operations WHERE syncStatus = 'PENDING' ORDER BY timestamp ASC")
    suspend fun getAllPendingOperations(): List<PendingRecetaOperationEntity>

    @Query("UPDATE pending_receta_operations SET syncStatus = :status, attempts = attempts + 1 WHERE id = :id")
    suspend fun updateOperationStatus(id: Long, status: String)

    @Query("DELETE FROM pending_receta_operations WHERE id = :id")
    suspend fun deletePendingOperation(id: Long)

    @Query("SELECT * FROM pending_receta_operations WHERE id = :id")
    suspend fun getPendingOperationById(id: Long): PendingRecetaOperationEntity?

    @Query("SELECT COUNT(*) FROM pending_receta_operations WHERE syncStatus = 'PENDING'")
    suspend fun getPendingOperationsCount(): Int
}