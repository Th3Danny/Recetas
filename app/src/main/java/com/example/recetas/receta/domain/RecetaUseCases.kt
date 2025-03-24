package com.example.recetas.receta.domain

import com.example.recetas.gustos.data.model.Gusto
import com.example.recetas.receta.data.model.Category
import com.example.recetas.receta.data.model.Ingredient
import com.example.recetas.receta.data.repository.CreateRecetaRepository

class CreateRecetaUseCase(private val repository: CreateRecetaRepository) {
    suspend operator fun invoke(
        title: String,
        description: String,
        instructions: String,
        preparationTime: Int,
        cookingTime: Int,
        servings: Int,
        difficulty: String,
        categoryIds: List<Int>,
        ingredients: List<Ingredient>,
        imagePath: String? = null
    ): Result<Unit> {
        return try {
            repository.createReceta(
                title = title,
                description = description,
                instructions = instructions,
                preparationTime = preparationTime,
                cookingTime = cookingTime,
                servings = servings,
                difficulty = difficulty,
                categoryIds = categoryIds,
                ingredients = ingredients,
                imagePath = imagePath
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetGustosUseCase(private val repository: CreateRecetaRepository) {
    suspend operator fun invoke(): Result<List<Gusto>> {
        return try {
            val gustos = repository.getGustos()
            Result.success(gustos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetIngredientsUseCase(private val repository: CreateRecetaRepository) {
    suspend operator fun invoke(): Result<List<Ingredient>> {
        return try {
            val ingredients = repository.getIngredients()
            Result.success(ingredients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class SincronizarRecetasPendientesUseCase(private val repository: CreateRecetaRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            repository.sincronizarRecetasPendientes()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetPendingOperationsCountUseCase(private val repository: CreateRecetaRepository) {
    suspend operator fun invoke(): Result<Int> {
        return try {
            val count = repository.contarOperacionesPendientes()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetCategoriesUseCase(private val repository: CreateRecetaRepository) {

    suspend operator fun invoke(): Result<List<Category>> {
        return try {
            val categories = repository.getCategories()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetRecetasByUserIdUseCase(private val repository: CreateRecetaRepository) {
    operator fun invoke(userId: Int) = repository.getRecetasByUserId(userId)
}