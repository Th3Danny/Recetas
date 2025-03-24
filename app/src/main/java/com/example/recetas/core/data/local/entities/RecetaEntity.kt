package com.example.recetas.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "recetas")
data class RecetaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
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
    val ingredients: List<IngredientEntity>,
    val isSynced: Boolean = false // Indica si la receta se ha sincronizado con el servidor
)

data class IngredientEntity(
    val ingredientId: Int,
    val quantity: String,
    val unit: String
)

class CategoryIdsConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromCategoryIdsList(value: List<Int>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCategoryIdsList(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType)
    }
}

class IngredientsConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromIngredientsList(value: List<IngredientEntity>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toIngredientsList(value: String): List<IngredientEntity> {
        val listType = object : TypeToken<List<IngredientEntity>>() {}.type
        return gson.fromJson(value, listType)
    }
}