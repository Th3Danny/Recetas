package com.example.recetas.core.data.local.dao

import androidx.room.*
import com.example.recetas.core.data.local.entities.RecetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecetaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceta(receta: RecetaEntity): Long

    @Update
    suspend fun updateReceta(receta: RecetaEntity)

    @Delete
    suspend fun deleteReceta(receta: RecetaEntity)

    @Query("SELECT * FROM recetas WHERE id = :id")
    suspend fun getRecetaById(id: Int): RecetaEntity?

    @Query("SELECT * FROM recetas ORDER BY id DESC")
    fun getAllRecetas(): Flow<List<RecetaEntity>>

    @Query("SELECT * FROM recetas WHERE isSynced = 0")
    suspend fun getUnsyncedRecetas(): List<RecetaEntity>

    @Query("UPDATE recetas SET isSynced = 1 WHERE id = :id")
    suspend fun markRecetaAsSynced(id: Int)

    @Query("SELECT * FROM recetas WHERE userId = :userId ORDER BY id DESC")
    fun getRecetasByUserId(userId: Int): Flow<List<RecetaEntity>>
}