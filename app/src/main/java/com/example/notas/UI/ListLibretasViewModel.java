package com.example.notas.UI;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.notas.data.Libreta;
import com.example.notas.data.NotasRepository;

import java.util.List;

public class ListLibretasViewModel extends AndroidViewModel {
    private final NotasRepository repositorio;
    private final MutableLiveData<List<Libreta>> libretas = new MutableLiveData<>();

    public ListLibretasViewModel(@NonNull Application application) {
        super(application);
        repositorio = NotasRepository.get(application);
    }

    public LiveData<List<Libreta>> getLibretas() {
        return libretas;
    }

    public void cargar() {
        repositorio.libretas(new NotasRepository.Callback<List<Libreta>>() {
            @Override
            public void onResult(List<Libreta> valor) {
                libretas.setValue(valor);
            }
        });
    }

    public void eliminar(int id) {
        repositorio.eliminarLibreta(id, new Runnable() {
            @Override
            public void run() {
                cargar();
            }
        });
    }
}
