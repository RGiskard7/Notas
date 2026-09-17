package com.example.notas.data;

import java.io.Serializable;

/**
 * Libreta que agrupa notas.
 *
 * <p>Solo almacena sus propios datos (título y fechas) y el número de notas que
 * contiene, que es lo que necesitan los listados. Las notas se consultan a
 * través del DAO correspondiente.</p>
 */
public class Libreta implements Serializable {

    private int id;
    private String titulo;
    private int numNotas;
    private long fechaCreacion;
    private long fechaModificacion;

    /**
     * Crea una libreta sin recuento de notas.
     *
     * @param id            identificador en la base de datos.
     * @param titulo        nombre de la libreta.
     * @param fechaCreacion fecha de creación en milisegundos.
     */
    public Libreta(int id, String titulo, long fechaCreacion) {
        this(id, titulo, 0, fechaCreacion);
    }

    /**
     * Crea una libreta con todos sus datos.
     *
     * @param id            identificador en la base de datos.
     * @param titulo        nombre de la libreta.
     * @param numNotas      número de notas que contiene.
     * @param fechaCreacion fecha de creación en milisegundos.
     */
    public Libreta(int id, String titulo, int numNotas, long fechaCreacion) {
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
        return id == ((Libreta) o).id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Libreta{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                '}';
    }
}
