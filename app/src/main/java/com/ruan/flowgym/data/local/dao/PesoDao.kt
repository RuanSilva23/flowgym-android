package com.ruan.flowgym.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ruan.flowgym.data.local.entity.PesoEntity

@Dao
interface PesoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarPesoLocal(peso: PesoEntity): Long

    @Query("SELECT * FROM pesos_pendentes WHERE usuarioId = :usuarioId")
    suspend fun listarPesosPendentes(usuarioId: Long): List<PesoEntity>

    @Query("DELETE FROM pesos_pendentes WHERE idLocal = :idLocal")
    suspend fun deletarPesoPendente(idLocal: Long)
}