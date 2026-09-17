package com.example.notas.UI;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.notas.data.Nota;
import com.example.notas.data.NotasRepository;

import java.util.List;

public class ListNotasViewModel extends AndroidViewModel {
    private final NotasRepository repositorio;
    private final MutableLiveData<List<Nota>> notas = new MutableLiveData<>();
    private int idLibreta = -1;
    private int idEtiqueta = -1;

    public ListNotasViewModel(@NonNull Application application) {
        super(application);
        repositorio = NotasRepository.get(application);
    }

    public LiveData<List<Nota>> getNotas() {
        return notas;
    }

    public void cargarTodas() {
        idLibreta = -1;
        idEtiqueta = -1;
        repositorio.notasTodas(new NotasRepository.Callback<List<Nota>>() {
            @Override
            public void onResult(List<Nota> valor) {
                notas.setValue(valor);
            }
        });
    }

    public void cargarDeLibreta(final int id) {
        idLibreta = id;
        idEtiqueta = -1;
        repositorio.notasDeLibreta(id, new NotasRepository.Callback<List<Nota>>() {
            @Override
            public void onResult(List<Nota> valor) {
                notas.setValue(valor);
            }
        });
    }

    public void cargarDeEtiqueta(final int id) {
        idEtiqueta = id;
        idLibreta = -1;
        repositorio.notasDeEtiqueta(id, new NotasRepository.Callback<List<Nota>>() {
            @Override
            public void onResult(List<Nota> valor) {
                notas.setValue(valor);
            }
        });
    }

    public void recargar() {
        if (idLibreta != -1) {
            cargarDeLibreta(idLibreta);
        } else if (idEtiqueta != -1) {
            cargarDeEtiqueta(idEtiqueta);
        } else {
            cargarTodas();
        }
    }

    public void eliminar(int id) {
        repositorio.eliminarNota(id, new Runnable() {
            @Override
            public void run() {
                recargar();
            }
        });
    }
}
