package com.duoc.artelab_grupo10.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Extension property para DataStore
 * Crea una instancia única de DataStore para preferencias
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "artelab_preferences")

/**
 * Keys para las preferencias almacenadas
 */
object PreferencesKeys {
    val AUTH_TOKEN = stringPreferencesKey("auth_token")
    val USER_ID = intPreferencesKey("user_id")
    val USER_EMAIL = stringPreferencesKey("user_email")
    val USER_NAME = stringPreferencesKey("user_name")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val THEME_MODE = stringPreferencesKey("theme_mode") // "light", "dark", "system"
    val LAST_SYNC = longPreferencesKey("last_sync")
}

/**
 * Manager para gestionar las preferencias de la aplicación usando DataStore
 * Reemplazo moderno de SharedPreferences
 */
class PreferencesManager(private val context: Context) {

    private val dataStore: DataStore<Preferences> = context.dataStore

    // ==================== TOKEN DE AUTENTICACIÓN ====================

    /**
     * Guarda el token de autenticación
     * CRÍTICO: Necesario para el interceptor de Retrofit
     */
    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] = token
        }
    }

    /**
     * Obtiene el token de autenticación como Flow
     */
    fun getAuthToken(): Flow<String?> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.AUTH_TOKEN]
            }
    }

    /**
     * Obtiene el token de forma síncrona (para interceptor)
     */
    suspend fun getAuthTokenSync(): String? {
        val preferences = dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        var token: String? = null
        preferences.collect { prefs ->
            token = prefs[PreferencesKeys.AUTH_TOKEN]
        }
        return token
    }

    // ==================== SESIÓN DE USUARIO ====================

    /**
     * Guarda la sesión completa del usuario
     * Se llama después de login/registro exitoso
     */
    suspend fun saveUserSession(
        userId: Int,
        email: String,
        name: String,
        token: String
    ) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
            preferences[PreferencesKeys.USER_EMAIL] = email
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.AUTH_TOKEN] = token
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
        }
    }

    /**
     * Obtiene el ID del usuario logueado
     */
    fun getUserId(): Flow<Int?> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.USER_ID]
            }
    }

    /**
     * Obtiene el email del usuario
     */
    fun getUserEmail(): Flow<String?> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.USER_EMAIL]
            }
    }

    /**
     * Obtiene el nombre del usuario
     */
    fun getUserName(): Flow<String?> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.USER_NAME]
            }
    }

    /**
     * Verifica si el usuario está logueado
     */
    fun isLoggedIn(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
            }
    }

    /**
     * Cierra la sesión del usuario
     * Limpia todas las preferencias relacionadas con la sesión
     */
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.AUTH_TOKEN)
            preferences.remove(PreferencesKeys.USER_ID)
            preferences.remove(PreferencesKeys.USER_EMAIL)
            preferences.remove(PreferencesKeys.USER_NAME)
            preferences[PreferencesKeys.IS_LOGGED_IN] = false
        }
    }

    // ==================== TEMA ====================

    /**
     * Guarda el modo de tema preferido
     */
    suspend fun saveThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    /**
     * Obtiene el modo de tema
     */
    fun getThemeMode(): Flow<String> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.THEME_MODE] ?: "system"
            }
    }

    // ==================== SINCRONIZACIÓN ====================

    /**
     * Guarda la última fecha de sincronización
     */
    suspend fun saveLastSync(timestamp: Long = System.currentTimeMillis()) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC] = timestamp
        }
    }

    /**
     * Obtiene la última fecha de sincronización
     */
    fun getLastSync(): Flow<Long?> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.LAST_SYNC]
            }
    }

    // ==================== UTILIDADES ====================

    /**
     * Limpia TODAS las preferencias
     * Útil para testing o reset completo
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
