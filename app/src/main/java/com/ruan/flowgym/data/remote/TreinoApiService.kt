package com.ruan.flowgym.data.remote

import com.ruan.flowgym.data.model.NovaSerieRequestDTO
import com.ruan.flowgym.data.model.SerieTreinoRequestDTO
import com.ruan.flowgym.data.model.SerieTreinoResponseDTO
import com.ruan.flowgym.data.model.SessaoTreinoResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface TreinoApiService {

    // POST /treinos/iniciar/{idUsuario}
    @POST("treinos/iniciar/{idUsuario}")
    suspend fun iniciarTreino(
        @Path("idUsuario") idUsuario: Long
    ): Response<SessaoTreinoResponseDTO>

    // POST /treinos/salvar-serie (envia o JSON no corpo da requisição)
    @POST("treinos/salvar-serie")
    suspend fun salvarSerie(
        @Body dto: SerieTreinoRequestDTO
    ): Response<SerieTreinoResponseDTO>

    // PUT /treinos/finalizar/{idSessao}
    @PUT("treinos/finalizar/{idSessao}")
    suspend fun finalizarTreino(
        @Path("idSessao") idSessao: Long
    ): Response<SessaoTreinoResponseDTO>

    // GET /treinos/historico/{idUsuario}
    @GET("treinos/historico/{idUsuario}")
    suspend fun buscarHistoricoUsuario(
        @Path("idUsuario") idUsuario: Long
    ): Response<List<SessaoTreinoResponseDTO>>

    // GET /treinos/historico/{idUsuario}/exercicio/{idExercicio}
    @GET("treinos/historico/{idUsuario}/exercicio/{idExercicio}")
    suspend fun buscarHistoricoExercicio(
        @Path("idUsuario") idUsuario: Long,
        @Path("idExercicio") idExercicio: Long
    ): Response<List<SerieTreinoResponseDTO>>
}