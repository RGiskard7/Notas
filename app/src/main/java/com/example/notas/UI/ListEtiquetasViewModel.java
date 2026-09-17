package com.example.notas.UI;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.notas.data.Etiqueta;
import com.example.notas.data.NotasRepository;

import java.util.List;

public class ListEtiquetasViewModel extends AndroidViewModel {
    private final NotasRepository repositorio;
    private final MutableLiveData<List<Etiqueta>> etiquetas = new MutableLiveData<>();

    public ListEtiquetasViewModel(@NonNull Application application) {
        super(application);
        repositorio = NotasRepository.get(application);
    }

    public LiveData<List<Etiqueta>> getEtiquetas() {
        return etiquetas;
    }

    public void cargar() {
        repositorio.etiquetas(new NotasRepository.Callback<List<Etiqueta>>() {
            @Override
            public void onResult(List<Etiqueta> valor) {
                etiquetas.setValue(valor);
            }
        });
    }

    public void eliminar(int id) {
        repositorio.eliminarEtiqueta(id, new Runnable() {
            @Override
            public void run() {
                cargar();
            }
        });
    }

    public void editar(int id, String titulo, NotasRepository.Callback<Boolean> callback) {
        repositorio.editarEtiquetaSiNoExiste(id, titulo, callback);
    }
}
