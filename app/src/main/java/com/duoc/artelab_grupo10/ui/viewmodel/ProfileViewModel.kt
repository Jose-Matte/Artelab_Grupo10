package com.duoc.artelab_grupo10.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.artelab_grupo10.data.local.dao.UsuarioDao
import com.duoc.artelab_grupo10.data.repository.AuthRepository
import com.duoc.artelab_grupo10.domain.model.UiState
import com.duoc.artelab_grupo10.ui.screens.profile.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Perfil
 *
 * Responsabilidades CRÍTICAS:
 * - Cargar datos del usuario desde GET /auth/me (OBLIGATORIO)
 * - Manejar actualización de foto de perfil
 * - Guardar URI de foto en Room Database (OBLIGATORIO para persistencia)
 * - Manejar cierre de sesión
 *
 * Estados:
 * - Loading: Cargando datos del usuario
 * - Success: Datos cargados correctamente
 * - Error: Falló la carga (token expirado, sin internet, etc.)
 */
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    // Estado de UI para datos del perfil
    private val _profileState = MutableStateFlow<UiState<UserData>>(UiState.Loading)
    val profileState: StateFlow<UiState<UserData>> = _profileState.asStateFlow()

    // Estado de actualización de avatar
    private val _avatarUpdateState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val avatarUpdateState: StateFlow<UiState<Unit>> = _avatarUpdateState.asStateFlow()

    /**
     * Cargar datos del usuario autenticado
     * CRÍTICO: Llama a GET /auth/me (obligatorio para evaluación)
     */
    fun loadUserData() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading

            // Llamar al endpoint /auth/me
            val result = authRepository.getMe()

            _profileState.value = result.fold(
                onSuccess = { userResponse ->
                    // Buscar URI de avatar guardado en Room
                    val localUser = usuarioDao.getUsuarioById(userResponse.id)
                    val avatarUri = localUser?.avatarUri

                    // Crear UserData con avatar local si existe
                    UiState.Success(
                        UserData(
                            id = userResponse.id,
                            email = userResponse.email,
                            name = userResponse.name,
                            avatarUri = avatarUri
                        )
                    )
                },
                onFailure = { error ->
                    UiState.Error(error.message ?: "Error al cargar perfil")
                }
            )
        }
    }

    /**
     * Actualizar foto de perfil
     * CRÍTICO: Guarda URI en Room Database para persistencia offline
     *
     * @param userId ID del usuario
     * @param avatarUri URI de la imagen seleccionada (cámara o galería)
     */
    fun updateAvatar(userId: Int, avatarUri: Uri) {
        viewModelScope.launch {
            _avatarUpdateState.value = UiState.Loading

            try {
                // Guardar URI en Room Database
                usuarioDao.updateAvatarUri(userId, avatarUri.toString())

                // Actualizar estado de perfil con nueva URI
                val currentState = _profileState.value
                if (currentState is UiState.Success) {
                    _profileState.value = UiState.Success(
                        currentState.data.copy(avatarUri = avatarUri.toString())
                    )
                }

                _avatarUpdateState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _avatarUpdateState.value = UiState.Error(
                    "Error al guardar foto: ${e.message}"
                )
            }
        }
    }

    /**
     * Cerrar sesión
     * Limpia token de DataStore y datos locales
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _profileState.value = UiState.Idle
        }
    }

    /**
     * Recargar datos del perfil
     * Útil después de volver de otra pantalla
     */
    fun refresh() {
        loadUserData()
    }

    /**
     * Resetear estado de actualización de avatar
     * Se usa después de mostrar mensaje de éxito/error
     */
    fun resetAvatarUpdateState() {
        _avatarUpdateState.value = UiState.Idle
    }
}
