package com.example.recetas.core.network


import com.example.recetas.gustos.data.datasource.GustosService
import com.example.recetas.login.data.datasource.LoginService
import com.example.recetas.home.data.datasource.RecetaService
import com.example.recetas.receta.data.datasource.CreateRecetaService
import com.example.recetas.register.data.datasource.RegisterService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    private const val BASE_URL = "http://4.tcp.ngrok.io:15583/api/"

    internal val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val loginService: LoginService = retrofit.create(LoginService::class.java)
    val registerService: RegisterService = retrofit.create(RegisterService::class.java)
    val recetaService : RecetaService = retrofit.create(RecetaService::class.java)
    val createRecetaService: CreateRecetaService = retrofit.create(CreateRecetaService::class.java)
    val gustosService : GustosService = retrofit.create(GustosService::class.java)

}