package com.inkpot.app.data;

import java.util.List;

/**
 * Contrato de acceso a datos de las libretas.
 *
 * <p>Además del CRUD básico incluye la consulta de las notas que contiene cada
 * libreta y el vínculo entre ambas.</p>
 */
public interface ILibretaDAO {

    /**
     * Crea una libreta nueva.
     *
     * @param titulo nombre de la libreta.
     */
    public void createLibreta(String titulo);

    /** Cierra la conexión con la base de datos. */
    public void closeDB();

    /**
     * Comprueba si ya existe una libreta con ese título.
     *
     * @param titulo título a comprobar.
     * @return {@code true} si ya existe.
     */
    public Boolean existTitulo(String titulo);

    /**
     * Recupera una libreta por su identificador, con su recuento de notas.
     *
     * @param id identificador de la libreta.
     * @return la libreta, o {@code null} si no existe.
     */
    public Libreta getLibreta(int id);

    /**
     * Elimina una libreta. Las notas que contenía no se borran.
     *
     * @param id identificador de la libreta.
     */
    public void deleteLibreta(int id);

    /**
     * Vincula una nota a una libreta.
     *
     * @param idLibreta identificador de la libreta.
     * @param idNota    identificador de la nota.
     */
    public void addNotaToLibreta(int idLibreta, int idNota);

    /**
     * Actualiza el título de una libreta. La fecha de creación se conserva.
     *
     * @param id     identificador de la libreta.
     * @param titulo nuevo título.
     */
    public void editLibreta(int id, String titulo);

    /**
     * Carga todas las libretas en la lista indicada, sustituyendo su contenido.
     *
     * @param list lista de salida.
     */
    public void getAllLibretas(List<Libreta> list);

    /**
     * Carga en la lista las notas que pertenecen a una libreta.
     *
     * @param idLibreta identificador de la libreta.
     * @param list      lista de salida.
     */
    public void getAllNotasFrom(int idLibreta, List<Nota> list);
}
