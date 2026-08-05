package com.ruan.flowgym.data.repository

import com.ruan.flowgym.data.local.model.RotinaComExercicios
import com.ruan.flowgym.data.model.CriarFichaRequestDTO
import kotlinx.coroutines.flow.Flow

interface FichaRepository {
    // Retorna um Flow do ROOM para atualizar a UI em tempo real
    fun getFichasDoUsuario(usuarioId: Long): Flow<List<RotinaComExercicios>>

    // Vai na API do Spring Boot, busca as fichas e atualiza o banco local
    suspend fun sincronizarFichas(usuarioId: Long)

    // Envia a nova ficha para a API e persiste o resultado localmente
    suspend fun criarFicha(dto: CriarFichaRequestDTO): Result<Unit>
}