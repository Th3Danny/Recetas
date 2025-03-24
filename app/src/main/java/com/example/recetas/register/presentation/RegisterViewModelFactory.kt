package com.example.recetas.register.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.recetas.register.domain.CreateUserUseCase


class RegisterViewModelFactory(
    private val createUserUseCase: CreateUserUseCase,
    private val getIngredientsUseCase: com.example.recetas.register.domain.GetIngredientsUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(createUserUseCase, getIngredientsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}