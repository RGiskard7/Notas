package com.inkpot.app.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AdjuntosTest {
    @Test
    public void extraeLaExtension() {
        assertEquals(".png", Adjuntos.extension("foto.png"));
        assertEquals(".jpeg", Adjuntos.extension("mi.foto.jpeg"));
    }

    @Test
    public void sinExtensionODesconocido_devuelveVacio() {
        assertEquals("", Adjuntos.extension("sin-extension"));
        assertEquals("", Adjuntos.extension(null));
        assertEquals("", Adjuntos.extension(".oculto"));
    }
}
