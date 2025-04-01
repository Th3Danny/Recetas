package com.example.recetas.login.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.recetas.ui.theme.*

@Composable
fun LoginUi(
    loginViewModel: LoginViewModel,
    navController: NavController
) {
    // Observamos los valores de LiveData como estados composables
    val username by loginViewModel.username.observeAsState("")
    val password by loginViewModel.password.observeAsState("")
    val success by loginViewModel.success.observeAsState(false)
    val error by loginViewModel.error.observeAsState("")
    val isLoading by loginViewModel.isLoading.observeAsState(false)

    // Efecto para navegar cuando el login es exitoso
    LaunchedEffect(key1 = success) {
        if (success) {
            // Enviamos el token FCM después de un login exitoso

            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
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
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Iniciar Sesión",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPink,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Email field
                OutlinedTextField(
                    value = username,
                    onValueChange = { loginViewModel.onChangeUsername(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPink,
                        unfocusedBorderColor = SecondaryPink
                    ),
                    placeholder = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { loginViewModel.onChangePassword(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPink,
                        unfocusedBorderColor = SecondaryPink
                    ),
                    placeholder = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Forgot password button
                TextButton(
                    onClick = { /* Implementar recuperación de contraseña */ },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = PrimaryPink
                    )
                }

                // Mostrar mensaje de error si existe
                if (error.isNotEmpty()) {
                    Text(
                        text = error,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Sign in button
                Button(
                    onClick = {
                        if (username.isNotEmpty() && password.isNotEmpty()) {
                            loginViewModel.loginUser(username, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPink,
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(text = "Iniciar sesión")
                    }
                }

                // Register link
                TextButton(
                    onClick = { navController.navigate("register") },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "¿No tienes cuenta? Regístrate",
                        color = PrimaryPink
                    )
                }
            }
        }
    }
}