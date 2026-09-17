package com.example.notas.data;

import java.io.Serializable;
import java.util.Objects;

public class Etiqueta implements Serializable {
    private int id;
    private String titulo;
    private int numNotas;
    private String fechaCreacion;
    private String fechaModificacion;

    public Etiqueta(int id, String titulo, String fechaCreacion) {
        this(id, titulo, 0, fechaCreacion);
    }

    public Etiqueta(int id, String titulo, int numNotas, String fechaCreacion) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Etiqueta etiqueta = (Etiqueta) o;
        return titulo.equals(etiqueta.titulo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titulo);
    }
}
