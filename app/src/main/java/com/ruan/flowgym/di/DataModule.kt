package com.ruan.flowgym.di

import android.content.Context
import com.ruan.flowgym.data.local.AppDatabase
import com.ruan.flowgym.data.local.dao.ExercicioDao
import com.ruan.flowgym.data.local.dao.RotinaDao
import com.ruan.flowgym.data.remote.RetrofitClient
import com.ruan.flowgym.data.remote.TreinoApiService
import com.ruan.flowgym.data.repository.ExercicioRepository
import com.ruan.flowgym.data.repository.FichaRepository
import com.ruan.flowgym.data.repository.FichaRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideExercicioDao(database: AppDatabase): ExercicioDao = database.exercicioDao()

    @Provides
    fun provideRotinaDao(database: AppDatabase): RotinaDao = database.rotinaDao()

    @Provides
    @Singleton
    fun provideTreinoApiService(): TreinoApiService = RetrofitClient.apiService

    @Provides
    @Singleton
    fun provideExercicioRepository(
        dao: ExercicioDao,
        api: TreinoApiService
    ): ExercicioRepository = ExercicioRepository(dao, api)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFichaRepository(
        impl: FichaRepositoryImpl
    ): FichaRepository
}