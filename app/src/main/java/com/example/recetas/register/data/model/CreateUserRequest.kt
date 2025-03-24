package com.example.recetas.register.data.model

data class UserRegister(
    val name: String,
    val email: String,
    val password: String,
    val gustoId: Int
)