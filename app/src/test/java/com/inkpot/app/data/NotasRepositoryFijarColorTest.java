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
 * Comprueba que las notas fijadas y el color de nota se guardan y se ordenan.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class NotasRepositoryFijarColorTest {
    private NotasRepository repositorio;
    private INotaDAO notaDAO;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        NotasRepository.resetParaTests();
        NotasDatabase.resetParaTests();
        NotasDatabase.permitirConsultasEnMainThreadParaTests();
        NotasRepository.modoSincronoParaTests();
        repositorio = NotasRepository.get(context);
        notaDAO = FactoryDAO.getFactory(FactoryDAO.ROOM_FACTORY).getNotaDao(context);
    }

    @Test
    public void fijarNota_seGuarda() {
        int id = notaDAO.createNota("Nota", "x");

        repositorio.fijarNota(id, true, null);

        assertTrue(notaDAO.getNota(id).isFijada());
    }

    @Test
    public void desfijarNota_seGuarda() {
        int id = notaDAO.createNota("Nota", "x");
        repositorio.fijarNota(id, true, null);

        repositorio.fijarNota(id, false, null);

        assertFalse(notaDAO.getNota(id).isFijada());
    }

    @Test
    public void cambiarColor_seGuarda() {
        int id = notaDAO.createNota("Nota", "x");

        repositorio.cambiarColorNota(id, 3, null);

        assertEquals(3, notaDAO.getNota(id).getColor());
    }

    @Test
    public void lasNotasFijadasSalenPrimero() {
        notaDAO.createNota("Primera", "x");
        int segunda = notaDAO.createNota("Segunda", "y");

        notaDAO.setFijada(segunda, 1);

        List<Nota> notas = new ArrayList<>();
        notaDAO.getAllNotas(notas);
        assertEquals("Segunda", notas.get(0).getTitulo());
    }
}
