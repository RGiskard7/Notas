package com.example.notas.UI;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.notas.data.Nota;
import com.example.notas.data.NotasRepository;

import java.util.List;

/**
 * ViewModel de la papelera.
 *
 * <p>Carga las notas eliminadas y permite restaurarlas o borrarlas
 * definitivamente.</p>
 */
public class ListPapeleraViewModel extends AndroidViewModel {
    private final NotasRepository repositorio;
    private final MutableLiveData<List<Nota>> notas = new MutableLiveData<>();

    public ListPapeleraViewModel(@NonNull Application application) {
        super(application);
        repositorio = NotasRepository.get(application);
    }

    public LiveData<List<Nota>> getNotas() {
        return notas;
    }

    public void cargar() {
        repositorio.notasPapelera(new NotasRepository.Callback<List<Nota>>() {
            @Override
            public void onResult(List<Nota> valor) {
                notas.setValue(valor);
            }
        });
    }

    public void restaurar(int id) {
        repositorio.restaurarNota(id, new Runnable() {
            @Override
            public void run() {
                cargar();
            }
        });
    }

    public void borrarDefinitivamente(int id) {
        repositorio.borrarNotaDefinitivamente(id, new Runnable() {
            @Override
            public void run() {
                cargar();
            }
        });
    }
}
