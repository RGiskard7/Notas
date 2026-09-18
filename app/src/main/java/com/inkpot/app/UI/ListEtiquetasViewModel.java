package com.inkpot.app.UI;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.inkpot.app.data.Etiqueta;
import com.inkpot.app.data.NotasRepository;

import java.util.List;

/**
 * ViewModel del listado de etiquetas.
 */
public class ListEtiquetasViewModel extends AndroidViewModel {
    private static final String CLAVE_CONSULTA = "consulta";

    private final NotasRepository repositorio;
    private final SavedStateHandle estado;
    private final MutableLiveData<List<Etiqueta>> etiquetas = new MutableLiveData<>();

    public ListEtiquetasViewModel(@NonNull Application application, SavedStateHandle estado) {
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
