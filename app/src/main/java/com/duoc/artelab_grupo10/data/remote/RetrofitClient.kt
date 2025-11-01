package com.duoc.artelab_grupo10.data.remote

import com.duoc.artelab_grupo10.data.remote.api.ArtelabApi
import com.duoc.artelab_grupo10.data.remote.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit singleton para comunicación con API
 *
 * Configuración:
 * - Base URL: https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW/
 * - Timeout: 30 segundos
 * - Logging: Nivel BODY (en desarrollo)
 * - Auth Interceptor: Agrega token automáticamente
 */
object RetrofitClient {

    /**
     * Crea una instancia de ArtelabApi con configuración completa
     *
     * @param tokenProvider Función que proporciona el token de autenticación
     * @return Instancia configurada de ArtelabApi
     */
    fun create(tokenProvider: () -> String?): ArtelabApi {
        // Logging interceptor para debugging
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // BODY muestra request y response completos
            // En producción cambiar a NONE o BASIC
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Auth interceptor para agregar token
        val authInterceptor = AuthInterceptor(tokenProvider)

        // Cliente OkHttp con interceptors y timeouts
        val okHttpClient = OkHttpClient.Builder()
            // Agregar interceptors (orden importa)
            .addInterceptor(loggingInterceptor)  // Primero logging
            .addInterceptor(authInterceptor)      // Luego auth

            // Timeouts (30 segundos)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

            // Configuración adicional
            .retryOnConnectionFailure(true)  // Reintentar si falla conexión

            .build()

        // Retrofit con Gson converter
        return Retrofit.Builder()
            .baseUrl(ArtelabApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ArtelabApi::class.java)
    }

    /**
     * Crea una instancia sin autenticación
     * Útil para endpoints públicos (signup, login)
     */
    fun createWithoutAuth(): ArtelabApi {
        return create { null }
    }
}
