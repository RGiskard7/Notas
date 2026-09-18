package com.example.notas.UI;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.example.notas.data.Etiqueta;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;
import com.example.notas.data.NotasRepository;
import com.example.notas.util.ConsultaFts;

import java.util.List;

/**
 * ViewModel del listado de notas.
 *
 * <p>Recuerda el ámbito desde el que se está listando (todas, una libreta o una
 * etiqueta) y el texto de búsqueda, y expone las notas como {@code LiveData},
 * de modo que la vista se actualiza sola al cambiar los datos.</p>
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

    /** Lista todas las notas (sin ámbito). */
    public void cargarTodas() {
        idLibreta = -1;
        idEtiqueta = -1;
        buscarInterno();
    }

    /** Lista las notas de una libreta. */
    public void cargarDeLibreta(int id) {
        idLibreta = id;
        idEtiqueta = -1;
        buscarInterno();
    }

    /** Lista las notas de una etiqueta. */
    public void cargarDeEtiqueta(int id) {
        idEtiqueta = id;
        idLibreta = -1;
        buscarInterno();
    }

    /**
     * Cambia el texto de búsqueda. Si está vacío se listan todas las notas del
     * ámbito actual; si no, se busca por título y contenido.
     */
    public void buscar(String consulta) {
        setConsulta(consulta);
        buscarInterno();
    }

    /** Vuelve a cargar con el ámbito y la búsqueda actuales. */
    public void recargar() {
        buscarInterno();
    }

    public void eliminar(int id) {
        repositorio.eliminarNota(id, new Runnable() {
            @Override
            public void run() {
                recargar();
            }
        });
    }

    /** Fija o desfija una nota. */
    public void fijar(int id, boolean fijada) {
        repositorio.fijarNota(id, fijada, new Runnable() {
            @Override
            public void run() {
                recargar();
            }
        });
    }

    /** Mueve una nota a otra libreta. */
    public void mover(int id, int idLibretaVieja, int idLibretaNueva) {
        repositorio.moverNota(id, idLibretaVieja, idLibretaNueva, new Runnable() {
            @Override
            public void run() {
                recargar();
            }
        });
    }

    /** Añade etiquetas a una nota. */
    public void etiquetar(int id, List<Etiqueta> etiquetas) {
        repositorio.anadirEtiquetasNota(id, etiquetas, new Runnable() {
            @Override
            public void run() {
                recargar();
            }
        });
    }

    /** Carga todas las libretas (para mover notas). */
    public void libretas(NotasRepository.Callback<List<Libreta>> callback) {
        repositorio.libretas(callback);
    }

    /** Carga todas las etiquetas (para etiquetar notas). */
    public void etiquetas(NotasRepository.Callback<List<Etiqueta>> callback) {
        repositorio.etiquetas(callback);
    }

    /** Saca una nota de la papelera (para deshacer un borrado). */
    public void restaurar(int id) {
        repositorio.restaurarNota(id, new Runnable() {
            @Override
            public void run() {
                recargar();
            }
        });
    }

    private void buscarInterno() {
        String consulta = ConsultaFts.paraMatch(getConsulta());
        NotasRepository.Callback<List<Nota>> callback = new NotasRepository.Callback<List<Nota>>() {
            @Override
            public void onResult(List<Nota> valor) {
                notas.setValue(valor);
            }
        };

        if (consulta.isEmpty()) {
            if (idLibreta != -1) {
                repositorio.notasDeLibreta(idLibreta, callback);
            } else if (idEtiqueta != -1) {
                repositorio.notasDeEtiqueta(idEtiqueta, callback);
            } else {
                repositorio.notasTodas(callback);
            }
        } else {
            repositorio.buscarNotas(consulta, idLibreta, idEtiqueta, callback);
        }
    }
}
