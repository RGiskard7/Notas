package com.example.notas.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.VisibleForTesting;

import com.example.notas.util.Adjuntos;
import com.example.notas.util.Markdown;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    // El singleton guarda el contexto de la aplicación (no de una Activity), por
    // lo que no supone una fuga de memoria.
    @SuppressLint("StaticFieldLeak")
    private static volatile NotasRepository INSTANCE;
    private static boolean sincronoParaTests = false;

    private final INotaDAO notaDAO;
    private final ILibretaDAO libretaDAO;
    private final IEtiquetaDAO etiquetaDAO;
    private final Context context;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private NotasRepository(Context context) {
        this.context = context;
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
            T resultado;
            try {
                resultado = tarea.ejecutar();
            } catch (RuntimeException e) {
                resultado = null;
            }
            callback.onResult(resultado);
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                T valor;
                try {
                    valor = tarea.ejecutar();
                } catch (RuntimeException e) {
                    valor = null;
                }
                final T resultado = valor;
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

    /** Cambia el título de una libreta si no lo tiene ya otra; el callback recibe true si se editó. */
    public void editarLibretaSiNoExiste(final int id, final String titulo, Callback<Boolean> callback) {
        leer(new Tarea<Boolean>() {
            @Override
            public Boolean ejecutar() {
                if (libretaDAO.existTitulo(titulo)) {
                    return false;
                }
                libretaDAO.editLibreta(id, titulo);
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
                          final List<Etiqueta> etiquetas, final int color, final Runnable onDone) {        escribir(new Runnable() {
            @Override
            public void run() {
                int idNota = notaDAO.createNota(titulo, texto);
                libretaDAO.addNotaToLibreta(idLibreta, idNota);
                notaDAO.setColor(idNota, color);
                if (etiquetas != null && !etiquetas.isEmpty()) {
                    notaDAO.addEtiquetasToNota(idNota, etiquetas);
                }
            }
        }, onDone);
    }

    /**
     * Crea varias notas de golpe (por ejemplo al importar una copia), todas en la
     * libreta {@code Default}.
     */
    public void crearNotas(final List<Markdown.NotaMarkdown> notas, final Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                for (Markdown.NotaMarkdown nota : notas) {
                    int idNota = notaDAO.createNota(nota.titulo, nota.texto);
                    libretaDAO.addNotaToLibreta(1, idNota);
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
                           final int color, final Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                notaDAO.editNota(id, titulo, texto);
                notaDAO.setColor(id, color);
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

    /** Mueve una nota de una libreta a otra. */
    public void moverNota(final int id, final int idLibretaVieja, final int idLibretaNueva, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                if (idLibretaVieja != idLibretaNueva) {
                    notaDAO.deleteLibreta(id, idLibretaVieja);
                    libretaDAO.addNotaToLibreta(idLibretaNueva, id);
                }
            }
        }, onDone);
    }

    /** Añade etiquetas a una nota sin quitar las que ya tiene. */
    public void anadirEtiquetasNota(final int id, final List<Etiqueta> etiquetas, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                if (etiquetas != null && !etiquetas.isEmpty()) {
                    notaDAO.addEtiquetasToNota(id, etiquetas);
                }
            }
        }, onDone);
    }

    /** Actualiza solo el texto de una nota (por ejemplo al marcar una tarea). */
    public void actualizarTextoNota(final int id, final String titulo, final String texto, Runnable onDone) {        escribir(new Runnable() {
            @Override
            public void run() {
                notaDAO.editNota(id, titulo, texto);
            }
        }, onDone);
    }

    /** Envía una nota a la papelera (se puede deshacer). */
    public void eliminarNota(final int id, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                notaDAO.deleteNota(id);
            }
        }, onDone);
    }

    /** Saca una nota de la papelera. */
    public void restaurarNota(final int id, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                notaDAO.restaurarNota(id);
            }
        }, onDone);
    }

    /** Borra una nota definitivamente, sin posibilidad de recuperarla. */
    public void borrarNotaDefinitivamente(final int id, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                List<Adjunto> adjuntos = new ArrayList<>();
                notaDAO.getAdjuntosFrom(id, adjuntos);
                notaDAO.borrarNotaDefinitivamente(id);
                for (Adjunto adjunto : adjuntos) {
                    borrarFicheroAdjunto(adjunto.getRuta());
                }
            }
        }, onDone);
    }

    /** Carga las notas que están en la papelera. */
    public void notasPapelera(Callback<List<Nota>> callback) {
        leer(new Tarea<List<Nota>>() {
            @Override
            public List<Nota> ejecutar() {
                List<Nota> lista = new ArrayList<>();
                notaDAO.getNotasEliminadas(lista);
                return lista;
            }
        }, callback);
    }

    /** Fija (o quita, con 0) el recordatorio de una nota. */
    public void ponerRecordatorio(final int id, final long cuando, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                notaDAO.setRecordatorio(id, cuando);
            }
        }, onDone);
    }

    /** Fija o desfija una nota. */
    public void fijarNota(final int id, final boolean fijada, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                notaDAO.setFijada(id, fijada ? 1 : 0);
            }
        }, onDone);
    }

    /** Cambia el color de fondo de una nota. */
    public void cambiarColorNota(final int id, final int color, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                notaDAO.setColor(id, color);
            }
        }, onDone);
    }

    /** Carga las notas con recordatorio pendiente (para reprogramarlos). */
    public void notasConRecordatorio(Callback<List<Nota>> callback) {
        leer(new Tarea<List<Nota>>() {
            @Override
            public List<Nota> ejecutar() {
                List<Nota> lista = new ArrayList<>();
                notaDAO.getNotasConRecordatorio(lista);
                return lista;
            }
        }, callback);
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

    /** Carga los adjuntos de una nota. */
    public void adjuntosDeNota(final int idNota, Callback<List<Adjunto>> callback) {
        leer(new Tarea<List<Adjunto>>() {
            @Override
            public List<Adjunto> ejecutar() {
                List<Adjunto> lista = new ArrayList<>();
                notaDAO.getAdjuntosFrom(idNota, lista);
                return lista;
            }
        }, callback);
    }

    /**
     * Copia el fichero elegido a la carpeta de adjuntos y lo asocia a la nota.
     *
     * @param idNota  nota a la que se añade.
     * @param uri     fichero seleccionado por el usuario.
     * @param nombre  nombre original del fichero.
     * @param mime    tipo de contenido.
     * @param callback recibe el adjunto creado, o {@code null} si falló la copia.
     */
    public void agregarAdjunto(final int idNota, final Uri uri, final String nombre, final String mime,
                               Callback<Adjunto> callback) {
        leer(new Tarea<Adjunto>() {
            @Override
            public Adjunto ejecutar() {
                String ruta = copiarAdjunto(uri, nombre);
                int id = notaDAO.addAdjunto(idNota, ruta, nombre, mime);
                return new Adjunto(id, idNota, ruta, nombre, mime, System.currentTimeMillis());
            }
        }, callback);
    }

    /** Elimina un adjunto de la base de datos y su fichero. */
    public void eliminarAdjunto(final Adjunto adjunto, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                notaDAO.deleteAdjunto(adjunto.getId());
                borrarFicheroAdjunto(adjunto.getRuta());
            }
        }, onDone);
    }

    /** Borra el fichero de un adjunto, si existe. */
    private void borrarFicheroAdjunto(String ruta) {
        File fichero = Adjuntos.fichero(context, ruta);
        if (fichero.exists()) {
            fichero.delete();
        }
    }

    private String copiarAdjunto(Uri uri, String nombre) {
        File carpeta = Adjuntos.carpeta(context);
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw new IllegalStateException("No se pudo crear la carpeta de adjuntos");
        }

        String destino = UUID.randomUUID().toString() + Adjuntos.extension(nombre);
        try (InputStream entrada = context.getContentResolver().openInputStream(uri);
             OutputStream salida = new FileOutputStream(new File(carpeta, destino))) {
            if (entrada == null) {
                throw new IllegalStateException("No se pudo leer el fichero");
            }
            byte[] buffer = new byte[4096];
            int leidos;
            while ((leidos = entrada.read(buffer)) != -1) {
                salida.write(buffer, 0, leidos);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo copiar el adjunto", e);
        }
        return destino;
    }
}
