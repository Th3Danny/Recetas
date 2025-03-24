package com.example.recetas.register.data.datasource

import com.example.recetas.gustos.data.model.GustoResponse
import com.example.recetas.register.data.model.UserDTO
import com.example.recetas.register.data.model.UserRegister
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RegisterService {
    @POST("users")
    suspend fun registerUser(@Body userRegister: UserRegister): Response<UserDTO>

    @GET("gustos")
    suspend fun ingredients(): Response<List<GustoResponse>>
}