package com.example.recetas.register.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.recetas.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterUi(
    registerViewModel: RegisterViewModel,
    navController: NavController,
    onNavigateToLogin: () -> Unit
) {
    val gustos by registerViewModel.gustos.collectAsState()
    val registrationState by registerViewModel.registrationState.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedGustoIndex by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    // Efecto para cargar los gustos al iniciar la pantalla
    LaunchedEffect(key1 = Unit) {
        registerViewModel.fetchGustos()
    }

    // Efecto para manejar el estado de registro
    LaunchedEffect(key1 = registrationState) {
        when (registrationState) {
            RegistrationState.SUCCESS -> {
                // Navegar a la pantalla de inicio después de un registro exitoso
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            RegistrationState.ERROR -> {
                // Mostrar un mensaje de error (ya está implementado en la UI)
            }
            else -> {
                // No hacer nada en otros estados
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPink)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            // Cambio: Usar CardDefaults.cardElevation() en lugar de elevation directamente
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundPink)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Register",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPink,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Campo de nombre
                Text(
                    text = "Name",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    color = PrimaryPink
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryPink,
                        unfocusedBorderColor = SecondaryPink
                    ),
                    placeholder = { Text("Enter your name") },
                    shape = RoundedCornerShape(8.dp)
                )

                // Campo de correo electrónico
                Text(
                    text = "Email",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    color = PrimaryPink
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryPink,
                        unfocusedBorderColor = SecondaryPink
                    ),
                    placeholder = { Text("Enter your email") },
                    shape = RoundedCornerShape(8.dp)
                )

                // Campo de contraseña
                Text(
                    text = "Password",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    color = PrimaryPink
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryPink,
                        unfocusedBorderColor = SecondaryPink
                    ),
                    placeholder = { Text("Enter password") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp)
                )

                // Campo de confirmación de contraseña
                Text(
                    text = "Confirm Password",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    color = PrimaryPink
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryPink,
                        unfocusedBorderColor = SecondaryPink
                    ),
                    placeholder = { Text("Confirm password") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp)
                )

                // Dropdown de gustos
                Text(
                    text = "Selecciona un gusto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    color = PrimaryPink
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = if (gustos.isNotEmpty() && selectedGustoIndex < gustos.size)
                                gustos[selectedGustoIndex].nombre
                            else
                                "Seleccionar gusto",
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        gustos.forEachIndexed { index, gusto ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedGustoIndex = index
                                    expanded = false
                                },
                                text = { // Aquí se define el texto del ítem
                                    Text(text = gusto.nombre)
                                }
                            )
                        }
                    }
                }

                // Mensaje de error
                if (registrationState == RegistrationState.ERROR) {
                    Text(
                        text = "Error al registrar. Verifica tus datos.",
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Botón de registro
                Button(
                    onClick = {
                        if (validateInputs(name, email, password, confirmPassword)) {
                            if (gustos.isNotEmpty()) {
                                registerViewModel.registerUser(
                                    name,
                                    email,
                                    password,
                                    gustos[selectedGustoIndex].id
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPink,
                        contentColor = Color.White
                    )
                ) {
                    if (registrationState == RegistrationState.LOADING) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(text = "Create")
                    }
                }

                // Enlace para iniciar sesión
                TextButton(
                    onClick = { onNavigateToLogin() },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "¿Ya tienes una cuenta? Iniciar sesión",
                        color = PrimaryPink
                    )
                }
            }
        }
    }
}

// Función de validación mejorada
private fun validateInputs(
    name: String,
    email: String,
    password: String,
    confirmPassword: String
): Boolean {
    if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
        return false
    }

    if (password != confirmPassword) {
        return false
    }

    // Validar formato de correo electrónico
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        return false
    }

    // Validar longitud de la contraseña
    if (password.length < 6) {
        return false
    }

    return true
}