package com.inkpot.app.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.inkpot.app.data.room.LibretaDAORoom;
import com.inkpot.app.data.room.LibretaEntity;
import com.inkpot.app.data.room.NotaDAORoom;
import com.inkpot.app.data.room.NotasDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class LibretaDAORoomTest {
    private static final String DB_NAME = "test_libretas_room";
    private Context context;
    private LibretaDAORoom libretaDAO;
    private NotaDAORoom notaDAO;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        NotasDatabase.resetParaTests();
        NotasDatabase.permitirConsultasEnMainThreadParaTests();
        context.deleteDatabase(DB_NAME);
        libretaDAO = new LibretaDAORoom(context, DB_NAME);
        notaDAO = new NotaDAORoom(context, DB_NAME);
    }

    @After
    public void tearDown() {
        libretaDAO.closeDB();
        notaDAO.closeDB();
        context.deleteDatabase(DB_NAME);
    }

    private Libreta buscarPorTitulo(String titulo) {
        List<Libreta> libretas = new ArrayList<>();
        libretaDAO.getAllLibretas(libretas);
        for (Libreta libreta : libretas) {
            if (libreta.getTitulo().equals(titulo)) {
                return libreta;
            }
        }
        return null;
    }

    @Test
    public void onCreate_insertaLaLibretaDefault() {
        List<Libreta> libretas = new ArrayList<>();
        libretaDAO.getAllLibretas(libretas);

        assertEquals(1, libretas.size());
        assertEquals(1, libretas.get(0).getId());
        assertEquals("Default", libretas.get(0).getTitulo());
    }

    @Test
    public void createLibreta_laAniadeAlListado() {
        libretaDAO.createLibreta("Trabajo");

        List<Libreta> libretas = new ArrayList<>();
        libretaDAO.getAllLibretas(libretas);

        assertEquals(2, libretas.size());
        assertNotNull(buscarPorTitulo("Trabajo"));
    }

    @Test
    public void existTitulo_distingueExistenteDeInexistente() {
        libretaDAO.createLibreta("Trabajo");

        assertTrue(libretaDAO.existTitulo("Trabajo"));
        assertFalse(libretaDAO.existTitulo("Personal"));
    }

    @Test
    public void existTitulo_conApostrofo_detectaElDuplicado() {
        libretaDAO.createLibreta("O'Brien");

        assertTrue(libretaDAO.existTitulo("O'Brien"));
    }

    @Test
    public void createLibreta_duplicada_lanzaSQLiteConstraintException() {
        libretaDAO.createLibreta("Unica");
        try {
            libretaDAO.createLibreta("Unica");
            org.junit.Assert.fail("Se esperaba SQLiteConstraintException");
        } catch (android.database.sqlite.SQLiteConstraintException esperada) {
            // comportamiento esperado
        }
    }

    @Test
    public void editLibreta_conservaFechaCreacion_yActualizaModificacion() {
        libretaDAO.createLibreta("Vieja");
        Libreta creada = buscarPorTitulo("Vieja");

        LibretaEntity entity = NotasDatabase.get(context, DB_NAME).libretaDao().getLibretaById(creada.getId());
        entity.fechaCreacion = 946684800000L; // 01/01/2000 00:00 UTC
        NotasDatabase.get(context, DB_NAME).libretaDao().updateLibreta(entity);

        libretaDAO.editLibreta(creada.getId(), "Nueva");

        Libreta editada = buscarPorTitulo("Nueva");
        assertEquals(creada.getId(), editada.getId());
        assertEquals(946684800000L, editada.getFechaCreacion());
        assertTrue(editada.getFechaModificacion() > 0);
    }

    @Test
    public void deleteLibreta_eliminaVinculosPeroNoLasNotas() {
        libretaDAO.createLibreta("Temporal");
        Libreta temporal = buscarPorTitulo("Temporal");
        int idNota = notaDAO.createNota("Nota hija", "texto");
        libretaDAO.addNotaToLibreta(temporal.getId(), idNota);

        libretaDAO.deleteLibreta(temporal.getId());

        assertNull(libretaDAO.getLibreta(temporal.getId()));
        assertNotNull(notaDAO.getNota(idNota));
        assertNull(notaDAO.getLibreta(idNota));
    }

    @Test
    public void getLibreta_incluyeElRecuentoDeNotas() {
        libretaDAO.createLibreta("Con notas");
        Libreta libreta = buscarPorTitulo("Con notas");
        int idNota = notaDAO.createNota("N", "x");
        libretaDAO.addNotaToLibreta(libreta.getId(), idNota);

        Libreta recargada = libretaDAO.getLibreta(libreta.getId());

        assertEquals(1, recargada.getNumNotas());
    }

    @Test
    public void getAllLibretas_incluyeElRecuentoDeNotas() {
        libretaDAO.createLibreta("Con notas");
        Libreta libreta = buscarPorTitulo("Con notas");
        int idNota1 = notaDAO.createNota("N1", "x");
        libretaDAO.addNotaToLibreta(libreta.getId(), idNota1);
        int idNota2 = notaDAO.createNota("N2", "y");
        libretaDAO.addNotaToLibreta(libreta.getId(), idNota2);

        Libreta recargada = buscarPorTitulo("Con notas");

        assertEquals(2, recargada.getNumNotas());
    }

    @Test
    public void getAllNotasFrom_devuelveSoloLasNotasDeLaLibreta() {
        libretaDAO.createLibreta("Destino");
        Libreta destino = buscarPorTitulo("Destino");

        int idA = notaDAO.createNota("A", "a");
        libretaDAO.addNotaToLibreta(1, idA);

        int idB = notaDAO.createNota("B", "b");
        libretaDAO.addNotaToLibreta(destino.getId(), idB);

        int idC = notaDAO.createNota("C", "c");
        libretaDAO.addNotaToLibreta(destino.getId(), idC);

        List<Nota> notas = new ArrayList<>();
        libretaDAO.getAllNotasFrom(destino.getId(), notas);

        assertEquals(2, notas.size());
        assertEquals("B", notas.get(0).getTitulo());
        assertEquals("C", notas.get(1).getTitulo());
    }
}
