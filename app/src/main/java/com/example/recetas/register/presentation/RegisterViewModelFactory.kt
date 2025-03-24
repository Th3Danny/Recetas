package com.example.recetas.register.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.recetas.gustos.domain.FetchGustosUseCase
import com.example.recetas.register.domain.CreateUserUseCase


class RegisterViewModelFactory(
    private val createUserUseCase: CreateUserUseCase,
    private val fetchGustosUseCase: FetchGustosUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(createUserUseCase, fetchGustosUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}