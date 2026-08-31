package com.ruan.apexlift.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ruan.apexlift.data.local.dao.ExercicioDao
import com.ruan.apexlift.data.local.dao.PesoDao
import com.ruan.apexlift.data.local.dao.RotinaDao
import com.ruan.apexlift.data.local.dao.SessaoPendenteDao
import com.ruan.apexlift.data.local.entity.ExercicioEntity
import com.ruan.apexlift.data.local.entity.FichaExercicioEntity
import com.ruan.apexlift.data.local.entity.PesoEntity
import com.ruan.apexlift.data.local.entity.RotinaEntity
import com.ruan.apexlift.data.local.entity.SeriePendenteEntity
import com.ruan.apexlift.data.local.entity.SessaoPendenteEntity

@Database(
    entities = [
        ExercicioEntity::class,
        RotinaEntity::class,
        FichaExercicioEntity::class,
        SessaoPendenteEntity::class,
        SeriePendenteEntity::class,
        PesoEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exercicioDao(): ExercicioDao
    abstract fun rotinaDao(): RotinaDao

    abstract fun sessaoPendenteDao(): SessaoPendenteDao

    abstract fun pesoDao(): PesoDao

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