package com.ruan.flowgym.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ruan.flowgym.data.local.dao.ExercicioDao
import com.ruan.flowgym.data.local.dao.RotinaDao
import com.ruan.flowgym.data.local.dao.SessaoPendenteDao
import com.ruan.flowgym.data.local.entity.ExercicioEntity
import com.ruan.flowgym.data.local.entity.FichaExercicioEntity
import com.ruan.flowgym.data.local.entity.RotinaEntity
import com.ruan.flowgym.data.local.entity.SeriePendenteEntity
import com.ruan.flowgym.data.local.entity.SessaoPendenteEntity

@Database(
    entities = [
        ExercicioEntity::class,
        RotinaEntity::class,
        FichaExercicioEntity::class,
        SessaoPendenteEntity::class,
        SeriePendenteEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exercicioDao(): ExercicioDao
    abstract fun rotinaDao(): RotinaDao

    abstract fun sessaoPendenteDao(): SessaoPendenteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flowgym_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}