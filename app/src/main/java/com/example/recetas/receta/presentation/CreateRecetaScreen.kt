package com.example.recetas.receta.presentation

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.recetas.gustos.data.model.Gusto
import com.example.recetas.receta.data.model.Category
import com.example.recetas.register.data.model.Ingredient
import com.example.recetas.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecetaScreen(
    createRecetaViewModel: CreateRecetaViewModel,
    navController: NavController
) {
    val title by createRecetaViewModel.title.observeAsState("")
    val description by createRecetaViewModel.description.observeAsState("")
    val instructions by createRecetaViewModel.instructions.observeAsState("")
    val preparationTime by createRecetaViewModel.preparationTime.observeAsState(10)
    val cookingTime by createRecetaViewModel.cookingTime.observeAsState(15)
    val servings by createRecetaViewModel.servings.observeAsState(4)
    val difficulty by createRecetaViewModel.difficulty.observeAsState("Medio")
    val gustosDisponibles by createRecetaViewModel.gustosDisponibles.observeAsState(emptyList())
    val selectedGustos by createRecetaViewModel.selectedGustos.observeAsState(emptyList())
    val ingredients by createRecetaViewModel.ingredients.observeAsState(emptyList())
    val selectedIngredients by createRecetaViewModel.selectedIngredients.observeAsState(emptyList())
    val isLoading by createRecetaViewModel.isLoading.observeAsState(false)
    val error by createRecetaViewModel.error.observeAsState("")
    val isSuccess by createRecetaViewModel.isSuccess.observeAsState(false)

    var showIngredientDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = isSuccess) {
        if (isSuccess) {
            navController.navigateUp()
        }
    }

    if (showIngredientDialog) {
        IngredientSelectionDialog(
            ingredients = ingredients,
            selectedIngredients = selectedIngredients,
            onDismiss = { showIngredientDialog = false },
            onAddIngredient = { createRecetaViewModel.addIngredient(it) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nueva Receta",
                        color = PrimaryPink,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = PrimaryPink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                actions = {
                    // Indicador de operaciones pendientes
                    val pendingCount by createRecetaViewModel.pendingOperationsCount.observeAsState(0)
                    if (pendingCount > 0) {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(32.dp)
                                .background(Color(0xFFFFE082), CircleShape)
                                .clickable { createRecetaViewModel.sincronizarPendientes() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pendingCount.toString(),
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { createRecetaViewModel.sincronizarPendientes() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sincronizar recetas pendientes",
                                tint = PrimaryPink
                            )
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp),
                            color = PrimaryPink
                        )
                    } else {
                        IconButton(
                            onClick = {
                                val selectedCategories = createRecetaViewModel.selectedCategories.value ?: emptyList()
                                if (validateInputs(title, description, instructions, selectedGustos, selectedIngredients, selectedCategories)) {
                                    createRecetaViewModel.createReceta()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Guardar",
                                tint = PrimaryPink
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundPink)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sección para subir una foto
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { /* Implementar selección de foto */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBox,
                                contentDescription = "Subir foto",
                                tint = PrimaryPink,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Añadir foto",
                                color = PrimaryPink,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Campo para el título
                OutlinedTextField(
                    value = title,
                    onValueChange = { createRecetaViewModel.updateTitle(it) },
                    label = { Text("Título de la receta") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPink,
                        unfocusedBorderColor = SecondaryPink,
                        focusedLabelColor = PrimaryPink
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Campo para la descripción
                OutlinedTextField(
                    value = description,
                    onValueChange = { createRecetaViewModel.updateDescription(it) },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPink,
                        unfocusedBorderColor = SecondaryPink,
                        focusedLabelColor = PrimaryPink
                    ),
                    shape = RoundedCornerShape(8.dp),
                    minLines = 3
                )

                // Campo para las instrucciones
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { createRecetaViewModel.updateInstructions(it) },
                    label = { Text("Instrucciones") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPink,
                        unfocusedBorderColor = SecondaryPink,
                        focusedLabelColor = PrimaryPink
                    ),
                    shape = RoundedCornerShape(8.dp),
                    minLines = 5
                )

                // Fila para tiempos y porciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tiempo de preparación
                    OutlinedTextField(
                        value = preparationTime.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { time ->
                                createRecetaViewModel.updatePreparationTime(time)
                            }
                        },
                        label = { Text("Tiempo prep.") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPink,
                            unfocusedBorderColor = SecondaryPink,
                            focusedLabelColor = PrimaryPink
                        ),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = { Text("min") }
                    )

                    // Tiempo de cocción
                    OutlinedTextField(
                        value = cookingTime.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { time ->
                                createRecetaViewModel.updateCookingTime(time)
                            }
                        },
                        label = { Text("Tiempo coc.") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPink,
                            unfocusedBorderColor = SecondaryPink,
                            focusedLabelColor = PrimaryPink
                        ),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = { Text("min") }
                    )

                    // Porciones
                    OutlinedTextField(
                        value = servings.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { portions ->
                                createRecetaViewModel.updateServings(portions)
                            }
                        },
                        label = { Text("Porciones") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPink,
                            unfocusedBorderColor = SecondaryPink,
                            focusedLabelColor = PrimaryPink
                        ),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // Selector de dificultad
                var difficultyMenuExpanded by remember { mutableStateOf(false) }
                val difficulties = listOf("Fácil", "Medio", "Difícil")

                Column {
                    Text(
                        text = "Dificultad",
                        color = PrimaryPink,
                        fontWeight = FontWeight.Medium
                    )

                    Box {
                        OutlinedTextField(
                            value = difficulty,
                            onValueChange = { },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPink,
                                unfocusedBorderColor = SecondaryPink
                            ),
                            shape = RoundedCornerShape(8.dp),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Seleccionar dificultad",
                                    modifier = Modifier.clickable { difficultyMenuExpanded = true }
                                )
                            }
                        )

                        DropdownMenu(
                            expanded = difficultyMenuExpanded,
                            onDismissRequest = { difficultyMenuExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(Color.White)
                        ) {
                            difficulties.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(text = item) },
                                    onClick = {
                                        createRecetaViewModel.updateDifficulty(item)
                                        difficultyMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Nueva sección de categorías
                Column {
                    Text(
                        text = "Categorías",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryPink
                    )

                    val categories by createRecetaViewModel.categories.observeAsState(emptyList())
                    val selectedCategories by createRecetaViewModel.selectedCategories.observeAsState(emptyList())

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = selectedCategories.any { it.id == category.id }
                            CategoryChip(
                                category = category,
                                isSelected = isSelected,
                                onToggle = {
                                    if (isSelected) {
                                        createRecetaViewModel.removeCategory(category)
                                    } else {
                                        createRecetaViewModel.addCategory(category)
                                    }
                                }
                            )
                        }
                    }
                }

                // Sección de ingredientes
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ingredientes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryPink
                        )

                        IconButton(
                            onClick = { showIngredientDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Añadir ingrediente",
                                tint = PrimaryPink
                            )
                        }
                    }

                    // Lista de ingredientes seleccionados
                    selectedIngredients.forEach { ingredient ->
                        IngredientItem(
                            ingredient = ingredient,
                            onRemove = { createRecetaViewModel.removeIngredient(ingredient) },
                            onQuantityChange = { createRecetaViewModel.updateIngredientQuantity(ingredient.id, it) },
                            onUnitChange = { createRecetaViewModel.updateIngredientUnit(ingredient.id, it) }
                        )
                    }
                }

                if (error.isNotEmpty()) {
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Botón para crear la receta
                Button(
                    onClick = {
                        // Agregamos logs para depuración
                        Log.d("CreateRecetaScreen", "Botón Publicar receta presionado")

                        // Obtener categorías seleccionadas
                        val selectedCategories = createRecetaViewModel.selectedCategories.value ?: emptyList()

                        // Comprobar si hay categorías o gustos seleccionados
                        val hasCategories = selectedCategories.isNotEmpty() || selectedGustos.isNotEmpty()

                        if (title.isNotBlank() &&
                            description.isNotBlank() &&
                            instructions.isNotBlank() &&
                            hasCategories &&
                            selectedIngredients.isNotEmpty() &&
                            selectedIngredients.all { it.quantity.isNotBlank() }) {

                            Log.d("CreateRecetaScreen", "Validación exitosa, llamando a createReceta()")
                            createRecetaViewModel.createReceta()
                        } else {
                            // Para depuración, identificamos qué validación está fallando
                            Log.d("CreateRecetaScreen", "Validación fallida. Title: ${title.isNotBlank()}, Desc: ${description.isNotBlank()}, " +
                                    "Instr: ${instructions.isNotBlank()}, Cats/Gustos: $hasCategories, " +
                                    "Ingr: ${selectedIngredients.isNotEmpty() && selectedIngredients.all { it.quantity.isNotBlank() }}")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPink,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Publicar receta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun GustoChip(
    gusto: Gusto,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) PrimaryPink else Color.White,
        contentColor = if (isSelected) Color.White else Color.Black,
        border = if (!isSelected) BorderStroke(1.dp, SecondaryPink) else null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = gusto.nombre,
                fontSize = 14.sp
            )

            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientItem(
    ingredient: Ingredient,
    onRemove: () -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit
) {
    val units = listOf("g", "kg", "ml", "L", "cdta", "cda", "taza", "unidad")
    var unitMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nombre del ingrediente
            Text(
                text = ingredient.name,
                modifier = Modifier.weight(2f)
            )

            // Campo de cantidad
            OutlinedTextField(
                value = ingredient.quantity,
                onValueChange = { onQuantityChange(it) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("Cant.") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPink,
                    unfocusedBorderColor = SecondaryPink
                )
            )

            // Selector de unidad
            Box(
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = ingredient.unit,
                    onValueChange = { },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPink,
                        unfocusedBorderColor = SecondaryPink
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Seleccionar unidad",
                            modifier = Modifier.clickable { unitMenuExpanded = true }
                        )
                    }
                )

                DropdownMenu(
                    expanded = unitMenuExpanded,
                    onDismissRequest = { unitMenuExpanded = false }
                ) {
                    units.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(text = unit) },
                            onClick = {
                                onUnitChange(unit)
                                unitMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Botón de eliminar
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar ingrediente",
                    tint = PrimaryPink
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientSelectionDialog(
    ingredients: List<Ingredient>,
    selectedIngredients: List<Ingredient>,
    onDismiss: () -> Unit,
    onAddIngredient: (Ingredient) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredIngredients = remember(searchQuery, ingredients) {
        if (searchQuery.isEmpty()) {
            ingredients
        } else {
            ingredients.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Seleccionar ingredientes",
                fontWeight = FontWeight.Bold,
                color = PrimaryPink
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar ingredientes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPink,
                        unfocusedBorderColor = SecondaryPink
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    filteredIngredients.forEach { ingredient ->
                        val isSelected = selectedIngredients.any { it.id == ingredient.id }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!isSelected) {
                                        onAddIngredient(ingredient)
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ingredient.name,
                                modifier = Modifier.weight(1f)
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryPink
                                )
                            }
                        }
                        Divider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = PrimaryPink)
            }
        },
        containerColor = Color.White
    )
}

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) PrimaryPink else Color.White,
        contentColor = if (isSelected) Color.White else Color.Black,
        border = if (!isSelected) BorderStroke(1.dp, SecondaryPink) else null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = category.name,
                fontSize = 14.sp
            )

            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

private fun validateInputs(
    title: String,
    description: String,
    instructions: String,
    selectedGustos: List<Gusto>,
    selectedIngredients: List<Ingredient>,
    selectedCategories: List<Category> = emptyList()  // Añadir parámetro opcional
): Boolean {
    // Mostrar mensaje de error para ayudar a depurar
    val hasTitle = title.isNotBlank()
    val hasDescription = description.isNotBlank()
    val hasInstructions = instructions.isNotBlank()
    val hasCategories = selectedCategories.isNotEmpty() || selectedGustos.isNotEmpty()
    val hasIngredients = selectedIngredients.isNotEmpty()
    val validIngredients = selectedIngredients.all { it.quantity.isNotBlank() }

    // Log para depuración
    Log.d("CreateRecetaScreen", "Validación: title=$hasTitle, desc=$hasDescription, instr=$hasInstructions, cats=$hasCategories, ingr=$hasIngredients, validIngr=$validIngredients")

    return hasTitle && hasDescription && hasInstructions &&
            hasCategories && hasIngredients && validIngredients
}