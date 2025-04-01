package com.example.recetas.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "pending_receta_operations")
data class PendingRecetaOperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operationType: String,
    val userId: Int,
    val title: String,
    val description: String,
    val instructions: String,
    val preparationTime: Int,
    val cookingTime: Int,
    val servings: Int,
    val difficulty: String,
    val image: String? = null,
    @TypeConverters(CategoryIdsConverter::class)
    val categoryIds: List<Int>,
    @TypeConverters(IngredientsConverter::class)
    val ingredients: String, // JSON string de los ingredientes
    val attempts: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING" // "PENDING", "IN_PROGRESS", "SYNCED", "ERROR"
)