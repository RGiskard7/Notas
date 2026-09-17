package com.example.notas.data.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "etiquetaNotas",
        primaryKeys = {"etiqueta_id", "nota_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = EtiquetaEntity.class,
                        parentColumns = "etiqueta_id",
                        childColumns = "etiqueta_id",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE),
                @ForeignKey(
                        entity = NotaEntity.class,
                        parentColumns = "nota_id",
                        childColumns = "nota_id",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE)
        },
        indices = {@Index("nota_id"), @Index("etiqueta_id")})
public class EtiquetaNotaCrossRef {
    @ColumnInfo(name = "etiqueta_id")
    public int etiquetaId;

    @ColumnInfo(name = "nota_id")
    public int notaId;

    public EtiquetaNotaCrossRef(int etiquetaId, int notaId) {
        this.etiquetaId = etiquetaId;
        this.notaId = notaId;
    }
}
