package com.duoc.artelab_grupo10.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Modelo de respuesta para login exitoso
 * Endpoint: POST /auth/login
 */
data class LoginResponse(
    @SerializedName("authToken")
    val authToken: String,

    @SerializedName("user")
    val user: UserData
)
