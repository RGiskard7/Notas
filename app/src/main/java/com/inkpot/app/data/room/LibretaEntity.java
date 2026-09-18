package com.inkpot.app.data.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "libretas", indices = {@Index(value = {"titulo"}, unique = true)})
/**
 * Entidad Room que representa la tabla de libretas.
 */
public class LibretaEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "libreta_id")
    public int id;

    @NonNull
    @ColumnInfo(name = "titulo")
    public String titulo;

    @ColumnInfo(name = "fecha_creacion")
    public long fechaCreacion;

    @ColumnInfo(name = "fecha_modificacion")
    public long fechaModificacion;

    public LibretaEntity(int id, @NonNull String titulo, long fechaCreacion, long fechaModificacion) {
        this.id = id;
        this.titulo = titulo;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
    }
}
