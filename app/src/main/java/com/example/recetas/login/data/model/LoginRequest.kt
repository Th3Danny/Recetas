package com.example.recetas.login.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("data") val data: LoginData
)

data class LoginData(
    @SerializedName("id_user") val idUser: Int,
    @SerializedName("access_token") val token: String,
    @SerializedName("fcm") val fcmToken: String
)