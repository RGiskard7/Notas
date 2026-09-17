package com.example.notas.data;

import java.io.Serializable;

/**
 * Fichero adjunto a una nota (normalmente una imagen).
 *
 * <p>{@code ruta} es el nombre del fichero dentro de la carpeta privada de
 * adjuntos de la aplicación.</p>
 */
public class Adjunto implements Serializable {

    private int id;
    private int notaId;
    private String ruta;
    private String nombre;
    private String mime;
    private long fecha;

    public Adjunto(int id, int notaId, String ruta, String nombre, String mime, long fecha) {
        this.id = id;
        this.notaId = notaId;
        this.ruta = ruta;
        this.nombre = nombre;
        this.mime = mime;
        this.fecha = fecha;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNotaId() {
        return notaId;
    }

    public void setNotaId(int notaId) {
        this.notaId = notaId;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }
}
