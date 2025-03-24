package com.example.recetas.receta.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.recetas.receta.data.repository.CreateRecetaRepository
import com.example.recetas.receta.domain.*

class CreateRecetaViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateRecetaViewModel::class.java)) {
            val repository = CreateRecetaRepository(context)
            val createRecetaUseCase = CreateRecetaUseCase(repository)
            val getGustosUseCase = GetGustosUseCase(repository)
            val getIngredientsUseCase = GetIngredientsUseCase(repository)
            val getCategoriesUseCase = GetCategoriesUseCase(repository)
            val sincronizarRecetasPendientesUseCase = SincronizarRecetasPendientesUseCase(repository)
            val getPendingOperationsCountUseCase = GetPendingOperationsCountUseCase(repository)

            @Suppress("UNCHECKED_CAST")
            return CreateRecetaViewModel(
                createRecetaUseCase,
                getGustosUseCase,
                getIngredientsUseCase,
                getCategoriesUseCase,
                sincronizarRecetasPendientesUseCase,
                getPendingOperationsCountUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}