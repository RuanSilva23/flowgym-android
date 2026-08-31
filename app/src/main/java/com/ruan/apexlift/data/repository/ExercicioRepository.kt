package com.ruan.apexlift.data.repository

import com.ruan.apexlift.data.local.dao.ExercicioDao
import com.ruan.apexlift.data.local.entity.ExercicioEntity
import com.ruan.apexlift.data.remote.TreinoApiService
import kotlinx.coroutines.flow.Flow

class ExercicioRepository(
    private val dao: ExercicioDao,
    private val api: TreinoApiService
) {
    // A UI consome APENAS este Flow vindo direto do banco local (Room)
    val todosExercicios: Flow<List<ExercicioEntity>> = dao.listarTodos()

    // Sincroniza em segundo plano sem travar a leitura do Room
    suspend fun sincronizarExercicios(idUsuario: Long) {
        try {
            val response = api.listarExercicios(idUsuario)
            if (response.isSuccessful && response.body() != null) {
                val entidades = response.body()!!.map { dto ->
                    ExercicioEntity(
                        id = dto.id,
                        nome = dto.nome,
                        grupoMuscular = dto.grupoMuscular
                    )
                }
                // Salva no banco local. O Flow 'todosExercicios' vai emitir a lista atualizada automaticamente
                dao.salvarTodos(entidades)
            }
        } catch (e: Exception) {
            // Falha de rede silenciosa: o Room continua entregando os dados já salvos localmente
        }
    }
}