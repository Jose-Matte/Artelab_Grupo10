package com.duoc.artelab_grupo10.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Usuario para Room Database
 * Almacena información del usuario incluyendo URI de imagen de perfil (OBLIGATORIO)
 */
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "email")
    val email: String,

    /**
     * URI de la imagen de perfil almacenada localmente
     * CRÍTICO: Guardar URI de imagen para persistencia offline
     */
    @ColumnInfo(name = "avatar_uri")
    val avatarUri: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
