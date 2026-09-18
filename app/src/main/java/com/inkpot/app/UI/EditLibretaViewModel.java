package com.inkpot.app.UI;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.inkpot.app.data.NotasRepository;

/**
 * ViewModel de la pantalla de crear o editar una libreta.
 */
public class EditLibretaViewModel extends AndroidViewModel {
    private final NotasRepository repositorio;

    public EditLibretaViewModel(@NonNull Application application) {
        super(application);
        repositorio = NotasRepository.get(application);
    }

    public void crearSiNoExiste(String titulo, NotasRepository.Callback<Boolean> callback) {
        repositorio.crearLibretaSiNoExiste(titulo, callback);
    }

    public void editar(int id, String titulo, Runnable onDone) {
        repositorio.editarLibreta(id, titulo, onDone);
    }
}
