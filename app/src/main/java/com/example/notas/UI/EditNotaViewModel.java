package com.example.notas.UI;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.notas.data.Etiqueta;
import com.example.notas.data.Libreta;
import com.example.notas.data.NotasRepository;

import java.util.List;

public class EditNotaViewModel extends AndroidViewModel {
    private final NotasRepository repositorio;
    private final MutableLiveData<List<Libreta>> libretas = new MutableLiveData<>();
    private final MutableLiveData<List<Etiqueta>> etiquetas = new MutableLiveData<>();
    private final MutableLiveData<List<Etiqueta>> etiquetasDeNota = new MutableLiveData<>();

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
}
