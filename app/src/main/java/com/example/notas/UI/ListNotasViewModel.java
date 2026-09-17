package com.example.notas.UI;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.example.notas.data.Nota;
import com.example.notas.data.NotasRepository;

import java.util.List;

/**
 * ViewModel del listado de notas.
 *
 * <p>Recuerda el ámbito desde el que se está listando (todas, una libreta o una
 * etiqueta) y expone las notas como {@code LiveData}, de modo que la vista se
 * actualiza sola al cambiar los datos.</p>
 */
public class ListNotasViewModel extends AndroidViewModel {
    private static final String CLAVE_CONSULTA = "consulta";

    private final NotasRepository repositorio;
    private final SavedStateHandle estado;
    private final MutableLiveData<List<Nota>> notas = new MutableLiveData<>();
    private int idLibreta = -1;
    private int idEtiqueta = -1;

    public ListNotasViewModel(@NonNull Application application, SavedStateHandle estado) {
        super(application);
        this.estado = estado;
        repositorio = NotasRepository.get(application);
    }

    /** Texto de búsqueda actual, conservado aunque se recree la pantalla. */
    public String getConsulta() {
        String consulta = estado.get(CLAVE_CONSULTA);
        return consulta == null ? "" : consulta;
    }

    public void setConsulta(String consulta) {
        estado.set(CLAVE_CONSULTA, consulta);
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
