package com.inkpot.app.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VinietasTest {
    @Test
    public void cursorEnCero_noNecesitaSalto() {
        assertFalse(Vinietas.necesitaSaltoDeLinea("hola", 0));
    }

    @Test
    public void cursorNegativo_noNecesitaSalto() {
        assertFalse(Vinietas.necesitaSaltoDeLinea("hola", -1));
    }

    @Test
    public void cursorFueraDeRango_noNecesitaSalto() {
        assertFalse(Vinietas.necesitaSaltoDeLinea("hola", 10));
    }

    @Test
    public void textoNulo_noNecesitaSalto() {
        assertFalse(Vinietas.necesitaSaltoDeLinea(null, 1));
    }

    @Test
    public void caracterAsciiAntes_necesitaSalto() {
        assertTrue(Vinietas.necesitaSaltoDeLinea("hola", 4));
    }

    @Test
    public void caracterDeControlAntesEnPosicion1_noFalla() {
        // Antes lanzaba StringIndexOutOfBounds al leer charAt(-1)
        assertFalse(Vinietas.necesitaSaltoDeLinea("\u0001hola", 1));
    }

    @Test
    public void vinietaEnPosicion0_cursor1_noFalla() {
        assertFalse(Vinietas.necesitaSaltoDeLinea("\u2022hola", 1));
    }

    @Test
    public void vinietaDosPosicionesAntes_necesitaSalto() {
        assertTrue(Vinietas.necesitaSaltoDeLinea("\u2022\u0001hola", 2));
    }

    @Test
    public void caracterNoAsciiNoVinieta_noNecesitaSalto() {
        assertFalse(Vinietas.necesitaSaltoDeLinea("\u20AC\u0001hola", 2));
    }
}
