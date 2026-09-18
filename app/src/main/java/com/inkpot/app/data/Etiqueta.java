package com.inkpot.app.data;

import java.io.Serializable;

/**
 * Etiqueta que puede asociarse a varias notas.
 *
 * <p>Igual que {@link Libreta}, guarda sus propios datos y el número de notas
 * que la usan. Su identidad a efectos de comparación es el título.</p>
 */
public class Etiqueta implements Serializable {

    private int id;
    private String titulo;
    private int numNotas;
    private long fechaCreacion;
    private long fechaModificacion;

    /**
     * Crea una etiqueta sin recuento de notas.
     *
     * @param id            identificador en la base de datos.
     * @param titulo        nombre de la etiqueta.
     * @param fechaCreacion fecha de creación en milisegundos.
     */
    public Etiqueta(int id, String titulo, long fechaCreacion) {
        this(id, titulo, 0, fechaCreacion);
    }

    /**
     * Crea una etiqueta con todos sus datos.
     *
     * @param id            identificador en la base de datos.
     * @param titulo        nombre de la etiqueta.
     * @param numNotas      número de notas que la usan.
     * @param fechaCreacion fecha de creación en milisegundos.
     */
    public Etiqueta(int id, String titulo, int numNotas, long fechaCreacion) {
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

    public long getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(long fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public long getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(long fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Etiqueta etiqueta = (Etiqueta) o;
        return titulo.equals(etiqueta.titulo);
    }

    @Override
    public int hashCode() {
        return titulo.hashCode();
    }
}
