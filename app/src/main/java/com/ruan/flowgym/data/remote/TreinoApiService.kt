package com.ruan.flowgym.data.remote

import com.ruan.flowgym.data.model.CriarFichaRequestDTO
import com.ruan.flowgym.data.model.ExercicioResponseDTO
import com.ruan.flowgym.data.model.NovaSerieRequestDTO
import com.ruan.flowgym.data.model.RotinaResponseDTO
import com.ruan.flowgym.data.model.SerieTreinoResponseDTO
import com.ruan.flowgym.data.model.SessaoTreinoResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface TreinoApiService {

    // === SESSÃO DE TREINO (SessaoTreinoController) ===

    @POST("treinos/iniciar/{idUsuario}")
    suspend fun iniciarSessao(
        @Path("idUsuario") idUsuario: Long,
        @Query("idRotina") idRotina: Long? = null
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

    @DELETE("treinos/{idSessao}")
    suspend fun deletarSessao(
        @Path("idSessao") idSessao: Long
    ): Response<Void>

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

    // === FICHAS (FichasController) ===

    @POST("api/fichas")
    suspend fun montarFicha(
        @Body dto: CriarFichaRequestDTO
    ): Response<RotinaResponseDTO>

    @GET("api/fichas/listar/{idUsuario}")
    suspend fun listarFichasPorUsuario(
        @Path("idUsuario") idUsuario: Long
    ): Response<List<RotinaResponseDTO>>

    @GET("api/fichas/{idRotina}")
    suspend fun buscarRotinaPorId(
        @Path("idRotina") idRotina: Long
    ): Response<RotinaResponseDTO>

    @PUT("api/fichas/{id}")
    suspend fun editarFicha(
        @Path("id") idFicha: Long,
        @Body request: CriarFichaRequestDTO
    ): Response<RotinaResponseDTO>

    @DELETE("api/fichas/{id}")
    suspend fun deletarFicha(
        @Path("id") idFicha: Long
    ): Response<Void>
}