package com.example.notas.data;

import java.io.Serializable;

public class Nota implements Serializable {
    private int id;
    private String titulo;
    private String texto;
    private Libreta libreta;
    private String fechaCreacion;

    public Nota(int id, String titulo, String texto, String fechaCreacion) {
        this.id = id;
        this.titulo = titulo;
        this.texto = texto;
        this.fechaCreacion = fechaCreacion;
    }

    public Nota(int id, String titulo, String texto, Libreta libreta, String fechaCreacion) {
        this.id = id;
        this.titulo = titulo;
        this.texto = texto;
        this.libreta = libreta;
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

    @Override
    public String toString() {
        return "Nota{" +
                "titulo='" + titulo + '\'' +
                '}';
    }
}
