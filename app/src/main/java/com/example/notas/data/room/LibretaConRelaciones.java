package com.example.notas.data.room;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

public class LibretaConRelaciones {
    @Embedded
    public LibretaEntity libreta;

    @Relation(
            parentColumn = "libreta_id",
            entityColumn = "nota_id",
            associateBy = @Junction(LibretaNotaCrossRef.class))
    public List<NotaEntity> notas;
}
