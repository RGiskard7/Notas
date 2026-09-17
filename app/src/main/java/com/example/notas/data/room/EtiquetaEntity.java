package com.example.notas.data.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "etiquetas", indices = {@Index(value = {"titulo"}, unique = true)})
/**
 * Entidad Room que representa la tabla de etiquetas.
 */
public class EtiquetaEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "etiqueta_id")
    public int id;

    @NonNull
    @ColumnInfo(name = "titulo")
    public String titulo;

    @ColumnInfo(name = "fecha_creacion")
    public long fechaCreacion;

    @ColumnInfo(name = "fecha_modificacion")
    public long fechaModificacion;

    public EtiquetaEntity(int id, @NonNull String titulo, long fechaCreacion, long fechaModificacion) {
        this.id = id;
        this.titulo = titulo;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
    }
}
