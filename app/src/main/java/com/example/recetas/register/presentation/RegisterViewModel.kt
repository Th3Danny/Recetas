package com.example.recetas.register.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetas.gustos.data.model.Gusto
import com.example.recetas.gustos.domain.FetchGustosUseCase
import com.example.recetas.register.data.model.UserRegister
import com.example.recetas.register.domain.CreateUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class RegistrationState {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

class RegisterViewModel(
    private val createUserUseCase: CreateUserUseCase,
    private val fetchGustosUseCase: FetchGustosUseCase
) : ViewModel() {

    private val _gustos = MutableStateFlow<List<Gusto>>(emptyList())
    val gustos: StateFlow<List<Gusto>> = _gustos

    private val _registrationState = MutableStateFlow(RegistrationState.IDLE)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    fun fetchGustos() {
        viewModelScope.launch {
            try {
                val result = fetchGustosUseCase()
                _gustos.value = result
            } catch (e: Exception) {
                // Manejar errores
                _gustos.value = emptyList()
            }
        }
    }

    fun registerUser(name: String, email: String, password: String, gustoId: Int) {
        viewModelScope.launch {
            try {
                _registrationState.value = RegistrationState.LOADING

                val userRegister = UserRegister(
                    name = name,
                    email = email,
                    password = password,
                    gustoId = gustoId
                )

                val success = createUserUseCase(userRegister)

                _registrationState.value = if (success) {
                    RegistrationState.SUCCESS
                } else {
                    RegistrationState.ERROR
                }

            } catch (e: Exception) {
                _registrationState.value = RegistrationState.ERROR
            }
        }
    }
}