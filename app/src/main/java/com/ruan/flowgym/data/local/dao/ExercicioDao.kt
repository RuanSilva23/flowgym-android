package com.ruan.flowgym.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ruan.flowgym.data.local.entity.ExercicioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExercicioDao {

    // Retorna um Flow contínuo: a interface atualiza sozinha quando o banco muda
    @Query("SELECT * FROM exercicios ORDER BY nome ASC")
    fun listarTodos(): Flow<List<ExercicioEntity>>

    // Insere ou atualiza os exercícios vindos da API
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarTodos(exercicios: List<ExercicioEntity>)

    @Query("DELETE FROM exercicios")
    suspend fun limparTabela()
}