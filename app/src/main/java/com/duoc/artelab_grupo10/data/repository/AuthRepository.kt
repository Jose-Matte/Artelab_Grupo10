package com.duoc.artelab_grupo10.data.repository

import com.duoc.artelab_grupo10.data.local.PreferencesManager
import com.duoc.artelab_grupo10.data.remote.api.ArtelabApi
import com.duoc.artelab_grupo10.data.remote.dto.LoginRequest
import com.duoc.artelab_grupo10.data.remote.dto.LoginResponse
import com.duoc.artelab_grupo10.data.remote.dto.SignupRequest
import com.duoc.artelab_grupo10.data.remote.dto.SignupResponse
import com.duoc.artelab_grupo10.data.remote.dto.UserResponse
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException

/**
 * Repositorio de autenticación
 *
 * Maneja toda la lógica de autenticación:
 * - Registro de nuevos usuarios
 * - Inicio de sesión
 * - Obtención de datos del usuario autenticado
 * - Gestión de token y sesión
 *
 * IMPORTANTE: Usa Result<T> para manejar éxitos y errores de forma clara
 */
class AuthRepository(
    private val api: ArtelabApi,
    private val preferencesManager: PreferencesManager
) {

    /**
     * Registrar nuevo usuario
     *
     * @param email Email del usuario
     * @param password Contraseña
     * @param name Nombre del usuario
     * @return Result con SignupResponse o mensaje de error
     */
    suspend fun signup(
        email: String,
        password: String,
        name: String
    ): Result<SignupResponse> {
        return try {
            // Llamar al endpoint de registro
            val response = api.signup(
                SignupRequest(
                    email = email,
                    password = password,
                    name = name
                )
            )

            // Guardar token y sesión
            preferencesManager.saveUserSession(
                userId = response.user.id,
                email = response.user.email,
                name = response.user.name,
                token = response.authToken
            )

            // Retornar éxito
            Result.success(response)

        } catch (e: IOException) {
            // Error de conexión (sin internet)
            Result.failure(Exception("Error de conexión. Verifica tu internet."))
        } catch (e: HttpException) {
            // Error del servidor (400, 500, etc.)
            val errorMessage = when (e.code()) {
                400 -> "Datos inválidos. Verifica los campos."
                409 -> "El email ya está registrado."
                else -> "Error del servidor. Intenta más tarde."
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            // Otros errores
            Result.failure(Exception("Error desconocido: ${e.message}"))
        }
    }

    /**
     * Iniciar sesión
     *
     * @param email Email del usuario
     * @param password Contraseña
     * @return Result con LoginResponse o mensaje de error
     */
    suspend fun login(
        email: String,
        password: String
    ): Result<LoginResponse> {
        return try {
            // Llamar al endpoint de login
            val response = api.login(
                LoginRequest(
                    email = email,
                    password = password
                )
            )

            // Guardar token y sesión
            preferencesManager.saveUserSession(
                userId = response.user.id,
                email = response.user.email,
                name = response.user.name,
                token = response.authToken
            )

            // Retornar éxito
            Result.success(response)

        } catch (e: IOException) {
            Result.failure(Exception("Error de conexión. Verifica tu internet."))
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                401 -> "Email o contraseña incorrectos."
                404 -> "Usuario no encontrado."
                else -> "Error del servidor. Intenta más tarde."
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(Exception("Error desconocido: ${e.message}"))
        }
    }

    /**
     * Obtener datos del usuario autenticado
     *
     * 🔴 CRÍTICO: Este método es obligatorio para la evaluación
     *
     * Llama al endpoint GET /auth/me con el token guardado
     *
     * @return Result con UserResponse o mensaje de error
     */
    suspend fun getMe(): Result<UserResponse> {
        return try {
            // Verificar que haya token guardado
            val token = preferencesManager.getAuthToken().first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No hay sesión activa."))
            }

            // Llamar al endpoint /auth/me
            // El token se agrega automáticamente mediante AuthInterceptor
            val response = api.getMe()

            // Retornar éxito
            Result.success(response)

        } catch (e: IOException) {
            Result.failure(Exception("Error de conexión. Verifica tu internet."))
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                401 -> "Sesión expirada. Inicia sesión nuevamente."
                else -> "Error del servidor. Intenta más tarde."
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(Exception("Error desconocido: ${e.message}"))
        }
    }

    /**
     * Cerrar sesión
     * Limpia el token y todos los datos de sesión
     */
    suspend fun logout() {
        preferencesManager.clearSession()
    }

    /**
     * Verificar si hay sesión activa
     *
     * @return true si hay token guardado, false si no
     */
    suspend fun hasActiveSession(): Boolean {
        val token = preferencesManager.getAuthToken().first()
        return !token.isNullOrEmpty()
    }
}
