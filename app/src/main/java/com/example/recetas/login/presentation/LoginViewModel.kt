package com.example.recetas.login.presentation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetas.login.domain.LoginUseCase
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.recetas.core.service.notification.FirebaseHelper
import com.example.recetas.login.data.model.LoginDTO
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

open class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val context: Context
) : ViewModel() {

    private val _username = MutableLiveData<String>("")
    val username: LiveData<String> = _username

    private val _password = MutableLiveData<String>("")
    val password: LiveData<String> = _password

    private val _success = MutableLiveData(false)
    val success: LiveData<Boolean> = _success

    private val _error = MutableLiveData("")
    val error: LiveData<String> = _error

    private val _token = MutableLiveData<String?>()
    val token: LiveData<String?> = _token

    // Flag para indicar si estamos procesando el login
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    var loginSuccess = mutableStateOf<Boolean?>(null)

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)
                _error.postValue("")

                // Manejar posibles excepciones al obtener el token FCM
                val fcmToken = try {
                    FirebaseMessaging.getInstance().token.await()
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "Error al obtener token FCM: ${e.message}")
                    "" // Usar cadena vacía si hay error
                }

                val loginRequest = LoginDTO(email = username, password = password, fcm = fcmToken)

                val result = withContext(Dispatchers.IO) { loginUseCase(loginRequest) }

                result.onSuccess { loginResponse ->
                    Log.d("LoginViewModel", "Login exitoso, Token recibido")

                    // Guardar datos en SharedPreferences
                    saveUserData(loginResponse.data.idUser, loginResponse.data.token)

                    // Actualizar estado de éxito (esto activará la navegación)
                    _success.postValue(true)
                    _error.postValue("")
                    _token.value = loginResponse.data.token

                    //  Guardar token en SharedPreferences
                    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putString("authToken", loginResponse.data.token)
                        apply()
                    }

                    //  Ahora obtenemos el token FCM y lo enviamos al backend
                    sendFcmTokenToBackend()
                }
                    .onFailure { exception ->
                        Log.e("LoginViewModel", "Login fallido: ${exception.message}")
                        _success.postValue(false)
                        _error.postValue(exception.message ?: "Error desconocido")
                    }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Excepción en el login: ${e.message}")
                _success.postValue(false)
                _error.postValue(e.message ?: "Error al intentar realizar la operación")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun saveUserData(userId: Int, authToken: String) {
        val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("userId", userId)
            putString("authToken", authToken)
            apply()
        }
        Log.d("LoginViewModel", "userId y authToken guardados en SharedPreferences")
    }

    // Función para enviar el token FCM al backend (ahora es pública para poder llamarla después)
    fun sendFcmTokenToBackend() {
        viewModelScope.launch {
            try {
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                Log.d("FCM", "Token FCM en login: $fcmToken")

                val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                val authToken = sharedPreferences.getString("authToken", "")

                if (!authToken.isNullOrEmpty()) {
                    // Postpone this to avoid immediate network errors
                    FirebaseHelper.sendTokenToServer(context, fcmToken)
                } else {
                    Log.e("FCM", "No se enviará el token de FCM porque el usuario no ha iniciado sesión.")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error al obtener/enviar token de FCM: ${e.message}")
            }
        }
    }

    fun resetLoginState() {
        loginSuccess.value = null
    }

    fun onChangeUsername(username: String) {
        _username.value = username
    }

    fun onChangePassword(password: String) {
        _password.value = password
    }
}