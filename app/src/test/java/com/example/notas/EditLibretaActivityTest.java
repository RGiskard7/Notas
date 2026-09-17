package com.example.notas;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.Intent;
import android.widget.EditText;

import androidx.test.core.app.ApplicationProvider;

import com.example.notas.data.FactoryDAO;
import com.example.notas.data.ILibretaDAO;
import com.example.notas.data.Libreta;
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
public class EditLibretaActivityTest {
    private Context context;
    private ILibretaDAO libretaDAO;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        NotasRepository.resetParaTests();
        NotasDatabase.resetParaTests();
        NotasDatabase.permitirConsultasEnMainThreadParaTests();
        NotasRepository.modoSincronoParaTests();
        context.deleteDatabase("DBNevernote");
        libretaDAO = FactoryDAO.getFactory(FactoryDAO.ROOM_FACTORY).getLibretaDao(context);
    }

    @After
    public void tearDown() {
        libretaDAO.closeDB();
        NotasRepository.resetParaTests();
        NotasDatabase.resetParaTests();
        context.deleteDatabase("DBNevernote");
    }

    private EditLibretaActivity lanzarNueva() {
        Intent intent = new Intent(context, EditLibretaActivity.class);
        intent.putExtra("tipo", "nueva");
        return Robolectric.buildActivity(EditLibretaActivity.class, intent).setup().get();
    }

    private EditLibretaActivity lanzarEdicion(Libreta libreta) {
        Intent intent = new Intent(context, EditLibretaActivity.class);
        intent.putExtra("libreta", libreta);
        intent.putExtra("tipo", "editable");
        return Robolectric.buildActivity(EditLibretaActivity.class, intent).setup().get();
    }

    private int numLibretas() {
        List<Libreta> libretas = new ArrayList<>();
        libretaDAO.getAllLibretas(libretas);
        return libretas.size();
    }

    private boolean existe(String titulo) {
        return libretaDAO.existTitulo(titulo);
    }

    @Test
    public void crearLibreta_laPersiste() {
        EditLibretaActivity activity = lanzarNueva();

        ((EditText) activity.findViewById(R.id.editTextTituloLibreta)).setText("Trabajo");
        shadowOf(activity).clickMenuItem(R.id.action_guardar2);

        assertEquals(2, numLibretas());
        assertEquals(true, existe("Trabajo"));
    }

    @Test
    public void crearLibreta_duplicada_noLaDuplica() {
        libretaDAO.createLibreta("Trabajo");
        EditLibretaActivity activity = lanzarNueva();

        ((EditText) activity.findViewById(R.id.editTextTituloLibreta)).setText("Trabajo");
        shadowOf(activity).clickMenuItem(R.id.action_guardar2);

        assertEquals(2, numLibretas());
    }

    @Test
    public void editarLibreta_cambiaElTitulo() {
        libretaDAO.createLibreta("Vieja");
        Libreta vieja = null;
        List<Libreta> libretas = new ArrayList<>();
        libretaDAO.getAllLibretas(libretas);
        for (Libreta libreta : libretas) {
            if (libreta.getTitulo().equals("Vieja")) {
                vieja = libreta;
            }
        }

        EditLibretaActivity activity = lanzarEdicion(vieja);
        assertEquals("Vieja", ((EditText) activity.findViewById(R.id.editTextTituloLibreta)).getText().toString());

        ((EditText) activity.findViewById(R.id.editTextTituloLibreta)).setText("Nueva");
        shadowOf(activity).clickMenuItem(R.id.action_guardar2);

        assertEquals(true, existe("Nueva"));
        assertEquals(false, existe("Vieja"));
    }
}
