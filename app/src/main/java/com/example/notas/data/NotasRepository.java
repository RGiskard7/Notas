package com.example.notas.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotasRepository {
    public interface Callback<T> {
        void onResult(T valor);
    }

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

    public void nota(final int id, Callback<Nota> callback) {
        leer(new Tarea<Nota>() {
            @Override
            public Nota ejecutar() {
                return notaDAO.getNota(id);
            }
        }, callback);
    }

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

    public void existeLibreta(final String titulo, Callback<Boolean> callback) {
        leer(new Tarea<Boolean>() {
            @Override
            public Boolean ejecutar() {
                return libretaDAO.existTitulo(titulo);
            }
        }, callback);
    }

    public void existeEtiqueta(final String titulo, Callback<Boolean> callback) {
        leer(new Tarea<Boolean>() {
            @Override
            public Boolean ejecutar() {
                return etiquetaDAO.existTitulo(titulo);
            }
        }, callback);
    }

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

    public void eliminarNota(final int id, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                notaDAO.deleteNota(id);
            }
        }, onDone);
    }

    public void crearLibreta(final String titulo, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                libretaDAO.createLibreta(titulo);
            }
        }, onDone);
    }

    public void editarLibreta(final int id, final String titulo, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                libretaDAO.editLibreta(id, titulo);
            }
        }, onDone);
    }

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

    public void eliminarEtiqueta(final int id, Runnable onDone) {
        escribir(new Runnable() {
            @Override
            public void run() {
                etiquetaDAO.deleteEtiqueta(id);
            }
        }, onDone);
    }
}
