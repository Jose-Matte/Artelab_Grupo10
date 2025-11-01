package com.duoc.artelab_grupo10.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor para agregar token de autenticación a las peticiones HTTP
 *
 * CRÍTICO: Este interceptor agrega automáticamente el header
 * "Authorization: Bearer <token>" a todas las peticiones que requieran autenticación
 *
 * @param tokenProvider Función lambda que proporciona el token actual
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Obtener el token del provider
        val token = tokenProvider()

        // Si hay token, agregar header Authorization
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            // Si no hay token, enviar request sin modificar
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
