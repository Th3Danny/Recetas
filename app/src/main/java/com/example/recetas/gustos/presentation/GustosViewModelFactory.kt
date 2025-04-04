package com.example.recetas.gustos.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.recetas.gustos.domain.AddGustoToUserUseCase
import com.example.recetas.gustos.domain.GetIngredientsUseCase
import com.example.recetas.gustos.domain.GetUserGustosUseCase
import com.example.recetas.gustos.domain.PostIngredientUseCase


class GustosViewModelFactory(
    private val addGustoToUserUseCase: AddGustoToUserUseCase,
    private val fetchGustosUseCase: GetIngredientsUseCase,
    private val getUserGustosUseCase: GetUserGustosUseCase,
    private val postIngredientUseCase: PostIngredientUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GustosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GustosViewModel(
                addGustoToUserUseCase,
                getUserGustosUseCase,
                fetchGustosUseCase,
                postIngredientUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}