package com.example.notas.seguridad;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class GestorPinTest {
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        GestorPin.quitarPin(context);
    }

    @Test
    public void alPrincipioNoHayPin() {
        assertFalse(GestorPin.hayPin(context));
    }

    @Test
    public void guardarPin_permiteComprobarlo() {
        GestorPin.guardarPin(context, "1234");

        assertTrue(GestorPin.hayPin(context));
        assertTrue(GestorPin.comprobar(context, "1234"));
        assertFalse(GestorPin.comprobar(context, "0000"));
    }

    @Test
    public void cambiarElPin_invalidaElAnterior() {
        GestorPin.guardarPin(context, "1234");

        GestorPin.guardarPin(context, "5678");

        assertFalse(GestorPin.comprobar(context, "1234"));
        assertTrue(GestorPin.comprobar(context, "5678"));
    }

    @Test
    public void quitarPin_loElimina() {
        GestorPin.guardarPin(context, "1234");

        GestorPin.quitarPin(context);

        assertFalse(GestorPin.hayPin(context));
        assertFalse(GestorPin.comprobar(context, "1234"));
    }

    @Test
    public void bloquearYdesbloquearCambianElEstadoDeLaSesion() {
        GestorPin.marcarDesbloqueado();
        assertTrue(GestorPin.estaDesbloqueado());

        GestorPin.bloquear();
        assertFalse(GestorPin.estaDesbloqueado());
    }
}
