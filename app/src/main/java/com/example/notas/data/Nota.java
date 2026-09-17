package com.example.notas.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Nota del usuario.
 *
 * <p>Una nota tiene un título, un texto libre, la libreta a la que pertenece y
 * el conjunto de etiquetas asociadas. Las fechas se guardan como milisegundos
 * desde el 1 de enero de 1970 y se formatean al mostrarlas.</p>
 *
 * <p>Implementa {@link Serializable} porque se pasa entre actividades a través
 * de un Intent.</p>
 */
public class Nota implements Serializable {

    private int id;
    private String titulo;
    private String texto;
    private Libreta libreta;
    private Set<Etiqueta> etiquetas;
    private long fechaCreacion;
    private long fechaModificacion;
    private long recordatorio;

    /**
     * Crea una nota con todos sus datos.
     *
     * @param id               identificador en la base de datos.
     * @param titulo           título de la nota.
     * @param texto            contenido de la nota.
     * @param libreta          libreta a la que pertenece (puede ser null).
     * @param etiquetas        etiquetas asociadas.
     * @param fechaCreacion    fecha de creación en milisegundos.
     */
    public Nota(int id, String titulo, String texto, Libreta libreta, List<Etiqueta> etiquetas, long fechaCreacion) {
        this.id = id;
        this.titulo = titulo;
        this.texto = texto;
        this.libreta = libreta;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaCreacion;
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

    /** Fecha del recordatorio en milisegundos, o 0 si no tiene. */
    public long getRecordatorio() {
        return recordatorio;
    }

    public void setRecordatorio(long recordatorio) {
        this.recordatorio = recordatorio;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return id == ((Nota) o).id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Nota{" +
                "titulo='" + titulo + '\'' +
                '}';
    }
}
