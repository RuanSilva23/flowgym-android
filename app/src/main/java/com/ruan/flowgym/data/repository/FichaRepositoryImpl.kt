package com.ruan.flowgym.data.repository

import com.ruan.flowgym.data.local.dao.RotinaDao
import com.ruan.flowgym.data.local.model.RotinaComExercicios
import com.ruan.flowgym.data.mapper.toEntity
import com.ruan.flowgym.data.model.CriarFichaRequestDTO
import com.ruan.flowgym.data.remote.TreinoApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FichaRepositoryImpl @Inject constructor(
    private val api: TreinoApiService,
    private val rotinaDao: RotinaDao
) : FichaRepository {

    // A UI consome diretamente do Room. Velocidade máxima!
    override fun getFichasDoUsuario(usuarioId: Long): Flow<List<RotinaComExercicios>> {
        return rotinaDao.listarRotinasPorUsuario(usuarioId)
    }

    // Sincronização em segundo plano
    override suspend fun sincronizarFichas(usuarioId: Long) {
        try {
            val response = api.listarFichasPorUsuario(usuarioId)
            if (response.isSuccessful) {
                response.body()?.let { listaRotinasDto ->
                    listaRotinasDto.forEach { rotinaDto ->
                        // Converte usando o Mapper que criamos
                        val rotinaEntity = rotinaDto.toEntity(usuarioId)
                        val itensEntities = rotinaDto.exercicios.map { itemDto ->
                            itemDto.toEntity(rotinaId = rotinaDto.id)
                        }

                        // Salva no SQLite local
                        rotinaDao.salvarFichaCompleta(rotinaEntity, itensEntities)
                    }
                }
            }
        } catch (e: Exception) {
            // Sem internet? Sem problemas. O app engole o erro de rede
            // e continua exibindo os dados salvos anteriormente no Room.
        }
    }

    // Criação de nova ficha (Escrita)
    override suspend fun criarFicha(dto: CriarFichaRequestDTO): Result<Unit> {
        return try {
            val response = api.montarFicha(dto)
            if (response.isSuccessful && response.body() != null) {
                val rotinaDto = response.body()!!

                // Converte a resposta oficial do Spring Boot para o Room
                val rotinaEntity = rotinaDto.toEntity(dto.idUsuario)
                val itensEntities = rotinaDto.exercicios.map { itemDto ->
                    itemDto.toEntity(rotinaId = rotinaDto.id)
                }

                // Salva localmente para atualizar a tela instantaneamente via Flow
                rotinaDao.salvarFichaCompleta(rotinaEntity, itensEntities)

                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro ao criar ficha no servidor"))
            }
        } catch (e: Exception) {
            // Falha de conexão ao tentar enviar a nova ficha
            Result.failure(e)
        }
    }
}