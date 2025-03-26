package com.example.recetas.gustos.presentation

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetas.gustos.data.model.Gusto
import com.example.recetas.gustos.domain.AddGustoToUserUseCase
import com.example.recetas.gustos.domain.GetUserGustosUseCase
import com.example.recetas.gustos.domain.PostIngredientUseCase
import com.example.recetas.receta.utils.getRealPathFromUri
import com.example.recetas.register.data.model.Ingredient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.cancellation.CancellationException

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
    private val addGustoToUserUseCase: AddGustoToUserUseCase,
    private val getUserGustosUseCase: GetUserGustosUseCase,
    private val getIngredientsUseCase: com.example.recetas.gustos.domain.GetIngredientsUseCase,
    private val postIngredientUseCase: PostIngredientUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "GustosViewModel"
    }

    // Estado de la UI para gustos
    private val _gustosState = MutableStateFlow<GustosState>(GustosState.Loading)
    val gustosState: StateFlow<GustosState> = _gustosState

    // Estado de guardado
    private val _saveState = MutableStateFlow(SaveState.IDLE)
    val saveState: StateFlow<SaveState> = _saveState

    // Lista de ingredientes
    private val _ingredients = MutableLiveData<List<Ingredient>>(emptyList())
    val ingredients: LiveData<List<Ingredient>> = _ingredients

    // Estado de carga
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Mensajes de error
    private val _error = MutableLiveData("")
    val error: LiveData<String> = _error

    // Listas para gestionar selecciones
    private val userGustos = mutableSetOf<Int>() // Gustos actuales del usuario
    private val pendingAdditions = mutableSetOf<Int>() // Gustos pendientes de añadir
    private val pendingRemovals = mutableSetOf<Int>() // Gustos pendientes de eliminar

    // Cargar ingredientes
    // Cargar ingredientes con manejo mejorado de errores
    fun loadIngredients() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = ""

                supervisorScope {
                    try {
                        // Usar supervisorScope para que un fallo no cancele otras corrutinas
                        val result = getIngredientsUseCase()
                        result.onSuccess { ingredientsList ->
                            _ingredients.value = ingredientsList
                            Log.d(TAG, "Ingredientes cargados: ${ingredientsList.size}")

                            // Inicializar lista de gustos vacía si no hay datos
                            if (_gustosState.value is GustosState.Loading) {
                                _gustosState.value = GustosState.Success(emptyList())
                            }

                        }.onFailure { exception ->
                            if (exception is CancellationException) {
                                // Manejar específicamente la cancelación
                                Log.w(TAG, "La carga de ingredientes fue cancelada")
                            } else {
                                Log.e(TAG, "Error al cargar ingredientes: ${exception.message}")
                                _error.value = "Error al cargar ingredientes: ${exception.message}"
                            }
                        }
                    } catch (e: CancellationException) {
                        // Capturar explícitamente CancellationException
                        Log.w(TAG, "La carga de ingredientes fue cancelada", e)
                        // No establecer error para cancelaciones, ya que son esperadas
                    } catch (e: Exception) {
                        Log.e(TAG, "Excepción al cargar ingredientes", e)
                        _error.value = "Error: ${e.message}"
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createNewIngredient(context: Context, name: String, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val imagePath = imageUri?.let { getRealPathFromUri(context, it) }

                val result = postIngredientUseCase(name, imagePath)

                result.onSuccess {
                    loadIngredients()
                }.onFailure {
                    _error.value = "Error al crear ingrediente: ${it.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }




    // Cargar gustos del usuario
    fun loadUserGustos() {
        viewModelScope.launch {
            _gustosState.value = GustosState.Loading

            try {
                val result = getUserGustosUseCase()

                result.onSuccess { gustosList ->
                    Log.d(TAG, "Gustos del usuario obtenidos: ${gustosList.size}")

                    // Guardar los IDs de los gustos actuales del usuario
                    userGustos.clear()
                    userGustos.addAll(gustosList.map { it.id })

                    // Crear la lista de GustoWithSelection
                    val gustosWithSelection = gustosList.map { gusto ->
                        GustoWithSelection(
                            gusto = gusto,
                            isSelected = true // Todos están seleccionados inicialmente
                        )
                    }

                    // Actualizar el estado
                    _gustosState.value = GustosState.Success(gustosWithSelection)
                }.onFailure { exception ->
                    if (exception is CancellationException) {
                        Log.w(TAG, "La carga de gustos del usuario fue cancelada")
                        // Inicializar con lista vacía para no bloquear la UI
                        _gustosState.value = GustosState.Success(emptyList())
                    } else {
                        Log.e(TAG, "Error al obtener gustos del usuario: ${exception.message}")
                        _gustosState.value = GustosState.Error("Error al cargar tus preferencias")
                    }
                }
            } catch (e: CancellationException) {
                // No propagar la excepción, solo registrarla
                Log.w(TAG, "La carga de gustos del usuario fue cancelada", e)
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al cargar gustos del usuario", e)
                _gustosState.value = GustosState.Error("Error: ${e.message}")
            }
        }
    }

    // Alternar la selección de un gusto
    fun toggleGustoSelection(gusto: Gusto) {
        val gustoId = gusto.id
        Log.d(TAG, "Alternando selección del gusto: ${gusto.nombre} (ID: $gustoId)")

        if (isGustoSelected(gustoId)) {
            // Si estaba seleccionado, lo eliminamos
            if (userGustos.contains(gustoId)) {
                // Estaba en la BD, lo marcamos para eliminación
                pendingRemovals.add(gustoId)
                pendingAdditions.remove(gustoId)
                Log.d(TAG, "Gusto marcado para eliminación: ${gusto.nombre}")
            } else {
                // Solo estaba marcado para añadirse, lo quitamos
                pendingAdditions.remove(gustoId)
                Log.d(TAG, "Cancelada adición del gusto: ${gusto.nombre}")
            }
        } else {
            // Si no estaba seleccionado, lo añadimos
            if (userGustos.contains(gustoId)) {
                // Ya estaba en la BD pero marcado para eliminarse, cancelamos eso
                pendingRemovals.remove(gustoId)
                Log.d(TAG, "Cancelada eliminación del gusto: ${gusto.nombre}")
            } else {
                // No estaba ni en la BD ni pendiente, lo marcamos para añadir
                pendingAdditions.add(gustoId)
                Log.d(TAG, "Gusto marcado para adición: ${gusto.nombre}")
            }
        }

        // Actualizar la UI
        updateGustosState()
    }

    // Añadir un ingrediente como gusto
    fun addIngredientAsGusto(ingredient: Ingredient) {
        // Crear un nuevo Gusto basado en el Ingredient
        val newGusto = Gusto(
            id = ingredient.id,
            nombre = ingredient.name
        )

        Log.d(TAG, "Añadiendo ingrediente como gusto: ${newGusto.nombre} (ID: ${newGusto.id})")

        // Verificar si ya está en los gustos
        val current = (_gustosState.value as? GustosState.Success)?.gustos
            ?.map { it.gusto } ?: emptyList()

        // Si el gusto ya existe, solo lo marcamos para añadir
        val existingGusto = current.firstOrNull { it.id == newGusto.id }

        if (existingGusto != null) {
            if (!isGustoSelected(existingGusto.id)) {
                toggleGustoSelection(existingGusto)
            }
        } else {
            // Si no existe, añadimos el nuevo gusto a la lista y lo marcamos como pendiente
            val updatedGustos = current + newGusto
            pendingAdditions.add(newGusto.id)

            // Actualizamos el estado con la nueva lista
            val gustosWithSelection = updatedGustos.map { gusto ->
                GustoWithSelection(
                    gusto = gusto,
                    isSelected = isGustoSelected(gusto.id)
                )
            }

            _gustosState.value = GustosState.Success(gustosWithSelection)
        }
    }

    // Guardar los cambios en los gustos del usuario
    fun saveUserGustos() {
        viewModelScope.launch {
            Log.d(TAG, "Guardando cambios: ${pendingAdditions.size} adiciones, ${pendingRemovals.size} eliminaciones")
            _saveState.value = SaveState.LOADING

            try {
                var success = true
                var totalProcessed = 0
                var successfulOperations = 0

                // Procesar adiciones
                for (gustoId in pendingAdditions) {
                    totalProcessed++
                    val result = addGustoToUserUseCase(gustoId)
                    if (result) {
                        successfulOperations++
                        Log.d(TAG, "Gusto añadido correctamente: $gustoId")
                    } else {
                        Log.e(TAG, "Error al añadir gusto: $gustoId")
                        success = false
                    }
                }

                // Procesar eliminaciones
//                for (gustoId in pendingRemovals) {
//                    totalProcessed++
//                    val result = removeGustoFromUserUseCase(gustoId)
//                    if (result) {
//                        successfulOperations++
//                        Log.d(TAG, "Gusto eliminado correctamente: $gustoId")
//                    } else {
//                        Log.e(TAG, "Error al eliminar gusto: $gustoId")
//                        success = false
//                    }
//                }

                if (success) {
                    // Actualizar el estado interno
                    userGustos.removeAll(pendingRemovals)
                    userGustos.addAll(pendingAdditions)
                    pendingAdditions.clear()
                    pendingRemovals.clear()

                    Log.d(TAG, "Todos los cambios guardados correctamente")
                    _saveState.value = SaveState.SUCCESS
                } else {
                    Log.e(TAG, "Algunos cambios no se pudieron guardar: $successfulOperations de $totalProcessed operaciones exitosas")
                    _saveState.value = SaveState.ERROR
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar cambios en gustos", e)
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