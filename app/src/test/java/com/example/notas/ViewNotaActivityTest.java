package com.example.notas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.test.core.app.ApplicationProvider;

import com.example.notas.data.FactoryDAO;
import com.example.notas.data.ILibretaDAO;
import com.example.notas.data.INotaDAO;
import com.example.notas.data.Nota;
import com.example.notas.data.NotasRepository;
import com.example.notas.data.room.NotaEntity;
import com.example.notas.data.room.NotasDatabase;
import com.example.notas.util.Fechas;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ViewNotaActivityTest {
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

    private Nota crearNota(String titulo, String texto) {
        int id = notaDAO.createNota(titulo, texto);
        libretaDAO.addNotaToLibreta(1, id);
        return notaDAO.getNota(id);
    }

    private ViewNotaActivity lanzar(Nota nota) {
        Intent intent = new Intent(context, ViewNotaActivity.class);
        intent.putExtra("nota", nota);
        return Robolectric.buildActivity(ViewNotaActivity.class, intent).setup().get();
    }

    @Test
    public void muestraTituloTextoYLibreta() {
        Nota nota = crearNota("Titulo", "Contenido");

        ViewNotaActivity activity = lanzar(nota);

        assertEquals("Titulo", ((TextView) activity.findViewById(R.id.textViewTituloNota)).getText().toString());
        assertEquals("Contenido", ((TextView) activity.findViewById(R.id.textViewTextoNota)).getText().toString());
        assertEquals("Default", ((TextView) activity.findViewById(R.id.textViewLibretaNota)).getText().toString());
    }

    @Test
    public void muestraLaFechaDeModificacionCuandoEsDistinta() {
        Nota nota = crearNota("Titulo", "Contenido");
        NotaEntity entity = NotasDatabase.get(context, "DBNevernote").notaDao().getNotaById(nota.getId());
        entity.fechaModificacion = 946684800000L; // 01/01/2000 00:00 UTC
        NotasDatabase.get(context, "DBNevernote").notaDao().updateNota(entity);

        ViewNotaActivity activity = lanzar(notaDAO.getNota(nota.getId()));

        TextView fechaModificacion = activity.findViewById(R.id.textViewFechaModificacion);
        assertEquals(View.VISIBLE, fechaModificacion.getVisibility());
        assertTrue(fechaModificacion.getText().toString().contains(Fechas.formatearNota(946684800000L)));
    }

    @Test
    public void eliminarNota_laMueveALaPapeleraTrasConfirmar() {
        Nota nota = crearNota("Para borrar", "x");
        ViewNotaActivity activity = lanzar(nota);

        shadowOf(activity).clickMenuItem(R.id.action_Eliminar);

        Dialog dialog = ShadowDialog.getLatestDialog();
        assertTrue(dialog instanceof AlertDialog);
        ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();

        List<Nota> activas = new ArrayList<>();
        notaDAO.getAllNotas(activas);
        assertTrue(activas.isEmpty());

        List<Nota> papelera = new ArrayList<>();
        notaDAO.getNotasEliminadas(papelera);
        assertEquals(1, papelera.size());
    }

    @Test
    public void cancelarEliminacion_conservaLaNota() {
        Nota nota = crearNota("Se queda", "x");
        ViewNotaActivity activity = lanzar(nota);

        shadowOf(activity).clickMenuItem(R.id.action_Eliminar);

        Dialog dialog = ShadowDialog.getLatestDialog();
        ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();

        assertTrue(notaDAO.getNota(nota.getId()) != null);
    }

    @Test
    public void laImagenAdjuntaSeMuestraPeroNoSePuedeQuitarDesdeLaVista() {
        Nota nota = crearNota("Con imagen", "x");
        notaDAO.addAdjunto(nota.getId(), "ruta.png", "foto.png", "image/png");

        ViewNotaActivity activity = lanzar(notaDAO.getNota(nota.getId()));

        LinearLayout contenedor = activity.findViewById(R.id.contenedorAdjuntos);
        assertEquals(1, contenedor.getChildCount());
        View quitar = contenedor.getChildAt(0).findViewById(R.id.buttonQuitarAdjunto);
        assertEquals(View.GONE, quitar.getVisibility());
    }
}
