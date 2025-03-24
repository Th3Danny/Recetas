package com.example.recetas.gustos.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.recetas.register.data.model.Ingredient
import com.example.recetas.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GustosScreen(
    gustosViewModel: GustosViewModel,
    navController: NavController
) {
    val ingredients by gustosViewModel.ingredients.observeAsState(emptyList())
    val gustosState by gustosViewModel.gustosState.collectAsState()
    val saveState by gustosViewModel.saveState.collectAsState()
    val isLoading by gustosViewModel.isLoading.observeAsState(false)
    val error by gustosViewModel.error.observeAsState("")

    // Estado para rastrear los ingredientes seleccionados
    var selectedIngredients by remember { mutableStateOf(setOf<Int>()) }

    // Cargar gustos e ingredientes al iniciar la pantalla
    LaunchedEffect(key1 = Unit) {
        gustosViewModel.loadIngredients()
        gustosViewModel.loadUserGustos()
    }

    // Manejar el estado de guardado
    LaunchedEffect(key1 = saveState) {
        if (saveState == SaveState.SUCCESS) {
            // Navegar de vuelta o mostrar mensaje de éxito
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Ingredientes y Gustos",
                        color = PrimaryPink,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundPink)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Mostrar mensaje de error si existe
                if (error.isNotEmpty()) {
                    Text(
                        text = error,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Mostrar indicador de carga
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryPink)
                    }
                } else {
                    // Sección de Gustos
                    Text(
                        text = "Mis Gustos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPink,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Selecciona los ingredientes que te gustan para ver recetas personalizadas",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    when (gustosState) {
                        is GustosState.Success -> {
                            val gustos = (gustosState as GustosState.Success).gustos

                            if (gustos.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(items = gustos) { gustoWithSelection ->
                                        GustoItem(
                                            nombre = gustoWithSelection.gusto.nombre,
                                            isSelected = gustoWithSelection.isSelected,
                                            onToggle = { gustosViewModel.toggleGustoSelection(gustoWithSelection.gusto) }
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No hay gustos disponibles.",
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        is GustosState.Error -> {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Error al cargar los gustos. Por favor, intenta de nuevo.",
                                    color = Color.Red
                                )
                            }
                        }

                        else -> {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PrimaryPink)
                            }
                        }
                    }

                    // Botón para guardar gustos
                    Button(
                        onClick = { gustosViewModel.saveUserGustos() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPink,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = saveState != SaveState.LOADING
                    ) {
                        if (saveState == SaveState.LOADING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Guardar mis gustos")
                        }
                    }

                    // Sección de Ingredientes
                    Text(
                        text = "Ingredientes disponibles",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPink,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    Text(
                        text = "Selecciona para añadir a tus gustos",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (ingredients.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(items = ingredients) { ingredient ->
                                val isSelected = selectedIngredients.contains(ingredient.id)
                                IngredientSelectableItem(
                                    ingredient = ingredient,
                                    isSelected = isSelected,
                                    onToggle = {
                                        // Toggle selection status
                                        if (isSelected) {
                                            selectedIngredients = selectedIngredients - ingredient.id
                                        } else {
                                            selectedIngredients = selectedIngredients + ingredient.id
                                        }
                                    }
                                )
                            }
                        }

                        // Botón para añadir ingredientes seleccionados a gustos
                        Button(
                            onClick = {
                                // Añadir los ingredientes seleccionados como gustos
                                selectedIngredients.forEach { ingredientId ->
                                    val ingredient = ingredients.find { it.id == ingredientId }
                                    if (ingredient != null) {
                                        gustosViewModel.addIngredientAsGusto(ingredient)
                                    }
                                }
                                // Limpiar selecciones
                                selectedIngredients = emptySet()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8BC34A),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = selectedIngredients.isNotEmpty()
                        ) {
                            Text("Añadir ingredientes seleccionados a mis gustos")
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay ingredientes disponibles.",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GustoItem(
    nombre: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = nombre,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = Color.Black
            )

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        color = if (isSelected) PrimaryPink else Color.LightGray,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (isSelected) "Quitar" else "Añadir",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun IngredientSelectableItem(
    ingredient: Ingredient,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE1F5FE) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = ingredient.name,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = Color.Black
            )

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        color = if (isSelected) Color(0xFF03A9F4) else Color.LightGray,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = if (isSelected) "Seleccionado" else "Seleccionar",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}