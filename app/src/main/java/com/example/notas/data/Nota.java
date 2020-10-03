package com.example.notas.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Nota implements Serializable {
    private int id;
    private String titulo;
    private String texto;
    private Libreta libreta;
    private Set<Etiqueta> etiquetas;
    private String fechaCreacion;

    public Nota(int id, String titulo, String texto, String fechaCreacion) {
        this.id = id;
        this.titulo = titulo;
        this.texto = texto;
        this.fechaCreacion = fechaCreacion;
        etiquetas = new HashSet<>();
    }

    public Nota(int id, String titulo, String texto, Libreta libreta, List<Etiqueta> etiquetas, String fechaCreacion) {
        this.id = id;
        this.titulo = titulo;
        this.texto = texto;
        this.libreta = libreta;
        this.fechaCreacion = fechaCreacion;
        this.etiquetas = new HashSet<>(etiquetas);
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

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Libreta getLibreta() {
        return libreta;
    }

    public void setLibreta(Libreta libreta) {
        this.libreta = libreta;
    }

    public Set<Etiqueta> getEtiquetas() {
        return etiquetas;
    }

    public void setEtiquetas(Set<Etiqueta> etiquetas) {
        this.etiquetas = etiquetas;
    }

    @Override
    public String toString() {
        return "Nota{" +
                "titulo='" + titulo + '\'' +
                '}';
    }
}
