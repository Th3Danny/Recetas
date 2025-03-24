package com.example.recetas.register.data.model

data class UserRegister(
    val name: String,
    val username: String,
    val email: String,
    val password: String,
    val fcm: String, // El token de Firebase se pasará como parámetro
    val preferred_ingredient_ids: List<Int>
)