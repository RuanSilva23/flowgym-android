package com.ruan.flowgym.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ruan.flowgym.data.local.dao.ExercicioDao
import com.ruan.flowgym.data.local.entity.ExercicioEntity

@Database(entities = [ExercicioEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exercicioDao(): ExercicioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flowgym_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}