package com.example.recetas.home.model

import com.example.recetas.home.data.repository.RecetaRepository
import com.example.recetas.home.data.model.Receta

class GetRecetasUseCase(private val recetaRepository: RecetaRepository) {

    suspend operator fun invoke(): Result<List<Receta>> {
        return try {
            val recetas = recetaRepository.getRecetas()
            Result.success(recetas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}