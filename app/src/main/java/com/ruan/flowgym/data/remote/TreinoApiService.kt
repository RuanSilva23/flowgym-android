package com.ruan.flowgym.data.remote

import com.ruan.flowgym.data.model.ExercicioResponseDTO
import com.ruan.flowgym.data.model.NovaSerieRequestDTO
import com.ruan.flowgym.data.model.SerieTreinoResponseDTO
import com.ruan.flowgym.data.model.SessaoTreinoResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface TreinoApiService {

    // === SESSÃO DE TREINO (SessaoTreinoController) ===

    @POST("treinos/iniciar/{idUsuario}")
    suspend fun iniciarSessao(
        @Path("idUsuario") idUsuario: Long
    ): Response<SessaoTreinoResponseDTO>

    @POST("treinos/salvar-serie")
    suspend fun registrarSerie(
        @Body request: NovaSerieRequestDTO
    ): Response<SerieTreinoResponseDTO>

    @PUT("treinos/finalizar/{idSessao}")
    suspend fun finalizarSessao(
        @Path("idSessao") idSessao: Long
    ): Response<SessaoTreinoResponseDTO>

    @GET("treinos/historico/{idUsuario}")
    suspend fun buscarHistoricoSessoes(
        @Path("idUsuario") idUsuario: Long
    ): Response<List<SessaoTreinoResponseDTO>>


    // === EXERCÍCIOS (ExercicioController) ===

    @GET("exercicios/listar")
    suspend fun listarExercicios(
        @Query("idUsuario") idUsuario: Long
    ): Response<List<ExercicioResponseDTO>>

    @GET("exercicios/listar/{grupoMuscular}")
    suspend fun listarPorGrupoMuscular(
        @Path("grupoMuscular") grupoMuscular: String
    ): Response<List<ExercicioResponseDTO>>

    @GET("treinos/historico/{idUsuario}/exercicio/{idExercicio}")
    suspend fun buscarHistoricoExercicio(
        @Path("idUsuario") idUsuario: Long,
        @Path("idExercicio") idExercicio: Long
    ): Response<List<SerieTreinoResponseDTO>>
}