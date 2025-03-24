package com.example.recetas.register.domain

import com.example.recetas.register.data.model.UserRegister
import com.example.recetas.register.data.repository.RegisterRepository
import javax.inject.Inject

class CreateUserUseCase @Inject constructor(
    private val repository: RegisterRepository // Inyecta el repositorio
) {
    suspend operator fun invoke(userRegister: UserRegister): Boolean {
        return repository.registerUser(userRegister) // Llama al repositorio para registrar al usuario
    }
}