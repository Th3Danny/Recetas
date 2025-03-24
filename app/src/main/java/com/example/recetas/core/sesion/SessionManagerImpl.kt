package com.example.recetas.core.sesion

import android.content.Context

class SessionManagerImpl(context: Context) : SessionManager {
    private val sharedPreferences = context.getSharedPreferences("RecetasApp", Context.MODE_PRIVATE)

    override fun getUserId(): Int? {
        return sharedPreferences.getInt("user_id", -1).takeIf { it != -1 }
    }

    fun saveUserId(userId: Int) {
        sharedPreferences.edit().putInt("user_id", userId).apply()
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}