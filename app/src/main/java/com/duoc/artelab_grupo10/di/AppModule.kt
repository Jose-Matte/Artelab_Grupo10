package com.duoc.artelab_grupo10.di

import android.content.Context
import com.duoc.artelab_grupo10.data.local.PreferencesManager
import com.duoc.artelab_grupo10.data.local.database.AppDatabase
import com.duoc.artelab_grupo10.data.remote.RetrofitClient
import com.duoc.artelab_grupo10.data.repository.AuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Módulo de inyección de dependencias manual
 *
 * Proporciona instancias singleton de:
 * - Database
 * - PreferencesManager
 * - RetrofitClient/API
 * - Repositorios
 *
 * NOTA: En producción se recomienda usar Hilt o Koin
 * Esta es una implementación simple para el proyecto educativo
 */
object AppModule {

    // Instancias singleton
    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var preferencesManager: PreferencesManager? = null

    @Volatile
    private var authRepository: AuthRepository? = null

    /**
     * Obtener instancia de AppDatabase
     */
    fun provideDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = AppDatabase.getDatabase(context)
            database = instance
            instance
        }
    }

    /**
     * Obtener instancia de PreferencesManager
     */
    fun providePreferencesManager(context: Context): PreferencesManager {
        return preferencesManager ?: synchronized(this) {
            val instance = PreferencesManager(context)
            preferencesManager = instance
            instance
        }
    }

    /**
     * Obtener instancia de AuthRepository
     */
    fun provideAuthRepository(context: Context): AuthRepository {
        return authRepository ?: synchronized(this) {
            val prefs = providePreferencesManager(context)

            // Crear API con token provider
            val api = RetrofitClient.create {
                // Token provider: obtiene token de DataStore
                runBlocking {
                    prefs.getAuthToken().first()
                }
            }

            val instance = AuthRepository(api, prefs)
            authRepository = instance
            instance
        }
    }

    /**
     * Limpiar todas las instancias
     * Útil para testing o logout completo
     */
    fun clear() {
        database = null
        preferencesManager = null
        authRepository = null
    }
}
