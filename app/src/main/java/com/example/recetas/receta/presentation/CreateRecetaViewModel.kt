package com.example.recetas.receta.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetas.gustos.data.model.Gusto
import com.example.recetas.receta.data.model.Category
import com.example.recetas.register.data.model.Ingredient
import com.example.recetas.receta.domain.CreateRecetaUseCase
import com.example.recetas.receta.domain.GetCategoriesUseCase
//import com.example.recetas.receta.domain.GetGustosUseCase
import com.example.recetas.receta.domain.GetIngredientsUseCase
import com.example.recetas.receta.domain.GetPendingOperationsCountUseCase
import com.example.recetas.receta.domain.SincronizarRecetasPendientesUseCase
import kotlinx.coroutines.launch

class CreateRecetaViewModel(
    private val createRecetaUseCase: CreateRecetaUseCase,
//    private val getGustosUseCase: GetGustosUseCase,
    private val getIngredientsUseCase: GetIngredientsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val sincronizarRecetasPendientesUseCase: SincronizarRecetasPendientesUseCase,
    private val getPendingOperationsCountUseCase: GetPendingOperationsCountUseCase
) : ViewModel() {

    // Propiedades existentes
    private val _title = MutableLiveData("")
    val title: LiveData<String> = _title

    private val _description = MutableLiveData("")
    val description: LiveData<String> = _description

    private val _instructions = MutableLiveData("")
    val instructions: LiveData<String> = _instructions

    private val _preparationTime = MutableLiveData(10)
    val preparationTime: LiveData<Int> = _preparationTime

    private val _cookingTime = MutableLiveData(15)
    val cookingTime: LiveData<Int> = _cookingTime

    private val _servings = MutableLiveData(4)
    val servings: LiveData<Int> = _servings

    private val _difficulty = MutableLiveData("Medio")
    val difficulty: LiveData<String> = _difficulty

    private val _gustosDisponibles = MutableLiveData<List<Gusto>>(emptyList())
    val gustosDisponibles: LiveData<List<Gusto>> = _gustosDisponibles

    private val _selectedGustos = MutableLiveData<List<Gusto>>(emptyList())
    val selectedGustos: LiveData<List<Gusto>> = _selectedGustos

    private val _ingredients = MutableLiveData<List<Ingredient>>(emptyList())
    val ingredients: LiveData<List<Ingredient>> = _ingredients

    private val _selectedIngredients = MutableLiveData<List<Ingredient>>(emptyList())
    val selectedIngredients: LiveData<List<Ingredient>> = _selectedIngredients

    // Nuevas propiedades para categorías
    private val _categories = MutableLiveData<List<Category>>(emptyList())
    val categories: LiveData<List<Category>> = _categories

    private val _selectedCategories = MutableLiveData<List<Category>>(emptyList())
    val selectedCategories: LiveData<List<Category>> = _selectedCategories

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData("")
    val error: LiveData<String> = _error

    private val _isSuccess = MutableLiveData(false)
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _pendingOperationsCount = MutableLiveData(0)
    val pendingOperationsCount: LiveData<Int> = _pendingOperationsCount
    private val _imagePath = MutableLiveData<String?>(null)
    private var imagePath: String? = null

    init {
//        loadGustos()
        loadIngredients()
        loadCategories()
        loadPendingOperationsCount()
    }

    // Métodos existentes
//    fun loadGustos() {
//        viewModelScope.launch {
//            try {
//                _isLoading.postValue(true)
//                _error.postValue("")
//
//                val result = getGustosUseCase()
//
//                result.onSuccess { gustos ->
//                    _gustosDisponibles.postValue(gustos)
//                }.onFailure { exception ->
//                    Log.e("CreateRecetaViewModel", "Error al cargar gustos: ${exception.message}")
//                    _error.postValue("Error al cargar las categorías: ${exception.message}")
//                }
//
//            } catch (e: Exception) {
//                Log.e("CreateRecetaViewModel", "Excepción al cargar gustos: ${e.message}")
//                _error.postValue("Error: ${e.message}")
//            } finally {
//                _isLoading.postValue(false)
//            }
//        }
//    }

    fun loadIngredients() {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)
                _error.postValue("")

                val result = getIngredientsUseCase()

                result.onSuccess { ingredients ->
                    _ingredients.postValue(ingredients)
                }.onFailure { exception ->
                    Log.e("CreateRecetaViewModel", "Error al cargar ingredientes: ${exception.message}")
                    _error.postValue("Error al cargar los ingredientes: ${exception.message}")
                }

            } catch (e: Exception) {
                Log.e("CreateRecetaViewModel", "Excepción al cargar ingredientes: ${e.message}")
                _error.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Nuevo método para cargar categorías
    fun loadCategories() {
        viewModelScope.launch {
            try {
                Log.d("CreateRecetaViewModel", "Iniciando carga de categorías")
                _isLoading.postValue(true)
                _error.postValue("")

                val result = getCategoriesUseCase()

                result.onSuccess { categories ->
                    Log.d("CreateRecetaViewModel", "Categorías cargadas exitosamente: ${categories.size}")
                    _categories.postValue(categories)
                }.onFailure { exception ->
                    Log.e("CreateRecetaViewModel", "Error al cargar categorías: ${exception.message}", exception)
                    _error.postValue("Error al cargar las categorías: ${exception.message}")
                }
            } catch (e: Exception) {
                Log.e("CreateRecetaViewModel", "Excepción al cargar categorías", e)
                _error.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Métodos para manejar categorías
    fun addCategory(category: Category) {
        val currentList = _selectedCategories.value ?: emptyList()
        if (!currentList.any { it.id == category.id }) {
            _selectedCategories.value = currentList + category
        }
    }

    fun removeCategory(category: Category) {
        val currentList = _selectedCategories.value ?: emptyList()
        _selectedCategories.value = currentList.filter { it.id != category.id }
    }

    private fun loadPendingOperationsCount() {
        viewModelScope.launch {
            try {
                val result = getPendingOperationsCountUseCase()
                result.onSuccess { count ->
                    _pendingOperationsCount.postValue(count)
                }
            } catch (e: Exception) {
                Log.e("CreateRecetaViewModel", "Error al cargar conteo de operaciones pendientes: ${e.message}")
            }
        }
    }

    fun sincronizarPendientes() {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)
                val result = sincronizarRecetasPendientesUseCase()
                result.onSuccess {
                    loadPendingOperationsCount()
                }.onFailure { exception ->
                    Log.e("CreateRecetaViewModel", "Error al sincronizar recetas: ${exception.message}")
                }
            } catch (e: Exception) {
                Log.e("CreateRecetaViewModel", "Excepción al sincronizar recetas: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    fun updateTitle(title: String) {
        _title.value = title
    }

    fun updateDescription(description: String) {
        _description.value = description
    }

    fun updateInstructions(instructions: String) {
        _instructions.value = instructions
    }

    fun updatePreparationTime(time: Int) {
        _preparationTime.value = time
    }

    fun updateCookingTime(time: Int) {
        _cookingTime.value = time
    }

    fun updateServings(servings: Int) {
        _servings.value = servings
    }

    fun updateDifficulty(difficulty: String) {
        _difficulty.value = difficulty
    }

    fun addGusto(gusto: Gusto) {
        val currentList = _selectedGustos.value ?: emptyList()
        if (!currentList.contains(gusto)) {
            _selectedGustos.value = currentList + gusto
        }
    }

    fun removeGusto(gusto: Gusto) {
        val currentList = _selectedGustos.value ?: emptyList()
        _selectedGustos.value = currentList.filter { it.id != gusto.id }
    }

    fun addIngredient(ingredient: Ingredient) {
        val currentList = _selectedIngredients.value ?: emptyList()
        if (!currentList.any { it.id == ingredient.id }) {
            _selectedIngredients.value = currentList + ingredient
        }
    }

    fun removeIngredient(ingredient: Ingredient) {
        val currentList = _selectedIngredients.value ?: emptyList()
        _selectedIngredients.value = currentList.filter { it.id != ingredient.id }
    }

    fun updateIngredientQuantity(id: Int, quantity: String) {
        val currentList = _selectedIngredients.value ?: emptyList()
        val updatedList = currentList.map {
            if (it.id == id) it.copy(quantity = quantity) else it
        }
        _selectedIngredients.value = updatedList
    }

    fun updateIngredientUnit(id: Int, unit: String) {
        val currentList = _selectedIngredients.value ?: emptyList()
        val updatedList = currentList.map {
            if (it.id == id) it.copy(unit = unit) else it
        }
        _selectedIngredients.value = updatedList
    }

    fun setImagePath(path: String) {
        imagePath = path
    }
    fun clearImagePath() {
        _imagePath.value = null
    }

    // Método createReceta modificado para usar o bien las categorías o los gustos seleccionados
    fun createReceta() {
        viewModelScope.launch {
            try {
                val titleActual = _title.value ?: ""
                val descriptionActual = _description.value ?: ""
                val instructionsActual = _instructions.value ?: ""
                val preparationTimeActual = _preparationTime.value ?: 10
                val cookingTimeActual = _cookingTime.value ?: 15
                val servingsActual = _servings.value ?: 4
                val difficultyActual = _difficulty.value ?: "Medio"

                // Usamos categorías si están disponibles, de lo contrario usamos gustos
                val categoriasActuales = _selectedCategories.value ?: emptyList()
                val gustosActuales = _selectedGustos.value ?: emptyList()

                // Lista de IDs de categoría final a usar
                val categoryIds = if (categoriasActuales.isNotEmpty()) {
                    categoriasActuales.map { it.id }
                } else {
                    gustosActuales.map { it.id }
                }

                val ingredientsActuales = _selectedIngredients.value ?: emptyList()

                // Validar campos requeridos
                if (titleActual.isBlank() || descriptionActual.isBlank() || instructionsActual.isBlank() ||
                    (categoriasActuales.isEmpty() && gustosActuales.isEmpty()) || ingredientsActuales.isEmpty()) {
                    _error.postValue("Por favor, completa todos los campos requeridos")
                    return@launch
                }

                // Validar que todos los ingredientes tengan cantidad
                val invalidIngredients = ingredientsActuales.filter { it.quantity.isBlank() }
                if (invalidIngredients.isNotEmpty()) {
                    _error.postValue("Por favor, especifica la cantidad para todos los ingredientes")
                    return@launch
                }

                _isLoading.postValue(true)
                _error.postValue("")

                val result = createRecetaUseCase(
                    title = titleActual,
                    description = descriptionActual,
                    instructions = instructionsActual,
                    preparationTime = preparationTimeActual,
                    cookingTime = cookingTimeActual,
                    servings = servingsActual,
                    difficulty = difficultyActual,
                    categoryIds = categoryIds,
                    ingredients = ingredientsActuales,
                    imagePath = imagePath
                )

                result.onSuccess {
                    _isSuccess.postValue(true)
                    clearImagePath()
                    loadPendingOperationsCount()
                }.onFailure { exception ->
                    Log.e("CreateRecetaViewModel", "Error al crear receta: ${exception.message}")
                    _error.postValue("Error al crear la receta: ${exception.message}")
                }

            } catch (e: Exception) {
                Log.e("CreateRecetaViewModel", "Excepción al crear receta: ${e.message}")
                _error.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}