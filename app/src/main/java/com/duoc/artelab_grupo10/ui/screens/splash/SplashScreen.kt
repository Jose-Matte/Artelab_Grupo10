package com.duoc.artelab_grupo10.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.duoc.artelab_grupo10.R
import com.duoc.artelab_grupo10.navigation.Screen
import kotlinx.coroutines.delay

/**
 * Pantalla de Splash/Bienvenida
 *
 * Funcionalidad:
 * - Muestra logo y nombre de la app durante 2 segundos
 * - Verifica si hay token guardado en DataStore
 * - Redirige a Login si no hay sesión
 * - Redirige a Home si hay sesión activa
 *
 * CRÍTICO: Esta pantalla previene loops de navegación
 */
@Composable
fun SplashScreen(
    navController: NavController,
    hasActiveSession: suspend () -> Boolean
) {
    // Ejecutar verificación al montar la pantalla
    LaunchedEffect(key1 = true) {
        // Esperar 2 segundos para mostrar splash
        delay(2000)

        // Verificar si hay sesión activa
        val isLoggedIn = hasActiveSession()

        // Navegar según resultado
        if (isLoggedIn) {
            // Hay sesión → Ir a Home
            navController.navigate(Screen.Home.route) {
                // Limpiar backstack para prevenir volver a Splash
                popUpTo(Screen.Splash.route) {
                    inclusive = true
                }
            }
        } else {
            // No hay sesión → Ir a Login
            navController.navigate(Screen.Login.route) {
                // Limpiar backstack para prevenir volver a Splash
                popUpTo(Screen.Splash.route) {
                    inclusive = true
                }
            }
        }
    }

    // UI del Splash
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo o icono (si existe drawable/ic_launcher_foreground)
            // Si no existe, esto fallará - reemplazar con Text o Icon
            /*
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo ArteLab",
                modifier = Modifier.size(120.dp)
            )
            */

            // Alternativa: Icono con Material Icons
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                contentDescription = "Logo ArteLab",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nombre de la app
            Text(
                text = "ArteLab",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Plataforma de Arte Digital",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
