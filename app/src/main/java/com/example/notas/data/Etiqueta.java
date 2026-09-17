package com.example.notas.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Etiqueta implements Serializable {
    private int id;
    private String titulo;
    private List<Nota> notas;
    private String fechaCreacion;
    private String fechaModificacion;

    public Etiqueta(int id, String titulo, String fechaCreacion) {
        this.id = id;
        this.titulo = titulo;
        notas = new ArrayList<Nota>();
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaCreacion;
    }

    public Etiqueta(int id, String titulo, List<Nota> notas, String fechaCreacion) {
        this.id = id;
        this.titulo = titulo;
        this.notas = notas;
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

    public List<Nota> getNotas() {
        return notas;
    }

    public void setNotas(List<Nota> notas) {
        this.notas = notas;
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

    public Boolean isEmpty() {
        if (notas.isEmpty()) {
            return true;
        }
        return false;
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
