package com.example.recetas.core.sesion

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

interface SessionManager {
    fun getUserId(): Int?
    fun getAuthToken(): String?
    fun isLoggedIn(): Boolean
}

class SessionManagerImpl(private val context: Context) : SessionManager {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val TAG = "SessionManagerImpl"
        private const val KEY_USER_ID = "userId"
        private const val KEY_AUTH_TOKEN = "authToken"
    }

    override fun getUserId(): Int? {
        val userId = sharedPreferences.getInt(KEY_USER_ID, -1)
        return if (userId != -1) {
            Log.d(TAG, "getUserId: Retornando userId: $userId")
            userId
        } else {
            Log.e(TAG, "getUserId: No se encontró el userId en SharedPreferences")
            null
        }
    }

    override fun getAuthToken(): String? {
        val token = sharedPreferences.getString(KEY_AUTH_TOKEN, null)
        if (token == null) {
            Log.e(TAG, "getAuthToken: No se encontró el token en SharedPreferences")
        }
        return token
    }

    override fun isLoggedIn(): Boolean {
        return getUserId() != null && getAuthToken() != null
    }
}