package com.example.notas.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Libreta implements Serializable {
    private int id;
    private String titulo;
    private List<Nota> notas;
    private String fechaCreacion;

    public Libreta(int id, String titulo, List<Nota> notas, String fechaCreacion) {
        this.id = id;
        this.titulo = titulo;
        this.notas = notas;
        this.fechaCreacion = fechaCreacion;
    }

    public Libreta(int id, String titulo, String fechaCreacion) {
        this.id = id;
        this.titulo = titulo;
        notas = new ArrayList<Nota>();
        this.fechaCreacion = fechaCreacion;
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

    public Boolean isEmpty() {
        if (notas.isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Libreta{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                '}';
    }
}
