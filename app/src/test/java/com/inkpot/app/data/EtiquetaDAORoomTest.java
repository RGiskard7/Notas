package com.inkpot.app.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.inkpot.app.data.room.EtiquetaDAORoom;
import com.inkpot.app.data.room.EtiquetaEntity;
import com.inkpot.app.data.room.LibretaDAORoom;
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
public class EtiquetaDAORoomTest {
    private static final String DB_NAME = "test_etiquetas_room";
    private Context context;
    private EtiquetaDAORoom etiquetaDAO;
    private NotaDAORoom notaDAO;
    private LibretaDAORoom libretaDAO;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        NotasDatabase.resetParaTests();
        NotasDatabase.permitirConsultasEnMainThreadParaTests();
        context.deleteDatabase(DB_NAME);
        etiquetaDAO = new EtiquetaDAORoom(context, DB_NAME);
        notaDAO = new NotaDAORoom(context, DB_NAME);
        libretaDAO = new LibretaDAORoom(context, DB_NAME);
    }

    @After
    public void tearDown() {
        etiquetaDAO.closeDB();
        notaDAO.closeDB();
        libretaDAO.closeDB();
        context.deleteDatabase(DB_NAME);
    }

    private Etiqueta buscarPorTitulo(String titulo) {
        List<Etiqueta> etiquetas = new ArrayList<>();
        etiquetaDAO.getAllEtiquetas(etiquetas);
        for (Etiqueta etiqueta : etiquetas) {
            if (etiqueta.getTitulo().equals(titulo)) {
                return etiqueta;
            }
        }
        return null;
    }

    private int nuevaNotaEnLibretaDefault(String titulo) {
        int idNota = notaDAO.createNota(titulo, "texto");
        libretaDAO.addNotaToLibreta(1, idNota);
        return idNota;
    }

    @Test
    public void createEtiqueta_laAniadeAlListado() {
        etiquetaDAO.createEtiqueta("Urgente");

        List<Etiqueta> etiquetas = new ArrayList<>();
        etiquetaDAO.getAllEtiquetas(etiquetas);

        assertEquals(1, etiquetas.size());
        assertEquals("Urgente", etiquetas.get(0).getTitulo());
    }

    @Test
    public void existTitulo_distingueExistenteDeInexistente() {
        etiquetaDAO.createEtiqueta("Urgente");

        assertTrue(etiquetaDAO.existTitulo("Urgente"));
        assertFalse(etiquetaDAO.existTitulo("Otra"));
    }

    @Test
    public void existTitulo_conApostrofo_detectaElDuplicado() {
        etiquetaDAO.createEtiqueta("O'Brien");

        assertTrue(etiquetaDAO.existTitulo("O'Brien"));
    }

    @Test
    public void editEtiqueta_conservaFechaCreacion_yActualizaModificacion() {
        etiquetaDAO.createEtiqueta("Vieja");
        Etiqueta creada = buscarPorTitulo("Vieja");

        EtiquetaEntity entity = NotasDatabase.get(context, DB_NAME).etiquetaDao().getEtiquetaById(creada.getId());
        entity.fechaCreacion = 946684800000L; // 01/01/2000 00:00 UTC
        NotasDatabase.get(context, DB_NAME).etiquetaDao().updateEtiqueta(entity);

        etiquetaDAO.editEtiqueta(creada.getId(), "Nueva");

        Etiqueta editada = buscarPorTitulo("Nueva");
        assertEquals(creada.getId(), editada.getId());
        assertEquals(946684800000L, editada.getFechaCreacion());
        assertTrue(editada.getFechaModificacion() > 0);
    }

    @Test
    public void deleteEtiqueta_eliminaVinculosPeroNoLaNota() {
        etiquetaDAO.createEtiqueta("Temporal");
        Etiqueta temporal = buscarPorTitulo("Temporal");
        int idNota = nuevaNotaEnLibretaDefault("Nota");
        List<Etiqueta> etiquetas = new ArrayList<>();
        etiquetas.add(temporal);
        notaDAO.addEtiquetasToNota(idNota, etiquetas);

        etiquetaDAO.deleteEtiqueta(temporal.getId());

        assertNull(etiquetaDAO.getEtiqueta(temporal.getId()));
        assertNotNull(notaDAO.getNota(idNota));
        List<Etiqueta> restantes = new ArrayList<>();
        notaDAO.getAllEtiquetasFrom(idNota, restantes);
        assertTrue(restantes.isEmpty());
    }

    @Test
    public void getAllEtiquetas_incluyeElRecuentoDeNotas() {
        etiquetaDAO.createEtiqueta("Marcada");
        Etiqueta etiqueta = buscarPorTitulo("Marcada");
        int idNota = nuevaNotaEnLibretaDefault("Nota");
        List<Etiqueta> etiquetas = new ArrayList<>();
        etiquetas.add(etiqueta);
        notaDAO.addEtiquetasToNota(idNota, etiquetas);

        Etiqueta recargada = buscarPorTitulo("Marcada");

        assertEquals(1, recargada.getNumNotas());
    }

    @Test
    public void getAllNotasFrom_devuelveLasNotasConEsaEtiqueta() {
        etiquetaDAO.createEtiqueta("Marcada");
        Etiqueta marcada = buscarPorTitulo("Marcada");

        int idConEtiqueta = nuevaNotaEnLibretaDefault("Con etiqueta");
        int idSinEtiqueta = nuevaNotaEnLibretaDefault("Sin etiqueta");
        List<Etiqueta> etiquetas = new ArrayList<>();
        etiquetas.add(marcada);
        notaDAO.addEtiquetasToNota(idConEtiqueta, etiquetas);

        List<Nota> notas = new ArrayList<>();
        etiquetaDAO.getAllNotasFrom(marcada.getId(), notas);

        assertEquals(1, notas.size());
        assertEquals("Con etiqueta", notas.get(0).getTitulo());
        assertEquals(1, notas.get(0).getLibreta().getId());
    }
}
