package com.ruan.flowgym.di

import android.content.Context
import androidx.room.Room
import com.ruan.flowgym.data.local.AppDatabase
import com.ruan.flowgym.data.local.SessionManager
import com.ruan.flowgym.data.local.dao.ExercicioDao
import com.ruan.flowgym.data.local.dao.PesoDao
import com.ruan.flowgym.data.local.dao.RotinaDao
import com.ruan.flowgym.data.local.dao.SessaoPendenteDao
import com.ruan.flowgym.data.remote.AuthInterceptor
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
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "flowgym_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(sessionManager: SessionManager): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Injeta o SessionManager diretamente no Interceptor para ler o Token JWT atualizado
        val authInterceptor = AuthInterceptor(sessionManager)

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideTreinoApiService(okHttpClient: OkHttpClient): TreinoApiService {
        return Retrofit.Builder()
            .baseUrl("http://192.168.31.161:8080/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TreinoApiService::class.java)
    }

    @Provides
    fun provideExercicioDao(database: AppDatabase): ExercicioDao = database.exercicioDao()

    @Provides
    fun provideRotinaDao(database: AppDatabase): RotinaDao = database.rotinaDao()

    @Provides
    fun provideSessaoPendenteDao(database: AppDatabase): SessaoPendenteDao = database.sessaoPendenteDao()

    @Provides
    @Singleton
    fun providePesoDao(database: AppDatabase): PesoDao = database.pesoDao()

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