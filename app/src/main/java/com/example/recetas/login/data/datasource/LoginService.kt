package com.example.recetas.login.data.datasource

import com.example.recetas.login.data.model.LoginDTO
import com.example.recetas.login.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("auth/authenticate")
    suspend fun loginUser(@Body request: LoginDTO): Response<LoginResponse>
}