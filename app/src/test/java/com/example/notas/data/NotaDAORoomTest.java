package com.example.notas.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.notas.data.room.EtiquetaDAORoom;import com.example.notas.data.room.LibretaDAORoom;
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
    public void getAllNotas_devuelveLaFechaDeCreacion() {
        nuevaNotaEnLibretaDefault("T", "X");

        List<Nota> notas = new ArrayList<>();
        notaDAO.getAllNotas(notas);

        assertTrue(notas.get(0).getFechaCreacion() > 0);
    }

    @Test
    public void getNota_devuelveLaFechaDeCreacion() {
        int id = nuevaNotaEnLibretaDefault("T", "X");

        Nota nota = notaDAO.getNota(id);

        assertEquals("T", nota.getTitulo());
        assertEquals("X", nota.getTexto());
        assertTrue(nota.getFechaCreacion() > 0);
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
        entity.fechaCreacion = 946684800000L; // 01/01/2000 00:00 UTC
        NotasDatabase.get(context, DB_NAME).notaDao().updateNota(entity);

        notaDAO.editNota(id, "Editado", "nuevo");

        Nota nota = notaDAO.getNota(id);
        assertEquals("Editado", nota.getTitulo());
        assertEquals("nuevo", nota.getTexto());
        assertEquals(946684800000L, nota.getFechaCreacion());
        assertTrue(nota.getFechaModificacion() > 0);
        assertNotEquals(946684800000L, nota.getFechaModificacion());
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
    public void deleteNota_laMueveALaPapeleraYConservaLosVinculos() {
        int id = nuevaNotaEnLibretaDefault("T", "X");
        etiquetaDAO.createEtiqueta("E");
        notaDAO.addEtiquetasToNota(id, todasLasEtiquetas());

        notaDAO.deleteNota(id);

        List<Nota> activas = new ArrayList<>();
        notaDAO.getAllNotas(activas);
        assertTrue(activas.isEmpty());

        List<Nota> papelera = new ArrayList<>();
        notaDAO.getNotasEliminadas(papelera);
        assertEquals(1, papelera.size());
        // Los vínculos se conservan para poder restaurarla tal cual estaba.
        assertNotNull(notaDAO.getLibreta(id));
        List<Etiqueta> etiquetas = new ArrayList<>();
        notaDAO.getAllEtiquetasFrom(id, etiquetas);
        assertEquals(1, etiquetas.size());
    }

    @Test
    public void laLibretaNoCuentaLasNotasEnLaPapelera() {
        int id = nuevaNotaEnLibretaDefault("A", "x");
        assertEquals(1, libretaDAO.getLibreta(1).getNumNotas());

        notaDAO.deleteNota(id);

        assertEquals(0, libretaDAO.getLibreta(1).getNumNotas());
    }

    @Test
    public void restaurarNota_laDevuelveAlListado() {
        int id = nuevaNotaEnLibretaDefault("T", "X");
        notaDAO.deleteNota(id);

        notaDAO.restaurarNota(id);

        List<Nota> activas = new ArrayList<>();
        notaDAO.getAllNotas(activas);
        assertEquals(1, activas.size());
        List<Nota> papelera = new ArrayList<>();
        notaDAO.getNotasEliminadas(papelera);
        assertTrue(papelera.isEmpty());
    }

    @Test
    public void borrarNotaDefinitivamente_laEliminaSinDejarRastro() {
        int id = nuevaNotaEnLibretaDefault("T", "X");
        notaDAO.deleteNota(id);

        notaDAO.borrarNotaDefinitivamente(id);

        List<Nota> papelera = new ArrayList<>();
        notaDAO.getNotasEliminadas(papelera);
        assertTrue(papelera.isEmpty());
        assertNull(notaDAO.getNota(id));
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

    @Test
    public void buscarNotas_encuentraPorElContenido() {
        int id = nuevaNotaEnLibretaDefault("Lista", "comprar elefante azul");
        nuevaNotaEnLibretaDefault("Otra", "texto distinto");

        List<Nota> encontradas = buscar("elefante*");

        assertEquals(1, encontradas.size());
        assertEquals(id, encontradas.get(0).getId());
    }

    @Test
    public void buscarNotas_encuentraPorElTitulo() {
        int id = nuevaNotaEnLibretaDefault("Receta", "harina y huevos");
        nuevaNotaEnLibretaDefault("Otra", "sin relación");

        List<Nota> encontradas = buscar("receta*");

        assertEquals(1, encontradas.size());
        assertEquals(id, encontradas.get(0).getId());
    }

    @Test
    public void buscarNotas_respetaElAmbitoDeLaLibreta() {
        libretaDAO.createLibreta("Trabajo");
        Libreta trabajo = null;
        List<Libreta> libretas = new ArrayList<>();
        libretaDAO.getAllLibretas(libretas);
        for (Libreta libreta : libretas) {
            if (libreta.getTitulo().equals("Trabajo")) {
                trabajo = libreta;
            }
        }

        int idEnDefault = nuevaNotaEnLibretaDefault("Informe", "pendiente");
        int idEnTrabajo = notaDAO.createNota("Informe", "pendiente");
        libretaDAO.addNotaToLibreta(trabajo.getId(), idEnTrabajo);

        List<Nota> encontradas = new ArrayList<>();
        notaDAO.buscarNotas("informe*", trabajo.getId(), -1, encontradas);

        assertEquals(1, encontradas.size());
        assertEquals(idEnTrabajo, encontradas.get(0).getId());
        assertTrue(idEnDefault != idEnTrabajo);
    }

    @Test
    public void buscarNotas_actualizaElIndiceAlEditar() {
        int id = nuevaNotaEnLibretaDefault("T", "palabra vieja");

        notaDAO.editNota(id, "T", "palabra nueva");

        assertTrue(buscar("vieja*").isEmpty());
        assertEquals(1, buscar("nueva*").size());
    }

    @Test
    public void buscarNotas_actualizaElIndiceAlEliminar() {
        int id = nuevaNotaEnLibretaDefault("T", "efímero");

        notaDAO.deleteNota(id);

        assertTrue(buscar("efímero*").isEmpty());
    }

    private List<Nota> buscar(String consulta) {
        List<Nota> encontradas = new ArrayList<>();
        notaDAO.buscarNotas(consulta, -1, -1, encontradas);
        return encontradas;
    }

    @Test
    public void adjuntos_seAnadenYSeListan() {
        int id = nuevaNotaEnLibretaDefault("T", "X");

        int idAdjunto = notaDAO.addAdjunto(id, "foto.png", "foto.png", "image/png");

        assertTrue(idAdjunto > 0);
        List<Adjunto> adjuntos = new ArrayList<>();
        notaDAO.getAdjuntosFrom(id, adjuntos);
        assertEquals(1, adjuntos.size());
        assertEquals("foto.png", adjuntos.get(0).getRuta());
        assertEquals(id, adjuntos.get(0).getNotaId());
    }

    @Test
    public void adjuntos_seBorraSoloElIndicado() {
        int id = nuevaNotaEnLibretaDefault("T", "X");
        int primero = notaDAO.addAdjunto(id, "a.png", "a.png", "image/png");
        notaDAO.addAdjunto(id, "b.png", "b.png", "image/png");

        notaDAO.deleteAdjunto(primero);

        List<Adjunto> adjuntos = new ArrayList<>();
        notaDAO.getAdjuntosFrom(id, adjuntos);
        assertEquals(1, adjuntos.size());
        assertEquals("b.png", adjuntos.get(0).getRuta());
    }

    @Test
    public void losAdjuntosNoSeMezclanEntreNotas() {
        int idUno = nuevaNotaEnLibretaDefault("A", "x");
        int idDos = nuevaNotaEnLibretaDefault("B", "y");
        notaDAO.addAdjunto(idUno, "a.png", "a.png", "image/png");
        notaDAO.addAdjunto(idDos, "b.png", "b.png", "image/png");

        List<Adjunto> deUno = new ArrayList<>();
        notaDAO.getAdjuntosFrom(idUno, deUno);

        assertEquals(1, deUno.size());
        assertEquals("a.png", deUno.get(0).getRuta());
    }

    @Test
    public void adjuntos_seEliminanAlBorrarLaNotaDefinitivamente() {
        int id = nuevaNotaEnLibretaDefault("T", "X");
        notaDAO.addAdjunto(id, "foto.png", "foto.png", "image/png");
        notaDAO.deleteNota(id);

        notaDAO.borrarNotaDefinitivamente(id);

        List<Adjunto> adjuntos = new ArrayList<>();
        notaDAO.getAdjuntosFrom(id, adjuntos);
        assertTrue(adjuntos.isEmpty());
    }
}
