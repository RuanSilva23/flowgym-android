package com.ruan.flowgym.data.local.dao

import androidx.room.*
import com.ruan.flowgym.data.local.entity.FichaExercicioEntity
import com.ruan.flowgym.data.local.entity.RotinaEntity
import com.ruan.flowgym.data.local.model.RotinaComExercicios
import kotlinx.coroutines.flow.Flow

@Dao
interface RotinaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirRotina(rotina: RotinaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirItensFicha(itens: List<FichaExercicioEntity>)

    // Salva a rotina e seus itens de uma só vez
    @Transaction
    suspend fun salvarFichaCompleta(rotina: RotinaEntity, itens: List<FichaExercicioEntity>) {
        inserirRotina(rotina)
        inserirItensFicha(itens)
    }

    @Transaction
    @Query("SELECT * FROM rotinas WHERE usuarioId = :usuarioId ORDER BY nome ASC")
    fun listarRotinasPorUsuario(usuarioId: Long): Flow<List<RotinaComExercicios>>

    @Transaction
    @Query("SELECT * FROM rotinas WHERE id = :rotinaId")
    fun buscarRotinaPorId(rotinaId: Long): Flow<RotinaComExercicios?>

    @Query("DELETE FROM rotinas WHERE id = :rotinaId")
    suspend fun deletarRotina(rotinaId: Long)
}