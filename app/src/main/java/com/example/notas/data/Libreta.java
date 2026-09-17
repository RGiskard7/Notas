package com.example.notas.data;

import java.io.Serializable;

public class Libreta implements Serializable {
    private int id;
    private String titulo;
    private int numNotas;
    private String fechaCreacion;
    private String fechaModificacion;

    public Libreta(int id, String titulo, String fechaCreacion) {
        this(id, titulo, 0, fechaCreacion);
    }

    public Libreta(int id, String titulo, int numNotas, String fechaCreacion) {
        this.id = id;
        this.titulo = titulo;
        this.numNotas = numNotas;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaCreacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getNumNotas() {
        return numNotas;
    }

    public void setNumNotas(int numNotas) {
        this.numNotas = numNotas;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(String fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    @Override
    public String toString() {
        return "Libreta{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                '}';
    }
}
