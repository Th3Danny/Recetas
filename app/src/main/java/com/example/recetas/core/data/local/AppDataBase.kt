package com.example.recetas.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.recetas.core.data.local.dao.PendingRecetaOperationDao
import com.example.recetas.core.data.local.dao.RecetaDao
import com.example.recetas.core.data.local.entities.CategoryIdsConverter
import com.example.recetas.core.data.local.entities.IngredientsConverter
import com.example.recetas.core.data.local.entities.PendingRecetaOperationEntity
import com.example.recetas.core.data.local.entities.RecetaEntity

@Database(
    entities = [
        RecetaEntity::class,
        PendingRecetaOperationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(CategoryIdsConverter::class, IngredientsConverter::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun recetaDao(): RecetaDao
    abstract fun pendingRecetaOperationDao(): PendingRecetaOperationDao

    companion object {
        private const val DATABASE_NAME = "recetas_database"

        @Volatile
        private var INSTANCE: AppDataBase? = null

        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}