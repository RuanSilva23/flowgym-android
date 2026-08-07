package com.ruan.flowgym.data.local.dao

import androidx.room.*
import com.ruan.flowgym.data.local.entity.SeriePendenteEntity
import com.ruan.flowgym.data.local.entity.SessaoPendenteEntity

@Dao
interface SessaoPendenteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarSessaoPendente(sessao: SessaoPendenteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarSeriePendente(serie: SeriePendenteEntity)

    @Query("SELECT * FROM sessoes_pendentes")
    suspend fun listarSessoesPendentes(): List<SessaoPendenteEntity>

    @Query("SELECT * FROM series_pendentes WHERE sessaoLocalId = :sessaoLocalId")
    suspend fun listarSeriesDaSessao(sessaoLocalId: Long): List<SeriePendenteEntity>

    @Query("DELETE FROM sessoes_pendentes WHERE idLocal = :sessaoLocalId")
    suspend fun deletarSessaoPendente(sessaoLocalId: Long)

    @Query("DELETE FROM series_pendentes WHERE sessaoLocalId = :sessaoLocalId")
    suspend fun deletarSeriesDaSessao(sessaoLocalId: Long)
}