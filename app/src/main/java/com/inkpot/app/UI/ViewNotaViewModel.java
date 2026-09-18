package com.inkpot.app.UI;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.inkpot.app.data.Adjunto;
import com.inkpot.app.data.Etiqueta;
import com.inkpot.app.data.Nota;
import com.inkpot.app.data.NotasRepository;

import java.util.List;

/**
 * ViewModel de la pantalla que muestra una nota.
 *
 * <p>Carga las etiquetas y los adjuntos, permite eliminarla y guardar cambios
 * sueltos, como marcar una tarea o añadir una imagen.</p>
 */
public class ViewNotaViewModel extends AndroidViewModel {
    private final NotasRepository repositorio;
    private final MutableLiveData<List<Etiqueta>> etiquetasDeNota = new MutableLiveData<>();
    private final MutableLiveData<List<Adjunto>> adjuntos = new MutableLiveData<>();
    private final MutableLiveData<Nota> nota = new MutableLiveData<>();

    public ViewNotaViewModel(@NonNull Application application) {
        super(application);
        repositorio = NotasRepository.get(application);
    }

    public LiveData<List<Etiqueta>> getEtiquetasDeNota() {
        return etiquetasDeNota;
    }

    public LiveData<List<Adjunto>> getAdjuntos() {
        return adjuntos;
    }

    public LiveData<Nota> getNota() {
        return nota;
    }

    public void cargarNota(int idNota) {
        repositorio.nota(idNota, new NotasRepository.Callback<Nota>() {
            @Override
            public void onResult(Nota valor) {
                nota.setValue(valor);
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

    public void cargarAdjuntos(int idNota) {
        repositorio.adjuntosDeNota(idNota, new NotasRepository.Callback<List<Adjunto>>() {
            @Override
            public void onResult(List<Adjunto> valor) {
                adjuntos.setValue(valor);
            }
        });
    }

    public void eliminar(int id, Runnable onDone) {
        repositorio.eliminarNota(id, onDone);
    }

    /** Fija o desfija la nota. */
    public void fijarNota(int id, boolean fijada, Runnable onDone) {
        repositorio.fijarNota(id, fijada, onDone);
    }

    /** Guarda el texto de la nota (por ejemplo al marcar una tarea). */
    public void actualizarTexto(int id, String titulo, String texto, Runnable onDone) {
        repositorio.actualizarTextoNota(id, titulo, texto, onDone);
    }

    /** Fija (o quita, con 0) el recordatorio de la nota. */
    public void ponerRecordatorio(int id, long cuando, Runnable onDone) {
        repositorio.ponerRecordatorio(id, cuando, onDone);
    }
}
