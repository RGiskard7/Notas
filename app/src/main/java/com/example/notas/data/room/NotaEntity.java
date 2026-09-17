package com.example.notas.data.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notas")
/**
 * Entidad Room que representa la tabla de notas.
 */
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

    public NotaEntity(int id, @NonNull String titulo, String texto, long fechaCreacion, long fechaModificacion) {
        this.id = id;
        this.titulo = titulo;
        this.texto = texto;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
    }
}
