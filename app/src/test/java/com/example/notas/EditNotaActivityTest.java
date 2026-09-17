package com.example.notas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.test.core.app.ApplicationProvider;

import com.example.notas.data.FactoryDAO;
import com.example.notas.data.ILibretaDAO;
import com.example.notas.data.INotaDAO;
import com.example.notas.data.Nota;
import com.example.notas.data.NotasRepository;
import com.example.notas.data.room.NotasDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class EditNotaActivityTest {
    private Context context;
    private INotaDAO notaDAO;
    private ILibretaDAO libretaDAO;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        NotasRepository.resetParaTests();
        NotasDatabase.resetParaTests();
        NotasDatabase.permitirConsultasEnMainThreadParaTests();
        NotasRepository.modoSincronoParaTests();
        FactoryDAO factory = FactoryDAO.getFactory(FactoryDAO.ROOM_FACTORY);
        notaDAO = factory.getNotaDao(context);
        libretaDAO = factory.getLibretaDao(context);
    }

    @After
    public void tearDown() {
        notaDAO.closeDB();
        libretaDAO.closeDB();
    }

    private EditNotaActivity lanzarNueva() {
        Intent intent = new Intent(context, EditNotaActivity.class);
        intent.putExtra("tipo", "nueva");
        return Robolectric.buildActivity(EditNotaActivity.class, intent).setup().get();
    }

    private EditNotaActivity lanzarEdicion(Nota nota) {
        Intent intent = new Intent(context, EditNotaActivity.class);
        intent.putExtra("nota", nota);
        intent.putExtra("tipo", "editable");
        return Robolectric.buildActivity(EditNotaActivity.class, intent).setup().get();
    }

    private void seleccionarPrimeraLibreta(EditNotaActivity activity) {
        Spinner spinner = activity.findViewById(R.id.spinnerOpcionLibretas);
        spinner.setSelection(0, true);
    }

    @Test
    public void crearNota_laPersisteEnLaLibretaSeleccionada() {
        EditNotaActivity activity = lanzarNueva();
        seleccionarPrimeraLibreta(activity);

        ((EditText) activity.findViewById(R.id.editTextTituloNwNota)).setText("Compra");
        ((EditText) activity.findViewById(R.id.editTextContenidoNwNota)).setText("Leche");
        shadowOf(activity).clickMenuItem(R.id.action_guardar);

        List<Nota> notas = new ArrayList<>();
        notaDAO.getAllNotas(notas);

        assertEquals(1, notas.size());
        assertEquals("Compra", notas.get(0).getTitulo());
        assertEquals("Leche", notas.get(0).getTexto());
        assertEquals(1, notas.get(0).getLibreta().getId());
    }

    @Test
    public void notaVacia_noSeGuarda() {
        EditNotaActivity activity = lanzarNueva();
        seleccionarPrimeraLibreta(activity);

        shadowOf(activity).clickMenuItem(R.id.action_guardar);

        List<Nota> notas = new ArrayList<>();
        notaDAO.getAllNotas(notas);
        assertTrue(notas.isEmpty());
    }

    @Test
    public void editarNota_cambiaTituloYConservaLaFechaDeCreacion() {
        int id = notaDAO.createNota("Original", "texto");
        libretaDAO.addNotaToLibreta(1, id);
        Nota original = notaDAO.getNota(id);

        EditNotaActivity activity = lanzarEdicion(original);

        assertEquals("Original", ((EditText) activity.findViewById(R.id.editTextTituloNwNota)).getText().toString());

        ((EditText) activity.findViewById(R.id.editTextTituloNwNota)).setText("Editada");
        shadowOf(activity).clickMenuItem(R.id.action_guardar);

        Nota editada = notaDAO.getNota(id);
        assertEquals("Editada", editada.getTitulo());
        assertEquals(original.getFechaCreacion(), editada.getFechaCreacion());
    }
}
