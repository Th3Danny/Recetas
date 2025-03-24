package com.example.recetas.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.recetas.home.model.GetRecetasUseCase

class HomeViewModelFactory(
    private val getRecetasUseCase: GetRecetasUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(getRecetasUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}