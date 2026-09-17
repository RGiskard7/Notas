package com.example.notas.data.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "libretas", indices = {@Index(value = {"titulo"}, unique = true)})
public class LibretaEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "libreta_id")
    public int id;

    @NonNull
    @ColumnInfo(name = "titulo")
    public String titulo;

    @ColumnInfo(name = "fecha_creacion")
    public String fechaCreacion;

    @ColumnInfo(name = "fecha_modificacion")
    public String fechaModificacion;

    public LibretaEntity(int id, @NonNull String titulo, String fechaCreacion, String fechaModificacion) {
        this.id = id;
        this.titulo = titulo;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
    }
}
