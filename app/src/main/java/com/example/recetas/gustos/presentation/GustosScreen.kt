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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.recetas.ui.theme.*

@Composable
fun GustosScreen(
    gustosViewModel: GustosViewModel,
    navController: NavController
) {
    val gustosState by gustosViewModel.gustosState.collectAsState()
    val saveState by gustosViewModel.saveState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        gustosViewModel.loadGustos()
    }

    LaunchedEffect(key1 = saveState) {
        if (saveState == SaveState.SUCCESS) {
            // Navegar de vuelta o mostrar mensaje de éxito
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPink)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Mis Gustos",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPink,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (gustosState) {
                is GustosState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryPink)
                    }
                }

                is GustosState.Success -> {
                    val gustos = (gustosState as GustosState.Success).gustos

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

                    Button(
                        onClick = { gustosViewModel.saveUserGustos() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPink,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Guardar cambios")
                    }
                }

                is GustosState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error al cargar los gustos. Por favor, intenta de nuevo.",
                            color = Color.Red
                        )
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
        shape = RoundedCornerShape(8.dp)
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