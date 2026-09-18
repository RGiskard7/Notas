package com.inkpot.app.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.inkpot.app.data.room.NotasDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprueba la creación y el renombrado de libretas sin títulos repetidos.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class NotasRepositoryLibretasTest {
    private NotasRepository repositorio;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        NotasRepository.resetParaTests();
        NotasDatabase.resetParaTests();
        NotasDatabase.permitirConsultasEnMainThreadParaTests();
        NotasRepository.modoSincronoParaTests();
        repositorio = NotasRepository.get(context);
    }

    private boolean crearLibreta(String titulo) {
        final boolean[] resultado = new boolean[1];
        repositorio.crearLibretaSiNoExiste(titulo, new NotasRepository.Callback<Boolean>() {
            @Override
            public void onResult(Boolean valor) {
                resultado[0] = valor;
            }
        });
        return resultado[0];
    }

    private boolean renombrar(int id, String titulo) {
        final boolean[] resultado = new boolean[1];
        repositorio.editarLibretaSiNoExiste(id, titulo, new NotasRepository.Callback<Boolean>() {
            @Override
            public void onResult(Boolean valor) {
                resultado[0] = valor;
            }
        });
        return resultado[0];
    }

    private List<Libreta> libretas() {
        final List<Libreta> resultado = new ArrayList<>();
        repositorio.libretas(new NotasRepository.Callback<List<Libreta>>() {
            @Override
            public void onResult(List<Libreta> valor) {
                resultado.addAll(valor);
            }
        });
        return resultado;
    }

    private int idDe(String titulo) {
        for (Libreta libreta : libretas()) {
            if (libreta.getTitulo().equals(titulo)) {
                return libreta.getId();
            }
        }
        throw new IllegalStateException("No existe la libreta " + titulo);
    }

    private String tituloDe(int id) {
        for (Libreta libreta : libretas()) {
            if (libreta.getId() == id) {
                return libreta.getTitulo();
            }
        }
        throw new IllegalStateException("No existe la libreta con id " + id);
    }

    @Test
    public void crearLibreta_conTituloNuevo_laCrea() {
        assertTrue(crearLibreta("Trabajo"));
    }

    @Test
    public void crearLibreta_conTituloRepetido_noLaCrea() {
        assertTrue(crearLibreta("Trabajo"));

        assertFalse(crearLibreta("Trabajo"));
    }

    @Test
    public void editarLibreta_conTituloLibre_laRenombra() {
        assertTrue(crearLibreta("Trabajo"));
        int id = idDe("Trabajo");

        assertTrue(renombrar(id, "Personal"));
        assertEquals("Personal", tituloDe(id));
    }

    @Test
    public void editarLibreta_conTituloRepetido_noLaRenombra() {
        assertTrue(crearLibreta("Trabajo"));
        assertTrue(crearLibreta("Personal"));
        int id = idDe("Trabajo");

        assertFalse(renombrar(id, "Personal"));
        assertEquals("Trabajo", tituloDe(id));
    }
}
