package com.duoc.artelab_grupo10.navigation

/**
 * Definición de rutas de navegación usando Sealed Class
 *
 * Cada objeto representa una pantalla de la aplicación.
 * Este patrón asegura type-safety en la navegación.
 */
sealed class Screen(val route: String) {

    /**
     * Pantalla de splash/bienvenida
     * - Verifica si hay token guardado
     * - Redirige a Login o Home según corresponda
     */
    object Splash : Screen("splash")

    /**
     * Pantalla de inicio de sesión
     * - Formulario con email y password
     * - Validaciones en tiempo real
     * - Redirige a Home si login exitoso
     */
    object Login : Screen("login")

    /**
     * Pantalla de registro
     * - Formulario con nombre, email, password, confirmación
     * - Checkbox de términos y condiciones
     * - Validaciones completas
     */
    object Register : Screen("register")

    /**
     * Pantalla principal/home
     * - Lista de obras de arte
     * - Navegación a perfil
     * - Barra de navegación inferior
     */
    object Home : Screen("home")

    /**
     * Pantalla de perfil de usuario
     * - Muestra datos del usuario autenticado
     * - Foto de perfil con cámara/galería
     * - Botón de cerrar sesión
     *
     * Ruta parametrizada: profile/{userId}
     * Para navegar: Profile.createRoute(userId)
     */
    object Profile : Screen("profile/{userId}") {
        /**
         * Crea una ruta con el userId específico
         * @param userId ID del usuario a mostrar
         * @return Ruta completa "profile/123"
         */
        fun createRoute(userId: Int): String = "profile/$userId"
    }

    /**
     * Lista de todas las rutas disponibles
     * Útil para debugging y testing
     */
    companion object {
        val allRoutes = listOf(
            Splash.route,
            Login.route,
            Register.route,
            Home.route,
            Profile.route
        )
    }
}
