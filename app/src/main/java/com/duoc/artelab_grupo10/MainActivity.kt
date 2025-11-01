package com.duoc.artelab_grupo10

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.duoc.artelab_grupo10.di.AppModule
import com.duoc.artelab_grupo10.domain.model.UiState
import com.duoc.artelab_grupo10.navigation.Screen
import com.duoc.artelab_grupo10.ui.screens.home.HomeScreen
import com.duoc.artelab_grupo10.ui.screens.login.LoginScreen
import com.duoc.artelab_grupo10.ui.screens.profile.ProfileScreen
import com.duoc.artelab_grupo10.ui.screens.register.RegisterScreen
import com.duoc.artelab_grupo10.ui.screens.splash.SplashScreen
import com.duoc.artelab_grupo10.ui.theme.Artelab_Grupo10Theme
import com.duoc.artelab_grupo10.ui.viewmodel.LoginViewModel
import com.duoc.artelab_grupo10.ui.viewmodel.ProfileViewModel
import com.duoc.artelab_grupo10.ui.viewmodel.RegisterViewModel
import com.duoc.artelab_grupo10.ui.viewmodel.SplashViewModel
import com.duoc.artelab_grupo10.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.first

/**
 * MainActivity - Punto de entrada de la aplicación
 *
 * Configuración:
 * - NavHost con todas las pantallas
 * - ViewModels integrados con AppModule
 * - Navegación completa con estados Loading/Success/Error
 * - Autenticación real con API
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Artelab_Grupo10Theme {
                ArtelabApp()
            }
        }
    }
}

/**
 * Composable principal de la app
 * Configura el NavHost con todas las rutas y ViewModels
 */
@Composable
fun ArtelabApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val factory = ViewModelFactory(context)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // ========== SPLASH SCREEN ==========
        composable(route = Screen.Splash.route) {
            val viewModel: SplashViewModel = viewModel(factory = factory)
            val sessionState by viewModel.sessionState.collectAsState()

            // Observar estado y navegar según resultado
            LaunchedEffect(sessionState) {
                when (sessionState) {
                    is UiState.Success -> {
                        val hasSession = (sessionState as UiState.Success).data
                        val destination = if (hasSession) Screen.Home.route else Screen.Login.route

                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                    else -> { /* Mantener en splash */ }
                }
            }

            SplashScreen(
                navController = navController,
                hasActiveSession = {
                    // Ya no se usa, el ViewModel maneja esto
                    false
                }
            )
        }

        // ========== LOGIN SCREEN ==========
        composable(route = Screen.Login.route) {
            val viewModel: LoginViewModel = viewModel(factory = factory)
            val uiState by viewModel.uiState.collectAsState()

            // Navegar a Home si login exitoso
            LaunchedEffect(uiState) {
                if (uiState is UiState.Success) {
                    // Obtener userId y userName de DataStore
                    val prefs = AppModule.providePreferencesManager(context)
                    val userId = prefs.getUserId().first() ?: 1

                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                    viewModel.resetState()
                }
            }

            LoginScreen(
                navController = navController,
                onLoginClick = { email, password ->
                    viewModel.login(email, password)
                    Result.success(Unit)
                }
            )
        }

        // ========== REGISTER SCREEN ==========
        composable(route = Screen.Register.route) {
            val viewModel: RegisterViewModel = viewModel(factory = factory)
            val uiState by viewModel.uiState.collectAsState()

            // Navegar a Home si registro exitoso
            LaunchedEffect(uiState) {
                if (uiState is UiState.Success) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                    viewModel.resetState()
                }
            }

            RegisterScreen(
                navController = navController,
                onRegisterClick = { email, password, name ->
                    viewModel.register(name, email, password, password, true)
                    Result.success(Unit)
                }
            )
        }

        // ========== HOME SCREEN ==========
        composable(route = Screen.Home.route) {
            val prefs = AppModule.providePreferencesManager(context)
            val userName by prefs.getUserName().collectAsState(initial = "Usuario")
            val userId by prefs.getUserId().collectAsState(initial = 1)

            HomeScreen(
                navController = navController,
                userName = userName ?: "Usuario",
                userId = userId ?: 1
            )
        }

        // ========== PROFILE SCREEN ==========
        composable(
            route = Screen.Profile.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 1
            val viewModel: ProfileViewModel = viewModel(factory = factory)
            val profileState by viewModel.profileState.collectAsState()

            // Cargar datos al entrar
            LaunchedEffect(Unit) {
                viewModel.loadUserData()
            }

            ProfileScreen(
                navController = navController,
                userId = userId,
                profileState = profileState,
                onUpdateAvatar = { uri ->
                    viewModel.updateAvatar(userId, uri)
                    Result.success(Unit)
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}