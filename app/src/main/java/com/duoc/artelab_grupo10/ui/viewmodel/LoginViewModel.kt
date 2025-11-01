package com.duoc.artelab_grupo10.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.artelab_grupo10.data.repository.AuthRepository
import com.duoc.artelab_grupo10.domain.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Login
 *
 * Responsabilidades:
 * - Validar credenciales en tiempo real
 * - Manejar el proceso de login con AuthRepository
 * - Exponer estados UI (Loading, Success, Error) mediante StateFlow
 * - Manejar navegación post-login
 *
 * Estados:
 * - Idle: Formulario inicial
 * - Loading: Enviando credenciales al API
 * - Success: Login exitoso, navegar a Home
 * - Error: Credenciales incorrectas o error de red
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Estado de UI privado (mutable)
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)

    // Estado de UI público (inmutable)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    // Estados de validación de campos
    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    /**
     * Validar email
     * @param email Email a validar
     * @return true si es válido, false si no
     */
    fun validateEmail(email: String): Boolean {
        _emailError.value = when {
            email.isBlank() -> "El email es obligatorio"
            !email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) ->
                "Formato de email inválido"
            else -> null
        }
        return _emailError.value == null
    }

    /**
     * Validar password
     * @param password Password a validar
     * @return true si es válido, false si no
     */
    fun validatePassword(password: String): Boolean {
        _passwordError.value = when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }
        return _passwordError.value == null
    }

    /**
     * Realizar login
     * Valida campos y llama al AuthRepository
     *
     * @param email Email del usuario
     * @param password Contraseña del usuario
     */
    fun login(email: String, password: String) {
        // Validar campos antes de enviar
        val isEmailValid = validateEmail(email)
        val isPasswordValid = validatePassword(password)

        if (!isEmailValid || !isPasswordValid) {
            _uiState.value = UiState.Error("Por favor completa todos los campos correctamente")
            return
        }

        // Iniciar proceso de login
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            // Llamar al repositorio
            val result = authRepository.login(email, password)

            // Procesar resultado
            _uiState.value = result.fold(
                onSuccess = {
                    // Login exitoso
                    UiState.Success(Unit)
                },
                onFailure = { error ->
                    // Login falló
                    UiState.Error(error.message ?: "Error desconocido")
                }
            )
        }
    }

    /**
     * Resetear estado a Idle
     * Se usa después de navegar para limpiar el estado
     */
    fun resetState() {
        _uiState.value = UiState.Idle
        _emailError.value = null
        _passwordError.value = null
    }

    /**
     * Limpiar mensaje de error
     * Se usa cuando el usuario empieza a escribir de nuevo
     */
    fun clearError() {
        if (_uiState.value is UiState.Error) {
            _uiState.value = UiState.Idle
        }
    }
}
