package com.inkpot.app.data.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;

/**
 * Tabla FTS4 de solo índice para buscar dentro del título y el texto de las notas.
 *
 * <p>Está enlazada a {@link NotaEntity} mediante {@code contentEntity}, de forma
 * que Room la mantiene sincronizada con la tabla real: no guarda una copia, solo
 * el índice de búsqueda.</p>
 */
@Fts4(contentEntity = NotaEntity.class)
@Entity(tableName = "notas_fts")
public class NotaFts {

    @ColumnInfo(name = "titulo")
    public String titulo;

    @ColumnInfo(name = "texto")
    public String texto;

    public NotaFts(String titulo, String texto) {
        this.titulo = titulo;
        this.texto = texto;
    }
}
