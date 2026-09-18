package com.inkpot.app.UI;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.inkpot.app.data.Libreta;
import com.inkpot.app.data.NotasRepository;

import java.util.List;

/**
 * ViewModel del listado de libretas.
 */
public class ListLibretasViewModel extends AndroidViewModel {
    private static final String CLAVE_CONSULTA = "consulta";

    private final NotasRepository repositorio;
    private final SavedStateHandle estado;
    private final MutableLiveData<List<Libreta>> libretas = new MutableLiveData<>();

    public ListLibretasViewModel(@NonNull Application application, SavedStateHandle estado) {
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

    /** Renombra la libreta si no hay otra con ese título; avisa por callback. */
    public void editarSiNoExiste(final int id, final String titulo, final NotasRepository.Callback<Boolean> callback) {
        repositorio.editarLibretaSiNoExiste(id, titulo, new NotasRepository.Callback<Boolean>() {
            @Override
            public void onResult(Boolean editada) {
                if (Boolean.TRUE.equals(editada)) {
                    cargar();
                }
                callback.onResult(editada);
            }
        });
    }
}
