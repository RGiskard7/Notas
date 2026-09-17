package com.example.notas.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.notas.data.room.EtiquetaDAORoom;
import com.example.notas.data.room.LibretaDAORoom;
import com.example.notas.data.room.NotaDAORoom;
import com.example.notas.data.room.NotaEntity;
import com.example.notas.data.room.NotasDatabase;

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
public class NotaDAORoomTest {
    private static final String DB_NAME = "test_notas_room";
    private Context context;
    private NotaDAORoom notaDAO;
    private LibretaDAORoom libretaDAO;
    private EtiquetaDAORoom etiquetaDAO;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        NotasDatabase.resetParaTests();
        NotasDatabase.permitirConsultasEnMainThreadParaTests();
        context.deleteDatabase(DB_NAME);
        notaDAO = new NotaDAORoom(context, DB_NAME);
        libretaDAO = new LibretaDAORoom(context, DB_NAME);
        etiquetaDAO = new EtiquetaDAORoom(context, DB_NAME);
    }

    @After
    public void tearDown() {
        notaDAO.closeDB();
        libretaDAO.closeDB();
        etiquetaDAO.closeDB();
        context.deleteDatabase(DB_NAME);
    }

    private int nuevaNotaEnLibretaDefault(String titulo, String texto) {
        int idNota = notaDAO.createNota(titulo, texto);
        libretaDAO.addNotaToLibreta(1, idNota);
        return idNota;
    }

    private List<Etiqueta> todasLasEtiquetas() {
        List<Etiqueta> etiquetas = new ArrayList<>();
        etiquetaDAO.getAllEtiquetas(etiquetas);
        return etiquetas;
    }

    @Test
    public void createNota_devuelveIdYGuardaTituloYTexto() {
        int id = nuevaNotaEnLibretaDefault("Mi titulo", "Mi texto");
        assertTrue(id > 0);

        List<Nota> notas = new ArrayList<>();
        notaDAO.getAllNotas(notas);

        assertEquals(1, notas.size());
        assertEquals(id, notas.get(0).getId());
        assertEquals("Mi titulo", notas.get(0).getTitulo());
        assertEquals("Mi texto", notas.get(0).getTexto());
    }

    @Test
    public void getAllNotas_devuelveLaFechaConFormato() {
        nuevaNotaEnLibretaDefault("T", "X");

        List<Nota> notas = new ArrayList<>();
        notaDAO.getAllNotas(notas);

        assertNotNull(notas.get(0).getFechaCreacion());
        assertTrue(notas.get(0).getFechaCreacion().matches("\\d{2}/\\d{2}/\\d{4} - \\d{2}:\\d{2}"));
    }

    @Test
    public void getNota_devuelveLaFechaDeCreacion() {
        int id = nuevaNotaEnLibretaDefault("T", "X");

        Nota nota = notaDAO.getNota(id);

        assertEquals("T", nota.getTitulo());
        assertEquals("X", nota.getTexto());
        assertNotNull(nota.getFechaCreacion());
        assertTrue(nota.getFechaCreacion().matches("\\d{2}/\\d{2}/\\d{4} - \\d{2}:\\d{2}"));
    }

    @Test
    public void getLibreta_devuelveLaLibretaVinculada() {
        int id = nuevaNotaEnLibretaDefault("T", "X");

        Libreta libreta = notaDAO.getLibreta(id);

        assertEquals(1, libreta.getId());
        assertEquals("Default", libreta.getTitulo());
    }

    @Test
    public void getNota_deNotaSinLibreta_devuelveLibretaNull() {
        int id = notaDAO.createNota("Sin libreta", "x");

        Nota nota = notaDAO.getNota(id);

        assertNotNull(nota);
        assertNull(nota.getLibreta());
    }

    @Test
    public void getNota_deIdInexistente_devuelveNull() {
        assertNull(notaDAO.getNota(999));
    }

    @Test
    public void editNota_conservaFechaCreacion_yActualizaModificacion() {
        int id = nuevaNotaEnLibretaDefault("Original", "texto");

        NotaEntity entity = NotasDatabase.get(context, DB_NAME).notaDao().getNotaById(id);
        entity.fechaCreacion = "01/01/2000 - 00:00";
        NotasDatabase.get(context, DB_NAME).notaDao().updateNota(entity);

        notaDAO.editNota(id, "Editado", "nuevo");

        Nota nota = notaDAO.getNota(id);
        assertEquals("Editado", nota.getTitulo());
        assertEquals("nuevo", nota.getTexto());
        assertEquals("01/01/2000 - 00:00", nota.getFechaCreacion());
        assertNotNull(nota.getFechaModificacion());
        assertTrue(nota.getFechaModificacion().matches("\\d{2}/\\d{2}/\\d{4} - \\d{2}:\\d{2}"));
    }

    @Test
    public void laLibretaDeUnaNota_llevaElRecuentoDeNotas() {
        nuevaNotaEnLibretaDefault("A", "x");
        nuevaNotaEnLibretaDefault("B", "y");

        List<Nota> notas = new ArrayList<>();
        notaDAO.getAllNotas(notas);

        assertEquals(2, notas.get(0).getLibreta().getNumNotas());
    }

    @Test
    public void lasEtiquetasDeUnaNota_llevanElRecuentoDeNotas() {
        int id = nuevaNotaEnLibretaDefault("A", "x");
        etiquetaDAO.createEtiqueta("E");
        notaDAO.addEtiquetasToNota(id, todasLasEtiquetas());

        Nota nota = notaDAO.getNota(id);
        Etiqueta etiqueta = nota.getEtiquetas().iterator().next();

        assertEquals(1, etiqueta.getNumNotas());
    }

    @Test
    public void deleteNota_eliminaLosVinculos() {
        int id = nuevaNotaEnLibretaDefault("T", "X");
        etiquetaDAO.createEtiqueta("E");
        notaDAO.addEtiquetasToNota(id, todasLasEtiquetas());

        notaDAO.deleteNota(id);

        assertNull(notaDAO.getNota(id));
        assertNull(notaDAO.getLibreta(id));
        List<Etiqueta> restantes = new ArrayList<>();
        notaDAO.getAllEtiquetasFrom(id, restantes);
        assertTrue(restantes.isEmpty());
    }

    @Test
    public void deletedEtiquetasFromNota_borraSoloLaEtiquetaIndicada() {
        int id = nuevaNotaEnLibretaDefault("T", "X");
        etiquetaDAO.createEtiqueta("A");
        etiquetaDAO.createEtiqueta("B");
        notaDAO.addEtiquetasToNota(id, todasLasEtiquetas());

        List<Etiqueta> quitar = new ArrayList<>();
        quitar.add(todasLasEtiquetas().get(0));
        notaDAO.deletedEtiquetasFromNota(id, quitar);

        List<Etiqueta> restantes = new ArrayList<>();
        notaDAO.getAllEtiquetasFrom(id, restantes);

        assertEquals(1, restantes.size());
        assertEquals("B", restantes.get(0).getTitulo());
    }
}
