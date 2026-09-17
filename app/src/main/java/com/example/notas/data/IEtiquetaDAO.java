package com.example.notas.data;

import java.util.List;

/**
 * Contrato de acceso a datos de las etiquetas.
 */
public interface IEtiquetaDAO {

    /**
     * Crea una etiqueta nueva.
     *
     * @param titulo nombre de la etiqueta.
     */
    public void createEtiqueta(String titulo);

    /** Cierra la conexión con la base de datos. */
    public void closeDB();

    /**
     * Recupera una etiqueta por su identificador, con su recuento de notas.
     *
     * @param id identificador de la etiqueta.
     * @return la etiqueta, o {@code null} si no existe.
     */
    public Etiqueta getEtiqueta(int id);

    /**
     * Carga todas las etiquetas en la lista indicada, sustituyendo su contenido.
     *
     * @param list lista de salida.
     */
    public void getAllEtiquetas(List<Etiqueta> list);

    /**
     * Elimina una etiqueta y sus vínculos con las notas.
     *
     * @param id identificador de la etiqueta.
     */
    public void deleteEtiqueta(int id);

    /**
     * Actualiza el título de una etiqueta. La fecha de creación se conserva.
     *
     * @param id     identificador de la etiqueta.
     * @param titulo nuevo título.
     */
    public void editEtiqueta(int id, String titulo);

    /**
     * Carga en la lista las notas que llevan esa etiqueta.
     *
     * @param idEtiqueta identificador de la etiqueta.
     * @param list       lista de salida.
     */
    public void getAllNotasFrom(int idEtiqueta, List<Nota> list);

    /**
     * Comprueba si ya existe una etiqueta con ese título.
     *
     * @param titulo título a comprobar.
     * @return {@code true} si ya existe.
     */
    public Boolean existTitulo(String titulo);
}
