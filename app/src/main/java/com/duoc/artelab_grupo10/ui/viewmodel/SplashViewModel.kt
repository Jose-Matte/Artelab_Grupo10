package com.duoc.artelab_grupo10.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.artelab_grupo10.data.repository.AuthRepository
import com.duoc.artelab_grupo10.domain.model.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Splash
 *
 * Responsabilidades:
 * - Verificar si hay sesión activa (token válido en DataStore)
 * - Decidir ruta de navegación inicial (Login o Home)
 * - Mostrar splash por 2 segundos mínimo para UX
 *
 * Estados:
 * - Loading: Verificando token
 * - Success(true): Hay sesión activa → Navegar a Home
 * - Success(false): No hay sesión → Navegar a Login
 */
class SplashViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Estado de verificación de sesión
    private val _sessionState = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val sessionState: StateFlow<UiState<Boolean>> = _sessionState.asStateFlow()

    init {
        // Verificar sesión al crear el ViewModel
        checkSession()
    }

    /**
     * Verifica si hay una sesión activa
     * Espera mínimo 2 segundos para mostrar el splash
     */
    private fun checkSession() {
        viewModelScope.launch {
            _sessionState.value = UiState.Loading

            // Esperar mínimo 2 segundos para UX
            delay(2000)

            // Verificar si hay token guardado
            val hasSession = authRepository.hasActiveSession()

            _sessionState.value = UiState.Success(hasSession)
        }
    }

    /**
     * Forzar recarga de verificación de sesión
     * Útil si el usuario vuelve a Splash
     */
    fun recheckSession() {
        checkSession()
    }
}
