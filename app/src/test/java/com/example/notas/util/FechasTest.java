package com.example.notas.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FechasTest {
    @Test
    public void parseaFormatoDeNota() {
        assertEquals(2020, anio(Fechas.parse("31/12/2020 - 23:59")));
    }

    @Test
    public void parseaFormatoDeFecha() {
        assertEquals(2020, anio(Fechas.parse("31/12/2020")));
    }

    @Test
    public void valorInvalido_devuelveEpoch() {
        assertEquals(0L, Fechas.parse("no es una fecha").getTime());
    }

    @Test
    public void valorNulo_devuelveEpoch() {
        assertEquals(0L, Fechas.parse(null).getTime());
    }

    @Test
    public void ordenaCronologicamenteEntreAnios() {
        assertTrue(Fechas.parse("31/12/2020 - 23:59").before(Fechas.parse("01/01/2021 - 00:00")));
    }

    @Test
    public void ordenaCronologicamenteEntreMeses() {
        assertTrue(Fechas.parse("31/01/2021 - 10:00").before(Fechas.parse("01/02/2021 - 09:00")));
    }

    private int anio(Date fecha) {
        return Integer.parseInt(new SimpleDateFormat("yyyy").format(fecha));
    }
}
