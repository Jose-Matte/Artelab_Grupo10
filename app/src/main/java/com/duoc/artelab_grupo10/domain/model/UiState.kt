package com.duoc.artelab_grupo10.domain.model

/**
 * Sealed class para representar los diferentes estados de UI
 *
 * Este patrón permite manejar de forma type-safe todos los estados
 * posibles de una operación asíncrona:
 * - Idle: Estado inicial, sin operaciones
 * - Loading: Operación en progreso
 * - Success: Operación exitosa con datos
 * - Error: Operación falló con mensaje de error
 * - Empty: Operación exitosa pero sin datos
 *
 * CRÍTICO: Este es el patrón obligatorio para IE3.4 (16% de la nota)
 *
 * Uso típico:
 * ```kotlin
 * when (uiState) {
 *     is UiState.Idle -> { /* Mostrar UI inicial */ }
 *     is UiState.Loading -> { /* Mostrar progress */ }
 *     is UiState.Success -> { /* Mostrar datos: uiState.data */ }
 *     is UiState.Error -> { /* Mostrar error: uiState.message */ }
 *     is UiState.Empty -> { /* Mostrar estado vacío */ }
 * }
 * ```
 */
sealed class UiState<out T> {
    /**
     * Estado inicial - Sin operaciones en curso
     * Se usa al inicializar un ViewModel
     */
    object Idle : UiState<Nothing>()

    /**
     * Estado de carga - Operación en progreso
     * Se muestra CircularProgressIndicator
     */
    object Loading : UiState<Nothing>()

    /**
     * Estado de éxito - Operación completada con datos
     * @param data Los datos resultantes de la operación
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Estado de error - Operación falló
     * @param message Mensaje de error para mostrar al usuario
     */
    data class Error(val message: String) : UiState<Nothing>()

    /**
     * Estado vacío - Operación exitosa pero sin datos
     * Se usa cuando una lista está vacía
     */
    object Empty : UiState<Nothing>()
}

/**
 * Extension function para verificar si está en loading
 */
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading

/**
 * Extension function para verificar si es error
 */
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error

/**
 * Extension function para verificar si es success
 */
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success

/**
 * Extension function para obtener datos si es success
 */
fun <T> UiState<T>.getDataOrNull(): T? {
    return if (this is UiState.Success) this.data else null
}

/**
 * Extension function para obtener mensaje de error si es error
 */
fun <T> UiState<T>.getErrorOrNull(): String? {
    return if (this is UiState.Error) this.message else null
}
