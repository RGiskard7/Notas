package com.example.notas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Looper;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.example.notas.UI.ListEtiquetasFragment;
import com.example.notas.UI.ListLibretasFragment;
import com.example.notas.UI.ListNotasFragment;
import com.example.notas.data.FactoryDAO;
import com.example.notas.data.ILibretaDAO;
import com.example.notas.data.INotaDAO;
import com.example.notas.data.Libreta;
import com.example.notas.data.NotasRepository;
import com.example.notas.data.room.NotasDatabase;
import com.google.android.material.navigation.NavigationView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class MainActivityTest {
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

    private MainActivity lanzar() {
        return Robolectric.buildActivity(MainActivity.class).setup().get();
    }

    private Fragment fragmentActual(MainActivity activity) {
        return activity.getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
    }

    private int crearLibreta(String titulo) {
        libretaDAO.createLibreta(titulo);
        List<Libreta> libretas = new ArrayList<>();
        libretaDAO.getAllLibretas(libretas);
        for (Libreta libreta : libretas) {
            if (libreta.getTitulo().equals(titulo)) {
                return libreta.getId();
            }
        }
        throw new IllegalStateException("No se creó la libreta " + titulo);
    }

    private void seleccionarMenu(MainActivity activity, int idMenu) {
        NavigationView nav = activity.findViewById(R.id.nav_view);
        nav.setCheckedItem(idMenu);
        nav.getMenu().performIdentifierAction(idMenu, 0);
        activity.getSupportFragmentManager().executePendingTransactions();
    }

    @Test
    public void alArrancar_muestraTodasLasNotas() {
        MainActivity activity = lanzar();

        assertTrue(fragmentActual(activity) instanceof ListNotasFragment);
        assertEquals("Todas las notas", activity.getSupportActionBar().getTitle().toString());
    }

    @Test
    public void seleccionarLibretas_cambiaElFragmentYElTitulo() {
        MainActivity activity = lanzar();

        seleccionarMenu(activity, R.id.allLibretas);

        assertTrue(fragmentActual(activity) instanceof ListLibretasFragment);
        assertEquals("Libretas", activity.getSupportActionBar().getTitle().toString());
    }

    @Test
    public void seleccionarEtiquetas_cambiaElFragmentYElTitulo() {
        MainActivity activity = lanzar();

        seleccionarMenu(activity, R.id.allEtiquetas);

        assertTrue(fragmentActual(activity) instanceof ListEtiquetasFragment);
        assertEquals("Etiquetas", activity.getSupportActionBar().getTitle().toString());
    }

    @Test
    public void fabDesdeNotas_abreEdicionDeNota() {
        MainActivity activity = lanzar();

        activity.findViewById(R.id.fab).performClick();

        Intent siguiente = shadowOf(activity).getNextStartedActivity();
        assertNotNull(siguiente);
        assertEquals(EditNotaActivity.class.getName(), siguiente.getComponent().getClassName());
    }

    @Test
    public void fabDesdeLibretas_abreEdicionDeLibreta() {
        MainActivity activity = lanzar();
        seleccionarMenu(activity, R.id.allLibretas);

        activity.findViewById(R.id.fab).performClick();

        Intent siguiente = shadowOf(activity).getNextStartedActivity();
        assertNotNull(siguiente);
        assertEquals(EditLibretaActivity.class.getName(), siguiente.getComponent().getClassName());
    }

    @Test
    public void atrasDesdeLibretas_vuelveATodasLasNotas() {
        MainActivity activity = lanzar();
        seleccionarMenu(activity, R.id.allLibretas);

        activity.onBackPressed();
        activity.getSupportFragmentManager().executePendingTransactions();

        assertTrue(fragmentActual(activity) instanceof ListNotasFragment);
        assertEquals("Todas las notas", activity.getSupportActionBar().getTitle().toString());
    }

    @Test
    public void listaDeUnaLibreta_soloMuestraSusNotas() {
        int idLibreta = crearLibreta("Trabajo");
        int idNotaTrabajo = notaDAO.createNota("Nota de trabajo", "x");
        libretaDAO.addNotaToLibreta(idLibreta, idNotaTrabajo);
        int idNotaDefault = notaDAO.createNota("Nota de default", "y");
        libretaDAO.addNotaToLibreta(1, idNotaDefault);

        MainActivity activity = lanzar();
        Libreta libreta = libretaDAO.getLibreta(idLibreta);
        ListNotasFragment fragment = ListNotasFragment.newInstance(libreta);
        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment).commit();
        activity.getSupportFragmentManager().executePendingTransactions();

        RecyclerView rv = fragment.getView().findViewById(R.id.listViewNotas);
        assertEquals(1, rv.getAdapter().getItemCount());
    }

    @Test
    public void trasRecrear_elFragmentConservaSuLibreta() {
        int idLibreta = crearLibreta("Trabajo");

        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup();
        MainActivity activity = controller.get();
        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, ListNotasFragment.newInstance(libretaDAO.getLibreta(idLibreta)))
                .commit();
        activity.getSupportFragmentManager().executePendingTransactions();

        controller.recreate();

        Fragment restaurado = controller.get().getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        assertTrue(restaurado instanceof ListNotasFragment);
        assertNotNull(((ListNotasFragment) restaurado).getLibreta());
        assertEquals(idLibreta, ((ListNotasFragment) restaurado).getLibreta().getId());
    }

    @Test
    public void mantenerPulsadaUnaNota_permiteEliminarla() {
        int id = notaDAO.createNota("Borrable", "x");
        libretaDAO.addNotaToLibreta(1, id);

        MainActivity activity = lanzar();
        ListNotasFragment fragment = (ListNotasFragment) fragmentActual(activity);
        RecyclerView rv = fragment.getView().findViewById(R.id.listViewNotas);
        rv.measure(0, 0);
        rv.layout(0, 0, 1000, 2000);

        RecyclerView.ViewHolder holder = rv.findViewHolderForAdapterPosition(0);
        assertNotNull(holder);
        holder.itemView.performLongClick();
        shadowOf(Looper.getMainLooper()).idle();

        AlertDialog opciones = (AlertDialog) ShadowDialog.getLatestDialog();
        ListView lista = opciones.getListView();
        lista.performItemClick(lista.getChildAt(1), 1, 1); // "Eliminar"
        shadowOf(Looper.getMainLooper()).idle();

        AlertDialog confirmacion = (AlertDialog) ShadowDialog.getLatestDialog();
        confirmacion.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();

        assertNull(notaDAO.getNota(id));
    }
}
