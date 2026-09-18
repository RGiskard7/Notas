package com.example.notas.UI;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.notas.data.Adjunto;
import com.example.notas.data.Etiqueta;
import com.example.notas.data.Libreta;
import com.example.notas.data.NotasRepository;

import java.util.List;

/**
 * ViewModel de la pantalla de crear o editar una nota.
 *
 * <p>Carga las libretas, las etiquetas disponibles y las etiquetas de la nota, y
 * guarda los cambios.</p>
 */
public class EditNotaViewModel extends AndroidViewModel {
    private final NotasRepository repositorio;
    private final MutableLiveData<List<Libreta>> libretas = new MutableLiveData<>();
    private final MutableLiveData<List<Etiqueta>> etiquetas = new MutableLiveData<>();
    private final MutableLiveData<List<Etiqueta>> etiquetasDeNota = new MutableLiveData<>();
    private final MutableLiveData<List<Adjunto>> adjuntos = new MutableLiveData<>();

    public EditNotaViewModel(@NonNull Application application) {
        super(application);
        repositorio = NotasRepository.get(application);
    }

    public LiveData<List<Libreta>> getLibretas() {
        return libretas;
    }

    public LiveData<List<Etiqueta>> getEtiquetas() {
        return etiquetas;
    }

    public LiveData<List<Etiqueta>> getEtiquetasDeNota() {
        return etiquetasDeNota;
    }

    public LiveData<List<Adjunto>> getAdjuntos() {
        return adjuntos;
    }

    public void cargarLibretas() {
        repositorio.libretas(new NotasRepository.Callback<List<Libreta>>() {
            @Override
            public void onResult(List<Libreta> valor) {
                libretas.setValue(valor);
            }
        });
    }

    public void cargarEtiquetas() {
        repositorio.etiquetas(new NotasRepository.Callback<List<Etiqueta>>() {
            @Override
            public void onResult(List<Etiqueta> valor) {
                etiquetas.setValue(valor);
            }
        });
    }

    public void cargarEtiquetasDeNota(int idNota) {
        repositorio.etiquetasDeNota(idNota, new NotasRepository.Callback<List<Etiqueta>>() {
            @Override
            public void onResult(List<Etiqueta> valor) {
                etiquetasDeNota.setValue(valor);
            }
        });
    }

    public void crearNota(String titulo, String texto, int idLibreta, List<Etiqueta> etiquetas, Runnable onDone) {
        repositorio.crearNota(titulo, texto, idLibreta, etiquetas, onDone);
    }

    public void editarNota(int id, String titulo, String texto, int idLibretaVieja, int idLibretaNueva,
                           List<Etiqueta> anadidas, List<Etiqueta> quitadas, Runnable onDone) {
        repositorio.editarNota(id, titulo, texto, idLibretaVieja, idLibretaNueva, anadidas, quitadas, onDone);
    }

    public void cargarAdjuntos(int idNota) {
        repositorio.adjuntosDeNota(idNota, new NotasRepository.Callback<List<Adjunto>>() {
            @Override
            public void onResult(List<Adjunto> valor) {
                adjuntos.setValue(valor);
            }
        });
    }

    /** Copia la imagen a la carpeta de adjuntos; el callback recibe true si se añadió. */
    public void agregarAdjunto(final int idNota, Uri uri, String nombre, String mime,
                               final NotasRepository.Callback<Boolean> callback) {
        repositorio.agregarAdjunto(idNota, uri, nombre, mime, new NotasRepository.Callback<Adjunto>() {
            @Override
            public void onResult(Adjunto valor) {
                if (valor != null) {
                    cargarAdjuntos(idNota);
                }
                callback.onResult(valor != null);
            }
        });
    }

    public void eliminarAdjunto(final Adjunto adjunto, final Runnable onDone) {
        repositorio.eliminarAdjunto(adjunto, new Runnable() {
            @Override
            public void run() {
                cargarAdjuntos(adjunto.getNotaId());
                if (onDone != null) {
                    onDone.run();
                }
            }
        });
    }
}
