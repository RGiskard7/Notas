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
     * Mueve una nota a la papelera (borrado lógico). Se puede recuperar con
     * {@link #restaurarNota(int)}.
     *
     * @param id identificador de la nota.
     */
    public void deleteNota(int id);

    /**
     * Saca una nota de la papelera y la deja activa otra vez.
     *
     * @param id identificador de la nota.
     */
    public void restaurarNota(int id);

    /**
     * Borra una nota definitivamente, sin posibilidad de recuperarla.
     *
     * @param id identificador de la nota.
     */
    public void borrarNotaDefinitivamente(int id);

    /**
     * Carga las notas que están en la papelera, de la más reciente a la más
     * antigua.
     *
     * @param list lista de salida.
     */
    public void getNotasEliminadas(List<Nota> list);

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

    /**
     * Añade un adjunto a una nota.
     *
     * @param idNota identificador de la nota.
     * @param ruta   nombre del fichero dentro de la carpeta de adjuntos.
     * @param nombre nombre original del fichero.
     * @param mime   tipo de contenido.
     * @return el identificador del adjunto.
     */
    public int addAdjunto(int idNota, String ruta, String nombre, String mime);

    /**
     * Carga en la lista los adjuntos de una nota.
     *
     * @param idNota identificador de la nota.
     * @param list   lista de salida.
     */
    public void getAdjuntosFrom(int idNota, List<Adjunto> list);

    /**
     * Elimina un adjunto.
     *
     * @param idAdjunto identificador del adjunto.
     */
    public void deleteAdjunto(int idAdjunto);
}
