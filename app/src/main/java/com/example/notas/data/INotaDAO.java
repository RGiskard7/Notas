package com.example.notas.data;

import java.util.List;

/**
 * Contrato de acceso a datos de las notas.
 *
 * <p>Las implementaciones concretas (por ejemplo la basada en Room) se encargan
 * de la persistencia; el resto de la aplicación solo conoce estos métodos.</p>
 */
public interface INotaDAO {

    /**
     * Crea una nota vacía de etiquetas.
     *
     * @param titulo título de la nota.
     * @param texto  contenido de la nota.
     * @return el identificador asignado a la nueva nota.
     */
    public int createNota(String titulo, String texto);

    /** Cierra la conexión con la base de datos. */
    public void closeDB();

    /**
     * Recupera una nota por su identificador.
     *
     * @param id identificador de la nota.
     * @return la nota, o {@code null} si no existe.
     */
    public Nota getNota(int id);

    /**
     * Actualiza el título y el texto de una nota existente. La fecha de creación
     * se conserva y se actualiza la de modificación.
     *
     * @param id     identificador de la nota.
     * @param titulo nuevo título.
     * @param texto  nuevo contenido.
     */
    public void editNota(int id, String titulo, String texto);

    /**
     * Devuelve la libreta a la que pertenece una nota.
     *
     * @param idNota identificador de la nota.
     * @return la libreta, o {@code null} si la nota no está asociada a ninguna.
     */
    public Libreta getLibreta(int idNota);

    /**
     * Desvincula una nota de una libreta.
     *
     * @param idNota    identificador de la nota.
     * @param idLibreta identificador de la libreta.
     */
    public void deleteLibreta(int idNota, int idLibreta);

    /**
     * Elimina una nota y sus vínculos con libretas y etiquetas.
     *
     * @param id identificador de la nota.
     */
    public void deleteNota(int id);

    /**
     * Carga todas las notas en la lista indicada, sustituyendo su contenido.
     *
     * @param list lista de salida.
     */
    public void getAllNotas(List<Nota> list);

    /**
     * Asocia una serie de etiquetas a una nota.
     *
     * @param idNota    identificador de la nota.
     * @param etiquetas etiquetas que se añaden.
     */
    public void addEtiquetasToNota(int idNota, List<Etiqueta> etiquetas);

    /**
     * Quita una serie de etiquetas de una nota.
     *
     * @param idNota    identificador de la nota.
     * @param etiquetas etiquetas que se eliminan.
     */
    public void deletedEtiquetasFromNota(int idNota, List<Etiqueta> etiquetas);

    /**
     * Carga en la lista las etiquetas asociadas a una nota.
     *
     * @param idNota identificador de la nota.
     * @param list   lista de salida.
     */
    public void getAllEtiquetasFrom(int idNota, List<Etiqueta> list);

    /**
     * Busca notas por título o contenido.
     *
     * @param consulta   consulta ya preparada para el índice de texto.
     * @param idLibreta  si no es -1, limita la búsqueda a esa libreta.
     * @param idEtiqueta si no es -1, limita la búsqueda a esa etiqueta.
     * @param list       lista de salida.
     */
    public void buscarNotas(String consulta, int idLibreta, int idEtiqueta, List<Nota> list);
}
