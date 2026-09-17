package com.example.notas.seguridad;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.Intent;
import android.widget.EditText;

import androidx.test.core.app.ApplicationProvider;

import com.example.notas.MainActivity;
import com.example.notas.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class BloqueoActivityTest {
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        GestorPin.quitarPin(context);
    }

    private BloqueoActivity lanzar() {
        return Robolectric.buildActivity(BloqueoActivity.class).setup().get();
    }

    @Test
    public void conPinCorrecto_abreLaAplicacion() {
        GestorPin.guardarPin(context, "1234");
        GestorPin.bloquear();
        BloqueoActivity activity = lanzar();

        ((EditText) activity.findViewById(R.id.editTextPin)).setText("1234");
        activity.findViewById(R.id.buttonDesbloquear).performClick();

        Intent siguiente = shadowOf(activity).getNextStartedActivity();
        assertNotNull(siguiente);
        assertEquals(MainActivity.class.getName(), siguiente.getComponent().getClassName());
    }

    @Test
    public void conPinIncorrecto_noAbreLaAplicacion() {
        GestorPin.guardarPin(context, "1234");
        GestorPin.bloquear();
        BloqueoActivity activity = lanzar();

        ((EditText) activity.findViewById(R.id.editTextPin)).setText("0000");
        activity.findViewById(R.id.buttonDesbloquear).performClick();

        assertNull(shadowOf(activity).getNextStartedActivity());
        assertEquals("", ((EditText) activity.findViewById(R.id.editTextPin)).getText().toString());
    }

    @Test
    public void sinPin_abreLaAplicacionDirectamente() {
        BloqueoActivity activity = lanzar();

        Intent siguiente = shadowOf(activity).getNextStartedActivity();
        assertNotNull(siguiente);
        assertEquals(MainActivity.class.getName(), siguiente.getComponent().getClassName());
    }
}
