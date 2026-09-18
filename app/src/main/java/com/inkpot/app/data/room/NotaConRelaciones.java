package com.inkpot.app.data.room;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

/**
 * Nota con su libreta y sus etiquetas ya cargadas.
 *
 * <p>Se usa en las consultas {@code @Transaction} para evitar una consulta por
 * cada nota (problema N+1).</p>
 */
public class NotaConRelaciones {
    @Embedded
    public NotaEntity nota;

    @Relation(
            parentColumn = "nota_id",
            entityColumn = "etiqueta_id",
            associateBy = @Junction(EtiquetaNotaCrossRef.class))
    public List<EtiquetaEntity> etiquetas;

    @Relation(
            parentColumn = "nota_id",
            entityColumn = "libreta_id",
            associateBy = @Junction(LibretaNotaCrossRef.class))
    public List<LibretaEntity> libretas;
}
