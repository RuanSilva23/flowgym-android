package com.ruan.flowgym.data.remote

import com.ruan.flowgym.data.model.SessaoTreinoResponseDTO
import com.ruan.flowgym.data.model.SerieTreinoResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface TreinoApiService {
    @POST("treinos/iniciar")
    suspend fun iniciarTreino(
        @Query("idUsuario") idUsuario: Long
    ): Response<SessaoTreinoResponseDTO>

    @POST("treinos/series")
    suspend fun registrarSerie(
        @Query("idSessao") idSessao: Long,
        @Query("idExercicio") idExercicio: Long,
        @Query("carga") carga: Double,
        @Query("repeticoes") repeticoes: Int
    ): Response<SerieTreinoResponseDTO>

    @PUT("treinos/finalizar/{id}")
    suspend fun finalizarTreino(
        @Path("id") idSessao: Long
    ): Response<SessaoTreinoResponseDTO>

    @GET("treinos/historico/{idUsuario}")
    suspend fun buscarHistoricoUsuario(
        @Path("idUsuario") idUsuario: Long
    ): Response<List<SessaoTreinoResponseDTO>>
}