package com.example.recetas.gustos.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.recetas.gustos.domain.FetchGustosUseCase
import com.example.recetas.gustos.domain.FetchGustosUseCase.AddGustoToUserUseCase
import com.example.recetas.gustos.domain.FetchGustosUseCase.GetUserGustosUseCase
import com.example.recetas.gustos.domain.FetchGustosUseCase.RemoveGustoFromUserUseCase



class GustosViewModelFactory(
    private val fetchGustosUseCase: FetchGustosUseCase,
    private val getUserGustosUseCase: GetUserGustosUseCase,
    private val addGustoToUserUseCase: AddGustoToUserUseCase,
    private val removeGustoFromUserUseCase: RemoveGustoFromUserUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GustosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GustosViewModel(
                fetchGustosUseCase,
                getUserGustosUseCase,
                addGustoToUserUseCase,
                removeGustoFromUserUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}