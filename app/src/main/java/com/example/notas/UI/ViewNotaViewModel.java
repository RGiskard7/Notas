package com.example.notas.UI;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.notas.data.Etiqueta;
import com.example.notas.data.Nota;
import com.example.notas.data.NotasRepository;

import java.util.List;

/**
 * ViewModel de la pantalla que muestra una nota.
 *
 * <p>Carga las etiquetas asociadas y permite eliminar la nota.</p>
 */
public class ViewNotaViewModel extends AndroidViewModel {
    private final NotasRepository repositorio;
    private final MutableLiveData<List<Etiqueta>> etiquetasDeNota = new MutableLiveData<>();
    private final MutableLiveData<Nota> nota = new MutableLiveData<>();

    public ViewNotaViewModel(@NonNull Application application) {
        super(application);
        repositorio = NotasRepository.get(application);
    }

    public LiveData<List<Etiqueta>> getEtiquetasDeNota() {
        return etiquetasDeNota;
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

    public void eliminar(int id, Runnable onDone) {
        repositorio.eliminarNota(id, onDone);
    }
}
