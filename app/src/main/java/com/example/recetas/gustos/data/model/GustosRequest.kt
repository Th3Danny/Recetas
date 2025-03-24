package com.example.recetas.gustos.data.model

data class AddGustoRequest(
    val userId: Int,
    val gustoId: Int
)

data class RemoveGustoRequest(
    val userId: Int,
    val gustoId: Int
)

data class GetUserGustosRequest(
    val userId: Int
)