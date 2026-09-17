package com.example.notas.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Punto único de acceso a los datos de la aplicación.
 *
 * <p>Ejecuta las consultas y escrituras en un hilo de fondo y devuelve el
 * resultado al hilo principal, de forma que la interfaz nunca bloquea ni toca
 * la base de datos directamente. Los ViewModel se apoyan en él para exponer
 * los datos como {@code LiveData}.</p>
 *
 * <p>Es un singleton para no abrir varias conexiones a la vez. En los tests se
 * puede activar un modo síncrono que ejecuta las tareas en el hilo que llama.</p>
 */
public class NotasRepository {

    /**
     * Callback con el resultado de una consulta.
     *
     * @param <T> tipo del valor devuelto.
     */
    public interface Callback<T> {
        void onResult(T valor);
    }

    /** Tarea que produce un valor y que puede lanzar errores en tiempo de ejecución. */
    private interface Tarea<T> {
        T ejecutar();
    }

    private static volatile NotasRepository INSTANCE;
    private static boolean sincronoParaTests = false;

    private final INotaDAO notaDAO;
    private final ILibretaDAO libretaDAO;
    private final IEtiquetaDAO etiquetaDAO;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private NotasRepository(Context context) {
        FactoryDAO factory = FactoryDAO.getFactory(FactoryDAO.ROOM_FACTORY);
        notaDAO = factory.getNotaDao(context);
        libretaDAO = factory.getLibretaDao(context);
        etiquetaDAO = factory.getEtiquetaDao(context);
    }

