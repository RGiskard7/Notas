package com.inkpot.app.ajustes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.Intent;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.test.core.app.ApplicationProvider;

import com.inkpot.app.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class AjustesActivityTest {
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        Preferencias.setTema(context, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    private AjustesActivity lanzar() {
        return Robolectric.buildActivity(AjustesActivity.class).setup().get();
    }

    @Test
    public void porDefecto_elTemaSigueAlSistema() {
        AjustesActivity activity = lanzar();

        assertTrue(((RadioButton) activity.findViewById(R.id.temaSistema)).isChecked());
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, Preferencias.getTema(context));
    }

    @Test
    public void elegirOscuro_loGuarda() {
        AjustesActivity activity = lanzar();

        ((RadioButton) activity.findViewById(R.id.temaOscuro)).setChecked(true);

        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, Preferencias.getTema(context));
    }

    @Test
    public void elegirClaro_loGuarda() {
        AjustesActivity activity = lanzar();

        ((RadioButton) activity.findViewById(R.id.temaClaro)).setChecked(true);

        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, Preferencias.getTema(context));
    }

    @Test
    public void alAbrirConTemaGuardado_muestraLaOpcionCorrecta() {
        Preferencias.setTema(context, AppCompatDelegate.MODE_NIGHT_YES);

        AjustesActivity activity = lanzar();

        assertTrue(((RadioButton) activity.findViewById(R.id.temaOscuro)).isChecked());
    }

    @Test
    public void clicEnAcercaDe_abreLaPantalla() {
        AjustesActivity activity = lanzar();

        activity.findViewById(R.id.filaAcercaDe).performClick();

        Intent siguiente = shadowOf(activity).getNextStartedActivity();
        assertNotNull(siguiente);
        assertEquals(AcercaDeActivity.class.getName(), siguiente.getComponent().getClassName());
    }
}
