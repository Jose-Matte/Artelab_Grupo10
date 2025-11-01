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
 * ViewModel para la pantalla de Registro
 *
 * Responsabilidades:
 * - Validar todos los campos del formulario en tiempo real
 * - Manejar el proceso de registro con AuthRepository
 * - Exponer estados UI (Loading, Success, Error)
 * - Validaciones complejas (password strength, confirmación, términos)
 *
 * Estados:
 * - Idle: Formulario inicial
 * - Loading: Enviando datos al API
 * - Success: Registro exitoso, navegar a Home
 * - Error: Datos inválidos o error de red
 */
class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Estado de UI
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    // Estados de validación por campo
    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()

    private val _termsError = MutableStateFlow<String?>(null)
    val termsError: StateFlow<String?> = _termsError.asStateFlow()

    /**
     * Validar nombre
     * Debe tener mínimo 3 caracteres y solo letras (incluye tildes y ñ)
     */
    fun validateName(name: String): Boolean {
        _nameError.value = when {
            name.isBlank() -> "El nombre es obligatorio"
            name.length < 3 -> "Mínimo 3 caracteres"
            !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) ->
                "Solo se permiten letras"
            else -> null
        }
        return _nameError.value == null
    }

    /**
     * Validar email
     * Debe tener formato válido con @ y dominio
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
     * Debe tener mínimo 6 caracteres, 1 mayúscula y 1 número
     */
    fun validatePassword(password: String): Boolean {
        _passwordError.value = when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 6 -> "Mínimo 6 caracteres"
            !password.contains(Regex("[A-Z]")) -> "Debe contener al menos 1 mayúscula"
            !password.contains(Regex("[0-9]")) -> "Debe contener al menos 1 número"
            else -> null
        }
        return _passwordError.value == null
    }

    /**
     * Validar confirmación de password
     * Debe coincidir exactamente con el password
     */
    fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        _confirmPasswordError.value = when {
            confirmPassword.isBlank() -> "Debes confirmar la contraseña"
            password != confirmPassword -> "Las contraseñas no coinciden"
            else -> null
        }
        return _confirmPasswordError.value == null
    }

    /**
     * Validar aceptación de términos
     * Es obligatorio aceptar
     */
    fun validateTerms(accepted: Boolean): Boolean {
        _termsError.value = if (!accepted) {
            "Debes aceptar los términos y condiciones"
        } else {
            null
        }
        return _termsError.value == null
    }

    /**
     * Realizar registro
     * Valida todos los campos y llama al AuthRepository
     *
     * @param name Nombre completo del usuario
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param confirmPassword Confirmación de contraseña
     * @param acceptedTerms Si aceptó términos y condiciones
     */
    fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        acceptedTerms: Boolean
    ) {
        // Validar todos los campos
        val isNameValid = validateName(name)
        val isEmailValid = validateEmail(email)
        val isPasswordValid = validatePassword(password)
        val isConfirmPasswordValid = validateConfirmPassword(password, confirmPassword)
        val areTermsAccepted = validateTerms(acceptedTerms)

        // Si hay algún error, no continuar
        if (!isNameValid || !isEmailValid || !isPasswordValid ||
            !isConfirmPasswordValid || !areTermsAccepted
        ) {
            _uiState.value = UiState.Error("Por favor completa todos los campos correctamente")
            return
        }

        // Iniciar proceso de registro
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            // Llamar al repositorio
            val result = authRepository.signup(
                email = email,
                password = password,
                name = name
            )

            // Procesar resultado
            _uiState.value = result.fold(
                onSuccess = {
                    // Registro exitoso
                    UiState.Success(Unit)
                },
                onFailure = { error ->
                    // Registro falló
                    UiState.Error(error.message ?: "Error desconocido")
                }
            )
        }
    }

    /**
     * Resetear estado a Idle
     */
    fun resetState() {
        _uiState.value = UiState.Idle
        _nameError.value = null
        _emailError.value = null
        _passwordError.value = null
        _confirmPasswordError.value = null
        _termsError.value = null
    }

    /**
     * Limpiar mensaje de error
     */
    fun clearError() {
        if (_uiState.value is UiState.Error) {
            _uiState.value = UiState.Idle
        }
    }
}
