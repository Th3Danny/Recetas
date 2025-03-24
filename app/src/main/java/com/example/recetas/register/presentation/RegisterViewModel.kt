package com.example.recetas.register.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetas.gustos.data.model.Gusto
import com.example.recetas.register.data.model.Ingredient
import com.example.recetas.register.domain.GetIngredientsUseCase
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
    private val getIngredientsUseCase: GetIngredientsUseCase
) : ViewModel() {
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _ingredients = MutableLiveData<List<Ingredient>>(emptyList())
    val ingredients: LiveData<List<Ingredient>> = _ingredients

    private val _error = MutableLiveData("")
    val error: LiveData<String> = _error

    private val _isSuccess = MutableLiveData(false)
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _gustos = MutableStateFlow<List<Gusto>>(emptyList())
    val gustos: StateFlow<List<Gusto>> = _gustos

    private val _registrationState = MutableStateFlow(RegistrationState.IDLE)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    fun loadIngredients() {
        viewModelScope.launch {
            try {
                _registrationState.value = RegistrationState.LOADING

                val result = getIngredientsUseCase()

                result.onSuccess { ingredients ->
                    // Convertir Ingredient a Gusto
                    val gustos = ingredients.map { ingredient ->
                        Gusto(
                            id = ingredient.id,
                            nombre = ingredient.name
                        )
                    }

                    // Actualizar los gustos
                    _gustos.value = gustos

                    // Cambiar el estado
                    _registrationState.value = RegistrationState.IDLE
                }.onFailure { exception ->
                    Log.e("RegisterViewModel", "Error al cargar ingredientes: ${exception.message}")
                    _registrationState.value = RegistrationState.ERROR
                }
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Excepción al cargar ingredientes: ${e.message}")
                _registrationState.value = RegistrationState.ERROR
            }
        }
    }

    fun registerUser(
        name: String,
        username: String,
        email: String,
        password: String,
        fcmToken: String,
        selectedIngredientIds: List<Int>
    ) {
        viewModelScope.launch {
            try {
                _registrationState.value = RegistrationState.LOADING

                val userRegister = UserRegister(
                    name = name,
                    username = username,
                    email = email,
                    password = password,
                    fcm = fcmToken, // Usar el token FCM pasado
                    preferred_ingredient_ids = selectedIngredientIds
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