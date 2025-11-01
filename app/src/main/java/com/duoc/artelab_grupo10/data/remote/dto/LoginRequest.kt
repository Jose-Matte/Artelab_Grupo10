package com.duoc.artelab_grupo10.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Modelo de solicitud para inicio de sesión
 * Endpoint: POST /auth/login
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)
