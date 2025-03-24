package com.example.recetas.register.presentation

import android.util.Log
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
import com.google.firebase.messaging.FirebaseMessaging

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterUi(
    registerViewModel: RegisterViewModel,
    navController: NavController,
    onNavigateToLogin: () -> Unit,
) {
    val ingredientes by registerViewModel.gustos.collectAsState()
    val registrationState by registerViewModel.registrationState.collectAsState()

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Estado para ingredientes seleccionados
    var selectedIngredientIds by remember { mutableStateOf(setOf<Int>()) }

    // Estado para mostrar el ingrediente seleccionado
    var selectedIngredientName by remember { mutableStateOf("Seleccionar ingrediente") }

    var expanded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var fcmToken by remember { mutableStateOf("") }

    // Efecto para cargar los gustos al iniciar la pantalla
    LaunchedEffect(key1 = Unit) {
        registerViewModel.loadIngredients()
    }

    LaunchedEffect(Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                fcmToken = task.result ?: ""
                Log.d("FCM", "Token obtenido para registro: $fcmToken")
            } else {
                Log.e("FCM", "Error obteniendo token FCM", task.exception)
            }
        }
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
                // Mostrar mensaje de error
                showError = true
                errorMessage = "Error en el registro. Por favor, intenta nuevamente."
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

                // Campo de username
                Text(
                    text = "Username",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    color = PrimaryPink
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryPink,
                        unfocusedBorderColor = SecondaryPink
                    ),
                    placeholder = { Text("Choose a username") },
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
                    // Mostrar indicador de carga si los ingredientes aún no están cargados
                    if (ingredientes.isEmpty()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(50.dp)
                                .align(Alignment.Center),
                            color = PrimaryPink
                        )
                    } else {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(text = selectedIngredientName)
                        }

                        // Mostrar el DropdownMenu
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            ingredientes.forEach { gusto ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedIngredientIds = setOf(gusto.id)
                                        selectedIngredientName = gusto.nombre
                                        expanded = false
                                    },
                                    text = { Text(gusto.nombre) }
                                )
                            }
                        }
                    }
                }

                // Botón para registro
                Button(
                    onClick = {
                        // Validar inputs antes de registrar
                        if (validateInputs(name, username, email, password, confirmPassword)) {
                            // Verificar que se hayan seleccionado ingredientes
                            if (selectedIngredientIds.isEmpty()) {
                                showError = true
                                errorMessage = "Por favor, selecciona un ingrediente"
                                return@Button
                            }

                            registerViewModel.registerUser(
                                name,
                                username,
                                email,
                                password,
                                fcmToken, // Pasar el token FCM
                                selectedIngredientIds.toList()
                            )
                        } else {
                            // Mostrar error de validación
                            showError = true
                            errorMessage = "Por favor, complete todos los campos correctamente"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink)
                ) {
                    Text(text = "Register", color = Color.White)
                }

                // Mostrar mensaje de error si es necesario
                if (showError) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
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
    username: String,
    email: String,
    password: String,
    confirmPassword: String
): Boolean {
    if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
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