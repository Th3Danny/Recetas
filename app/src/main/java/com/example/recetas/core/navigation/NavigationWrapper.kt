package com.example.recetas.core.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recetas.core.network.RetrofitHelper.gustosService
import com.example.recetas.core.sesion.SessionManager
import com.example.recetas.core.sesion.SessionManagerImpl
import com.example.recetas.gustos.data.repository.GustosRepository
import com.example.recetas.gustos.domain.FetchGustosUseCase
import com.example.recetas.gustos.domain.FetchGustosUseCase.AddGustoToUserUseCase
import com.example.recetas.gustos.domain.FetchGustosUseCase.GetUserGustosUseCase
import com.example.recetas.gustos.domain.FetchGustosUseCase.RemoveGustoFromUserUseCase
import com.example.recetas.gustos.presentation.GustosScreen
import com.example.recetas.gustos.presentation.GustosViewModel
import com.example.recetas.gustos.presentation.GustosViewModelFactory
import com.example.recetas.home.data.repository.RecetaRepository
import com.example.recetas.home.model.GetRecetasUseCase
import com.example.recetas.home.presentation.HomeScreen
import com.example.recetas.home.presentation.HomeViewModel
import com.example.recetas.home.presentation.HomeViewModelFactory
import com.example.recetas.login.data.repository.LoginRepository
import com.example.recetas.login.domain.LoginUseCase
import com.example.recetas.login.presentation.LoginUi
import com.example.recetas.login.presentation.LoginViewModel
import com.example.recetas.login.presentation.LoginViewModelFactory
import com.example.recetas.receta.presentation.CreateRecetaScreen
import com.example.recetas.receta.presentation.CreateRecetaViewModel
import com.example.recetas.receta.presentation.CreateRecetaViewModelFactory
import com.example.recetas.register.data.repository.RegisterRepository
import com.example.recetas.register.domain.CreateUserUseCase
import com.example.recetas.register.presentation.RegisterUi
import com.example.recetas.register.presentation.RegisterViewModel
import com.example.recetas.register.presentation.RegisterViewModelFactory

@SuppressLint("RestrictedApi")
@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "login") {
        // Pantalla de Login
        composable("login") {
            val loginRepository = LoginRepository()
            val loginUseCase = LoginUseCase(loginRepository)
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(loginUseCase, context)
            )

            LoginUi(
                loginViewModel = loginViewModel,
                navController = navController
            )
        }

        // Pantalla de Registro
        composable("register") {
            val registerRepository = RegisterRepository()
            val createUserUseCase = CreateUserUseCase(registerRepository)

            // Crear GustosRepository
            val sessionManager = SessionManagerImpl(context)
            val gustosRepository = GustosRepository(gustosService, sessionManager)
            val fetchGustosUseCase = FetchGustosUseCase(gustosRepository)

            val registerViewModel: RegisterViewModel = viewModel(
                factory = RegisterViewModelFactory(createUserUseCase, fetchGustosUseCase)
            )

            RegisterUi(
                registerViewModel = registerViewModel,
                navController = navController,
                onNavigateToLogin = { navController.navigate("login") }
            )
        }

        // Pantalla de Home
        composable("home") {
            val recetaRepository = RecetaRepository()
            val getRecetasUseCase = GetRecetasUseCase(recetaRepository)
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(getRecetasUseCase)
            )

            HomeScreen(
                homeViewModel = homeViewModel,
                navController = navController
            )
        }

        // Pantalla de Creación de Receta
        composable("create_receta") {
            val createRecetaViewModel: CreateRecetaViewModel = viewModel(
                factory = CreateRecetaViewModelFactory(context)
            )

            CreateRecetaScreen(
                createRecetaViewModel = createRecetaViewModel,
                navController = navController
            )
        }

        // Pantalla de Gustos
        composable("gustos") {
            val sessionManager: SessionManager = SessionManagerImpl(context) // Usa la interfaz
            val gustosRepository = GustosRepository(gustosService,
                sessionManager as SessionManagerImpl
            )

            val fetchGustosUseCase = FetchGustosUseCase(gustosRepository)
            val getUserGustosUseCase = GetUserGustosUseCase(gustosRepository)
            val addGustoToUserUseCase = AddGustoToUserUseCase(gustosRepository)
            val removeGustoFromUserUseCase = RemoveGustoFromUserUseCase(gustosRepository)

            val gustosViewModel: GustosViewModel = viewModel(
                factory = GustosViewModelFactory(
                    fetchGustosUseCase,
                    getUserGustosUseCase,
                    addGustoToUserUseCase,
                    removeGustoFromUserUseCase
                )
            )

            GustosScreen(
                gustosViewModel = gustosViewModel,
                navController = navController
            )
        }

        // Pantalla de Detalle de Receta
        composable(
            route = "receta/{recetaId}",
            arguments = listOf(
                navArgument("recetaId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val recetaId = backStackEntry.arguments?.getInt("recetaId") ?: 0
            val recetaRepository = RecetaRepository()
            // Aquí irían los casos de uso y ViewModel para el detalle de receta
            // Por ahora lo dejamos como placeholder

            // DetailRecetaScreen(
            //     detailViewModel = detailViewModel,
            //     navController = navController,
            //     recetaId = recetaId
            // )

            // Como placeholder, redirigimos al home
            navController.navigate("home")
        }

        // Pantalla de Perfil (placeholder)
        composable("profile") {
            // ProfileScreen(
            //     profileViewModel = profileViewModel,
            //     navController = navController
            // )

            // Como placeholder, redirigimos al home
            navController.navigate("home")
        }
    }
}