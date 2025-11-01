package com.duoc.artelab_grupo10.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.duoc.artelab_grupo10.di.AppModule

/**
 * Factory para crear ViewModels con dependencias
 *
 * Este factory permite inyectar dependencias (repositorios, DAOs)
 * en los ViewModels al momento de su creación
 */
class ViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            // LoginViewModel
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                val authRepository = AppModule.provideAuthRepository(context)
                LoginViewModel(authRepository) as T
            }

            // RegisterViewModel
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                val authRepository = AppModule.provideAuthRepository(context)
                RegisterViewModel(authRepository) as T
            }

            // SplashViewModel
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> {
                val authRepository = AppModule.provideAuthRepository(context)
                SplashViewModel(authRepository) as T
            }

            // ProfileViewModel
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                val authRepository = AppModule.provideAuthRepository(context)
                val database = AppModule.provideDatabase(context)
                val usuarioDao = database.usuarioDao()
                ProfileViewModel(authRepository, usuarioDao) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
