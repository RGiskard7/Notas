package com.inkpot.app.data.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "libretaNotas",
        primaryKeys = {"libreta_id", "nota_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = LibretaEntity.class,
                        parentColumns = "libreta_id",
                        childColumns = "libreta_id",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE),
                @ForeignKey(
                        entity = NotaEntity.class,
                        parentColumns = "nota_id",
                        childColumns = "nota_id",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE)
        },
        indices = {@Index("nota_id"), @Index("libreta_id")})
/**
 * Tabla puente que relaciona libretas y notas (una libreta tiene muchas notas).
 */
public class LibretaNotaCrossRef {
    @ColumnInfo(name = "libreta_id")
    public int libretaId;

    @ColumnInfo(name = "nota_id")
    public int notaId;

    public LibretaNotaCrossRef(int libretaId, int notaId) {
        this.libretaId = libretaId;
        this.notaId = notaId;
    }
}
