package com.ruan.apexlift.data.repository

import com.ruan.apexlift.data.local.model.RotinaComExercicios
import com.ruan.apexlift.data.model.CriarFichaRequestDTO
import com.ruan.apexlift.data.model.ItemFichaRequestDTO
import kotlinx.coroutines.flow.Flow

interface FichaRepository {
    // Retorna um Flow do ROOM para atualizar a UI em tempo real
    fun getFichasDoUsuario(usuarioId: Long): Flow<List<RotinaComExercicios>>

    // Vai na API do Spring Boot, busca as fichas e atualiza o banco local
    suspend fun sincronizarFichas(usuarioId: Long)

    // Envia a nova ficha para a API e persiste o resultado localmente
    suspend fun criarFicha(dto: CriarFichaRequestDTO): Result<Unit>

    suspend fun editarFicha(
        idFicha: Long,
        usuarioId: Long,
        nome: String,
        descricao: String?,
        itens: List<ItemFichaRequestDTO>

    ): Result<Unit>
}