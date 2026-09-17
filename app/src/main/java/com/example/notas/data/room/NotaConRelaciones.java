package com.example.notas.data.room;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

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
