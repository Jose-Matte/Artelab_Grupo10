package com.duoc.artelab_grupo10.data.remote.api

import com.duoc.artelab_grupo10.data.remote.dto.*
import retrofit2.http.*

/**
 * Interfaz de API REST para ArteLab
 * Base URL: https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW/
 *
 * Todos los métodos son suspend para usar con Coroutines
 */
interface ArtelabApi {

    companion object {
        const val BASE_URL = "https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW/"
    }

    /**
     * Registrar nuevo usuario
     * POST /auth/signup
     *
     * @param request Datos del usuario (email, password, name)
     * @return Token de autenticación y datos del usuario
     */
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): SignupResponse

    /**
     * Iniciar sesión
     * POST /auth/login
     *
     * @param request Credenciales (email, password)
     * @return Token de autenticación y datos del usuario
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    /**
     * Obtener usuario autenticado
     * GET /auth/me
     *
     * 🔴 OBLIGATORIO: Este endpoint es crítico para la evaluación
     *
     * Requiere token en header: Authorization: Bearer <token>
     * El token se agrega automáticamente mediante AuthInterceptor
     *
     * @return Datos del usuario autenticado
     */
    @GET("auth/me")
    suspend fun getMe(): UserResponse
}
