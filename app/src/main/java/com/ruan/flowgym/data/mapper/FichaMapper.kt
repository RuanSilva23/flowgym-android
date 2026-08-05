package com.ruan.flowgym.data.mapper

import com.ruan.flowgym.data.local.entity.FichaExercicioEntity
import com.ruan.flowgym.data.local.entity.RotinaEntity
import com.ruan.flowgym.data.model.FichaResponseDTO
import com.ruan.flowgym.data.model.RotinaResponseDTO

// Transforma a Rotina do Backend no formato do ROOM
fun RotinaResponseDTO.toEntity(usuarioId: Long): RotinaEntity {
    return RotinaEntity(
        id = this.id,
        usuarioId = usuarioId,
        nome = this.nome,
        descricao = this.descricao
    )
}

// Transforma o item de exercício do Backend no formato do ROOM
fun FichaResponseDTO.toEntity(rotinaId: Long): FichaExercicioEntity {
    return FichaExercicioEntity(
        id = this.id,
        rotinaId = rotinaId,
        exercicioId = this.idExercicio,
        ordem = this.ordem,
        seriesAlvo = this.seriesAlvo,
        repeticoesAlvo = this.repeticoesAlvo,
        cargaAlvo = this.cargaAlvo,
        descansoSeg = this.descanso
    )
}