    public static NotasRepository get(Context context) {
        if (INSTANCE == null) {
            synchronized (NotasRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NotasRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    @VisibleForTesting
    public static void modoSincronoParaTests() {
        sincronoParaTests = true;
    }

    @VisibleForTesting
    public static void resetParaTests() {
        NotasRepository repo = INSTANCE;
        INSTANCE = null;
        if (repo != null) {
            repo.cerrar();
        }
    }

    @VisibleForTesting
    public void cerrar() {
        executor.shutdownNow();
        notaDAO.closeDB();
        libretaDAO.closeDB();
        etiquetaDAO.closeDB();
    }

    private <T> void leer(final Tarea<T> tarea, final Callback<T> callback) {
        if (sincronoParaTests) {
            callback.onResult(tarea.ejecutar());
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final T resultado = tarea.ejecutar();
                main.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(resultado);
                    }
                });
            }
        });
    }

    private void escribir(final Runnable tarea, final Runnable onDone) {
        if (sincronoParaTests) {
            tarea.run();
            if (onDone != null) {
                onDone.run();
            }
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                tarea.run();
                if (onDone != null) {
                    main.post(onDone);
                }
            }
        });
    }

    /** Carga todas las notas. */
    public void notasTodas(Callback<List<Nota>> callback) {
        leer(new Tarea<List<Nota>>() {
            @Override
            public List<Nota> ejecutar() {
                List<Nota> lista = new ArrayList<>();
                notaDAO.getAllNotas(lista);
                return lista;
            }
        }, callback);
    }

    /** Carga las notas que pertenecen a una libreta. */
    public void notasDeLibreta(final int idLibreta, Callback<List<Nota>> callback) {
        leer(new Tarea<List<Nota>>() {
            @Override
            public List<Nota> ejecutar() {
                List<Nota> lista = new ArrayList<>();
                libretaDAO.getAllNotasFrom(idLibreta, lista);
                return lista;
            }
        }, callback);
    }

    /** Carga las notas que llevan una etiqueta. */
    public void notasDeEtiqueta(final int idEtiqueta, Callback<List<Nota>> callback) {
        leer(new Tarea<List<Nota>>() {
            @Override
            public List<Nota> ejecutar() {
                List<Nota> lista = new ArrayList<>();
                etiquetaDAO.getAllNotasFrom(idEtiqueta, lista);
                return lista;
            }
        }, callback);
    }

    /**
     * Busca notas por título o contenido.
     *
     * @param consulta   consulta ya preparada para el índice de texto.
     * @param idLibreta  si no es -1, limita la búsqueda a esa libreta.
     * @param idEtiqueta si no es -1, limita la búsqueda a esa etiqueta.
     * @param callback   recibe las notas encontradas.
     */
    public void buscarNotas(final String consulta, final int idLibreta, final int idEtiqueta,
                            Callback<List<Nota>> callback) {
        leer(new Tarea<List<Nota>>() {
            @Override
            public List<Nota> ejecutar() {
                List<Nota> lista = new ArrayList<>();
                notaDAO.buscarNotas(consulta, idLibreta, idEtiqueta, lista);
                return lista;
            }
        }, callback);
    }

    /** Carga todas las libretas con su recuento de notas. */
    public void libretas(Callback<List<Libreta>> callback) {
        leer(new Tarea<List<Libreta>>() {
            @Override
            public List<Libreta> ejecutar() {
                List<Libreta> lista = new ArrayList<>();
                libretaDAO.getAllLibretas(lista);
                return lista;
            }
        }, callback);
    }

    /** Carga todas las etiquetas con su recuento de notas. */
    public void etiquetas(Callback<List<Etiqueta>> callback) {
        leer(new Tarea<List<Etiqueta>>() {
            @Override
            public List<Etiqueta> ejecutar() {
                List<Etiqueta> lista = new ArrayList<>();
                etiquetaDAO.getAllEtiquetas(lista);
                return lista;
            }
        }, callback);
    }

    /** Carga una nota por su identificador (puede devolver null). */
    public void nota(final int id, Callback<Nota> callback) {
        leer(new Tarea<Nota>() {
            @Override
            public Nota ejecutar() {
                return notaDAO.getNota(id);
            }
        }, callback);
    }

    /** Carga las etiquetas asociadas a una nota. */
    public void etiquetasDeNota(final int idNota, Callback<List<Etiqueta>> callback) {
        leer(new Tarea<List<Etiqueta>>() {
            @Override
            public List<Etiqueta> ejecutar() {
                List<Etiqueta> lista = new ArrayList<>();
                notaDAO.getAllEtiquetasFrom(idNota, lista);
                return lista;
            }
        }, callback);
    }

    /** Comprueba si ya hay una libreta con ese título. */
    public void existeLibreta(final String titulo, Callback<Boolean> callback) {
        leer(new Tarea<Boolean>() {
            @Override
            public Boolean ejecutar() {
                return libretaDAO.existTitulo(titulo);
            }
        }, callback);
    }

    /** Comprueba si ya hay una etiqueta con ese título. */
    public void existeEtiqueta(final String titulo, Callback<Boolean> callback) {
        leer(new Tarea<Boolean>() {
            @Override
            public Boolean ejecutar() {
                return etiquetaDAO.existTitulo(titulo);
            }
        }, callback);
    }

    /** Crea la libreta si el título no está en uso; el callback recibe true si se creó. */
    public void crearLibretaSiNoExiste(final String titulo, Callback<Boolean> callback) {
        leer(new Tarea<Boolean>() {
            @Override
            public Boolean ejecutar() {
                if (libretaDAO.existTitulo(titulo)) {
                    return false;
                }
                libretaDAO.createLibreta(titulo);
                return true;
            }
        }, callback);
    }

    /** Crea la etiqueta si el título no está en uso; el callback recibe true si se creó. */
    public void crearEtiquetaSiNoExiste(final String titulo, Callback<Boolean> callback) {
        leer(new Tarea<Boolean>() {
            @Override
            public Boolean ejecutar() {
                if (etiquetaDAO.existTitulo(titulo)) {
                    return false;
                }
                etiquetaDAO.createEtiqueta(titulo);
                return true;
            }
        }, callback);
    }

    /** Cambia el título de una etiqueta si no lo tiene ya otra; el callback recibe true si se editó. */
    public void editarEtiquetaSiNoExiste(final int id, final String titulo, Callback<Boolean> callback) {
        leer(new Tarea<Boolean>() {
            @Override
            public Boolean ejecutar() {
                if (etiquetaDAO.existTitulo(titulo)) {
                    return false;
                }
                etiquetaDAO.editEtiqueta(id, titulo);
                return true;
            }
        }, callback);
    }

    /** Crea una nota, la asocia a la libreta y le añade las etiquetas indicadas. */
    public void crearNota(final String titulo, final String texto, final int idLibreta,
                          final List<Etiqueta> etiquetas, final Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                int idNota = notaDAO.createNota(titulo, texto);
                libretaDAO.addNotaToLibreta(idLibreta, idNota);
                if (etiquetas != null && !etiquetas.isEmpty()) {
                    notaDAO.addEtiquetasToNota(idNota, etiquetas);
                }
            }
        }, onDone);
    }

    /**
     * Actualiza una nota y recalcula su libreta (si ha cambiado) y sus etiquetas.
     *
     * @param id               identificador de la nota.
     * @param titulo           nuevo título.
     * @param texto            nuevo contenido.
     * @param idLibretaVieja   libreta actual de la nota.
     * @param idLibretaNueva   libreta seleccionada.
     * @param anadidas         etiquetas que hay que añadir.
     * @param quitadas         etiquetas que hay que quitar.
     * @param onDone           se ejecuta al terminar.
     */
    public void editarNota(final int id, final String titulo, final String texto,
                           final int idLibretaVieja, final int idLibretaNueva,
                           final List<Etiqueta> anadidas, final List<Etiqueta> quitadas,
                           final Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                notaDAO.editNota(id, titulo, texto);
                if (idLibretaVieja != idLibretaNueva) {
                    notaDAO.deleteLibreta(id, idLibretaVieja);
                    libretaDAO.addNotaToLibreta(idLibretaNueva, id);
                }
                if (quitadas != null && !quitadas.isEmpty()) {
                    notaDAO.deletedEtiquetasFromNota(id, quitadas);
                }
                if (anadidas != null && !anadidas.isEmpty()) {
                    notaDAO.addEtiquetasToNota(id, anadidas);
                }
            }
        }, onDone);
    }

    /** Elimina una nota y sus vínculos. */
    public void eliminarNota(final int id, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                notaDAO.deleteNota(id);
            }
        }, onDone);
    }

    /** Crea una libreta. */
    public void crearLibreta(final String titulo, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                libretaDAO.createLibreta(titulo);
            }
        }, onDone);
    }

    /** Cambia el título de una libreta. */
    public void editarLibreta(final int id, final String titulo, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                libretaDAO.editLibreta(id, titulo);
            }
        }, onDone);
    }

    /** Elimina una libreta; sus notas se mueven a la libreta Default (id 1). */
    public void eliminarLibreta(final int id, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                List<Nota> notas = new ArrayList<>();
                libretaDAO.getAllNotasFrom(id, notas);
                libretaDAO.deleteLibreta(id);
                for (Nota nota : notas) {
                    libretaDAO.addNotaToLibreta(1, nota.getId());
                }
            }
        }, onDone);
    }

    /** Elimina una etiqueta y sus vínculos con las notas. */
    public void eliminarEtiqueta(final int id, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                etiquetaDAO.deleteEtiqueta(id);
            }
        }, onDone);
    }
}
