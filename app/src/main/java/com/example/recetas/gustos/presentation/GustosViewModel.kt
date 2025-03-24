package com.example.recetas.gustos.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetas.gustos.data.model.Gusto
import com.example.recetas.gustos.domain.FetchGustosUseCase
import com.example.recetas.gustos.domain.FetchGustosUseCase.AddGustoToUserUseCase
import com.example.recetas.gustos.domain.FetchGustosUseCase.GetUserGustosUseCase
import com.example.recetas.gustos.domain.FetchGustosUseCase.RemoveGustoFromUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class GustosState {
    object Loading : GustosState()
    data class Success(val gustos: List<GustoWithSelection>) : GustosState()
    data class Error(val message: String) : GustosState()
}

enum class SaveState {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

data class GustoWithSelection(
    val gusto: Gusto,
    val isSelected: Boolean
)

class GustosViewModel(
    private val fetchGustosUseCase: FetchGustosUseCase,
    private val getUserGustosUseCase: GetUserGustosUseCase,
    private val addGustoToUserUseCase: AddGustoToUserUseCase,
    private val removeGustoFromUserUseCase: RemoveGustoFromUserUseCase
) : ViewModel() {

    // Estado de la UI
    private val _gustosState = MutableStateFlow<GustosState>(GustosState.Loading)
    val gustosState: StateFlow<GustosState> = _gustosState

    // Estado de guardado
    private val _saveState = MutableStateFlow(SaveState.IDLE)
    val saveState: StateFlow<SaveState> = _saveState

    // Listas para gestionar selecciones
    private val userGustos = mutableSetOf<Int>() // Gustos actuales del usuario
    private val pendingAdditions = mutableSetOf<Int>() // Gustos pendientes de añadir
    private val pendingRemovals = mutableSetOf<Int>() // Gustos pendientes de eliminar

    // Cargar gustos disponibles y gustos del usuario
    fun loadGustos() {
        viewModelScope.launch {
            _gustosState.value = GustosState.Loading

            try {
                // Obtener todos los gustos disponibles
                val allGustos = fetchGustosUseCase()

                // Obtener gustos del usuario
                val userGustosList = getUserGustosUseCase()
                userGustos.clear()
                userGustos.addAll(userGustosList.map { it.id })

                // Actualizar la UI con los gustos y sus selecciones
                updateGustosState(allGustos)

            } catch (e: Exception) {
                _gustosState.value = GustosState.Error("Error al cargar los gustos: ${e.message}")
            }
        }
    }

    // Alternar la selección de un gusto
    fun toggleGustoSelection(gusto: Gusto) {
        val gustoId = gusto.id

        if (isGustoSelected(gustoId)) {
            // Si estaba seleccionado, lo eliminamos
            if (userGustos.contains(gustoId)) {
                // Estaba en la BD, lo marcamos para eliminación
                pendingRemovals.add(gustoId)
                pendingAdditions.remove(gustoId)
            } else {
                // Solo estaba marcado para añadirse, lo quitamos
                pendingAdditions.remove(gustoId)
            }
        } else {
            // Si no estaba seleccionado, lo añadimos
            if (userGustos.contains(gustoId)) {
                // Ya estaba en la BD pero marcado para eliminarse, cancelamos eso
                pendingRemovals.remove(gustoId)
            } else {
                // No estaba ni en la BD ni pendiente, lo marcamos para añadir
                pendingAdditions.add(gustoId)
            }
        }

        // Actualizar la UI
        updateGustosState()
    }

    // Guardar los cambios en los gustos del usuario
    fun saveUserGustos() {
        viewModelScope.launch {
            _saveState.value = SaveState.LOADING

            try {
                var success = true

                // Procesar adiciones
                for (gustoId in pendingAdditions) {
                    val result = addGustoToUserUseCase(gustoId)
                    if (!result) success = false
                }

                // Procesar eliminaciones
                for (gustoId in pendingRemovals) {
                    val result = removeGustoFromUserUseCase(gustoId)
                    if (!result) success = false
                }

                if (success) {
                    // Actualizar el estado interno
                    userGustos.removeAll(pendingRemovals)
                    userGustos.addAll(pendingAdditions)
                    pendingAdditions.clear()
                    pendingRemovals.clear()

                    _saveState.value = SaveState.SUCCESS
                } else {
                    _saveState.value = SaveState.ERROR
                }

            } catch (e: Exception) {
                _saveState.value = SaveState.ERROR
            }
        }
    }

    // Verificar si un gusto está seleccionado
    private fun isGustoSelected(gustoId: Int): Boolean {
        val inUserGustos = userGustos.contains(gustoId)
        val inPendingAdditions = pendingAdditions.contains(gustoId)
        val inPendingRemovals = pendingRemovals.contains(gustoId)

        return (inUserGustos && !inPendingRemovals) || (!inUserGustos && inPendingAdditions)
    }

    // Actualizar el estado de la UI con los gustos y sus selecciones
    private fun updateGustosState(allGustos: List<Gusto>? = null) {
        // Obtener la lista de gustos disponibles
        val gustosDisponibles = allGustos
            ?: (_gustosState.value as? GustosState.Success)?.gustos?.map { it.gusto }

        // Mapear los gustos disponibles a GustoWithSelection
        val gustosWithSelection = gustosDisponibles
            ?.map { gusto ->
                GustoWithSelection(
                    gusto = gusto,
                    isSelected = isGustoSelected(gusto.id)
                )
            } ?: emptyList()

        // Actualizar el estado de la UI
        _gustosState.value = GustosState.Success(gustosWithSelection)
    }
}