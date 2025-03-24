package com.example.recetas.register.domain

import com.example.recetas.register.data.model.Ingredient
import com.example.recetas.register.data.repository.RegisterRepository
import com.example.recetas.register.data.model.UserRegister

import javax.inject.Inject

class CreateUserUseCase @Inject constructor(
    private val repository: RegisterRepository // Inyecta el repositorio
) {
    suspend operator fun invoke(userRegister: UserRegister): Boolean {
        return repository.registerUser(userRegister) // Llama al repositorio para registrar al usuario
    }
}

class GetIngredientsUseCase(private val repository: RegisterRepository) {
    suspend operator fun invoke(): Result<List<Ingredient>> {
        return try {
            val ingredients = repository.getIngredients()
            Result.success(ingredients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}