package com.inkpot.app.data.room;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

/**
 * Libreta con sus notas ya cargadas, para calcular el recuento sin consultas
 * adicionales.
 */
public class LibretaConRelaciones {
    @Embedded
    public LibretaEntity libreta;

    @Relation(
            parentColumn = "libreta_id",
            entityColumn = "nota_id",
            associateBy = @Junction(LibretaNotaCrossRef.class))
    public List<NotaEntity> notas;
}
