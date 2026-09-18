package com.inkpot.app.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FechasTest {
    @Test
    public void formateaNotaConElPatronEsperado() {
        assertTrue(Fechas.formatearNota(0L).matches("\\d{2}/\\d{2}/\\d{4} - \\d{2}:\\d{2}"));
    }

    @Test
    public void formateaFechaConElPatronEsperado() {
        assertTrue(Fechas.formatearFecha(0L).matches("\\d{2}/\\d{2}/\\d{4}"));
    }

    @Test
    public void laMismaMarcaProduceElMismoTexto() {
        assertEquals(Fechas.formatearNota(1000L), Fechas.formatearNota(1000L));
    }

    @Test
    public void marcasDistintasProducenTextosDistintos() {
        assertNotEquals(Fechas.formatearNota(0L), Fechas.formatearNota(86400000L));
    }
}
