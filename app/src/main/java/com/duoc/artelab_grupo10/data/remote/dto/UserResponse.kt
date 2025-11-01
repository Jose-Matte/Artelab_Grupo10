package com.duoc.artelab_grupo10.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Modelo de respuesta para obtener usuario autenticado
 * Endpoint: GET /auth/me (OBLIGATORIO)
 *
 * CRÍTICO: Este endpoint es obligatorio para la evaluación
 */
data class UserResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("created_at")
    val createdAt: Long? = null,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null
)
