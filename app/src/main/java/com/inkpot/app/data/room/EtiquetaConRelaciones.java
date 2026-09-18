package com.inkpot.app.data.room;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

/**
 * Etiqueta con sus notas ya cargadas, para calcular el recuento sin consultas
 * adicionales.
 */
public class EtiquetaConRelaciones {
    @Embedded
    public EtiquetaEntity etiqueta;

    @Relation(
            parentColumn = "etiqueta_id",
            entityColumn = "nota_id",
            associateBy = @Junction(EtiquetaNotaCrossRef.class))
    public List<NotaEntity> notas;
}
