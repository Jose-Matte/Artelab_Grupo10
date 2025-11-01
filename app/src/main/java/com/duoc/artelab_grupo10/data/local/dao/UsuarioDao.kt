package com.duoc.artelab_grupo10.data.local.dao

import androidx.room.*
import com.duoc.artelab_grupo10.data.local.entities.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para operaciones CRUD de Usuario
 */
@Dao
interface UsuarioDao {

    // READ - Obtener todos los usuarios (Flow para observar cambios)
    @Query("SELECT * FROM usuarios ORDER BY created_at DESC")
    fun getAllUsuarios(): Flow<List<Usuario>>

    // READ - Obtener usuario por ID
    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun getUsuarioById(id: Int): Usuario?

    // READ - Buscar por email
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun getUsuarioByEmail(email: String): Usuario?

    // CREATE - Insertar usuario
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: Usuario): Long

    // UPDATE - Actualizar usuario completo
    @Update
    suspend fun updateUsuario(usuario: Usuario)

    // UPDATE - Actualizar solo la imagen de perfil (CRÍTICO para foto de perfil)
    @Query("UPDATE usuarios SET avatar_uri = :avatarUri WHERE id = :id")
    suspend fun updateAvatarUri(id: Int, avatarUri: String?)

    // DELETE - Eliminar usuario
    @Delete
    suspend fun deleteUsuario(usuario: Usuario)

    // DELETE - Eliminar por ID
    @Query("DELETE FROM usuarios WHERE id = :id")
    suspend fun deleteUsuarioById(id: Int)

    // DELETE - Eliminar todos
    @Query("DELETE FROM usuarios")
    suspend fun deleteAllUsuarios()
}
