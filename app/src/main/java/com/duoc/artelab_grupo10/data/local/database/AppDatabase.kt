package com.duoc.artelab_grupo10.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.duoc.artelab_grupo10.data.local.dao.ObraDao
import com.duoc.artelab_grupo10.data.local.dao.UsuarioDao
import com.duoc.artelab_grupo10.data.local.entities.Obra
import com.duoc.artelab_grupo10.data.local.entities.Usuario

/**
 * Base de datos principal de la aplicación ArteLab
 * Contiene las entidades Usuario y Obra
 */
@Database(
    entities = [Usuario::class, Obra::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun obraDao(): ObraDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la instancia singleton de la base de datos
         * Thread-safe con sincronización
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "artelab_database"
                )
                    // NOTA: fallbackToDestructiveMigration solo para desarrollo
                    // En producción usar migraciones apropiadas
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Limpia la instancia de la base de datos
         * Útil para testing
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
