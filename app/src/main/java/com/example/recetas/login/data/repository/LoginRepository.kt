package com.example.recetas.login.data.repository

import com.example.recetas.core.network.RetrofitHelper
import com.example.recetas.login.data.model.LoginDTO
import com.example.recetas.login.data.model.LoginResponse


class LoginRepository {
    private val loginUser = RetrofitHelper.loginService

    suspend fun loginUser(request: LoginDTO): Result<LoginResponse> {

        return try {
            val response = loginUser.loginUser(request)

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}