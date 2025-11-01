package com.duoc.artelab_grupo10.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Modelo de solicitud para registro de nuevo usuario
 * Endpoint: POST /auth/signup
 */
data class SignupRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("name")
    val name: String
)
