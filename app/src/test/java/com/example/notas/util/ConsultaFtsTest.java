package com.example.notas.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConsultaFtsTest {
    @Test
    public void consultaNula_devuelveVacio() {
        assertEquals("", ConsultaFts.paraMatch(null));
    }

    @Test
    public void consultaVacia_devuelveVacio() {
        assertEquals("", ConsultaFts.paraMatch("   "));
    }

    @Test
    public void soloSimbolos_devuelveVacio() {
        assertEquals("", ConsultaFts.paraMatch("!!! ¿?"));
    }

    @Test
    public void unTermino_seBuscaPorPrefijo() {
        assertEquals("casa*", ConsultaFts.paraMatch("casa"));
    }

    @Test
    public void variosTerminos_seUnen() {
        assertEquals("casa* perro*", ConsultaFts.paraMatch("casa perro"));
    }

    @Test
    public void limpiaApostrofosYGuiones() {
        assertEquals("casas*", ConsultaFts.paraMatch("casa's"));
        assertEquals("ab*", ConsultaFts.paraMatch("a-b"));
    }

    @Test
    public void conservaAcentos() {
        assertEquals("canción*", ConsultaFts.paraMatch("canción"));
    }
}
