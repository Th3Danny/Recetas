package com.example.recetas.home.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetas.home.model.GetRecetasUseCase
import com.example.recetas.home.data.model.Receta
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getRecetasUseCase: GetRecetasUseCase
) : ViewModel() {

    private val _recetas = MutableLiveData<List<Receta>>(emptyList())
    val recetas: LiveData<List<Receta>> = _recetas

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData("")
    val error: LiveData<String> = _error

    fun loadRecetas() {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)
                _error.postValue("")

                val result = getRecetasUseCase()

                result.onSuccess { listaRecetas ->
                    _recetas.postValue(listaRecetas)
                }.onFailure { exception ->
                    Log.e("HomeViewModel", "Error al cargar recetas: ${exception.message}")
                    _error.postValue("Error al cargar las recetas: ${exception.message}")
                }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Excepción al cargar recetas: ${e.message}")
                _error.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun refreshRecetas() {
        loadRecetas()
    }
}