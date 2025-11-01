package com.duoc.artelab_grupo10.data.local.dao

import androidx.room.*
import com.duoc.artelab_grupo10.data.local.entities.Obra
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para operaciones CRUD de Obra
 */
@Dao
interface ObraDao {

    // READ - Obtener todas las obras (Flow para observar cambios)
    @Query("SELECT * FROM obras ORDER BY fecha_creacion DESC")
    fun getAllObras(): Flow<List<Obra>>

    // READ - Obtener obras por usuario
    @Query("SELECT * FROM obras WHERE usuario_id = :usuarioId ORDER BY fecha_creacion DESC")
    fun getObrasByUsuario(usuarioId: Int): Flow<List<Obra>>

    // READ - Obtener obra por ID
    @Query("SELECT * FROM obras WHERE id = :id")
    suspend fun getObraById(id: Int): Obra?

    // READ - Buscar obras por título
    @Query("SELECT * FROM obras WHERE titulo LIKE '%' || :query || '%' ORDER BY fecha_creacion DESC")
    fun searchObrasByTitulo(query: String): Flow<List<Obra>>

    // CREATE - Insertar obra
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObra(obra: Obra): Long

    // UPDATE - Actualizar obra
    @Update
    suspend fun updateObra(obra: Obra)

    // UPDATE - Incrementar likes
    @Query("UPDATE obras SET likes = likes + 1 WHERE id = :id")
    suspend fun incrementLikes(id: Int)

    // DELETE - Eliminar obra
    @Delete
    suspend fun deleteObra(obra: Obra)

    // DELETE - Eliminar por ID
    @Query("DELETE FROM obras WHERE id = :id")
    suspend fun deleteObraById(id: Int)

    // DELETE - Eliminar todas las obras de un usuario
    @Query("DELETE FROM obras WHERE usuario_id = :usuarioId")
    suspend fun deleteObrasByUsuario(usuarioId: Int)

    // DELETE - Eliminar todas las obras
    @Query("DELETE FROM obras")
    suspend fun deleteAllObras()

    // COUNT - Contar obras por usuario
    @Query("SELECT COUNT(*) FROM obras WHERE usuario_id = :usuarioId")
    suspend fun countObrasByUsuario(usuarioId: Int): Int
}
