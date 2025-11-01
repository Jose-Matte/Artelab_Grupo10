package com.duoc.artelab_grupo10.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Modelo de respuesta para registro exitoso
 * Endpoint: POST /auth/signup
 */
data class SignupResponse(
    @SerializedName("authToken")
    val authToken: String,

    @SerializedName("user")
    val user: UserData
)

/**
 * Datos del usuario incluidos en la respuesta
 */
data class UserData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("created_at")
    val createdAt: Long? = null
)
