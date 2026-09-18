package com.example.notas.data.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad Room que representa la tabla de notas.
 *
 * <p>{@code eliminadaEn} vale 0 mientras la nota está activa y guarda la fecha
 * del borrado cuando está en la papelera. {@code fijada} vale 1 si la nota está
 * fijada y {@code color} es el índice de su color de fondo (0 = ninguno).</p>
 */
@Entity(tableName = "notas")
public class NotaEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "nota_id")
    public int id;

    @NonNull
    @ColumnInfo(name = "titulo")
    public String titulo;

    @ColumnInfo(name = "texto")
    public String texto;

    @ColumnInfo(name = "fecha_creacion")
    public long fechaCreacion;

    @ColumnInfo(name = "fecha_modificacion")
    public long fechaModificacion;

    @ColumnInfo(name = "eliminada_en")
    public long eliminadaEn;

    @ColumnInfo(name = "recordatorio")
    public long recordatorio;

    @ColumnInfo(name = "fijada")
    public int fijada;

    @ColumnInfo(name = "color")
    public int color;

    public NotaEntity(int id, @NonNull String titulo, String texto, long fechaCreacion,
                      long fechaModificacion, long eliminadaEn, long recordatorio,
                      int fijada, int color) {
        this.id = id;
        this.titulo = titulo;
        this.texto = texto;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
        this.eliminadaEn = eliminadaEn;
        this.recordatorio = recordatorio;
        this.fijada = fijada;
        this.color = color;
    }
}
