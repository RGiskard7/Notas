package com.example.notas.data.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room que representa un adjunto de una nota.
 *
 * <p>El fichero se guarda en el almacenamiento interno de la aplicación y aquí
 * solo se apunta su nombre y sus datos. Si se borra la nota, sus adjuntos se
 * borran en cascada.</p>
 */
@Entity(
        tableName = "adjuntos",
        foreignKeys = @ForeignKey(
                entity = NotaEntity.class,
                parentColumns = "nota_id",
                childColumns = "nota_id",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE),
        indices = {@Index("nota_id")})
public class AdjuntoEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "adjunto_id")
    public int id;

    @ColumnInfo(name = "nota_id")
    public int notaId;

    @NonNull
    @ColumnInfo(name = "ruta")
    public String ruta;

    @ColumnInfo(name = "nombre")
    public String nombre;

    @ColumnInfo(name = "mime")
    public String mime;

    @ColumnInfo(name = "fecha")
    public long fecha;

    public AdjuntoEntity(int id, int notaId, @NonNull String ruta, String nombre, String mime, long fecha) {
        this.id = id;
        this.notaId = notaId;
        this.ruta = ruta;
        this.nombre = nombre;
        this.mime = mime;
        this.fecha = fecha;
    }
}
