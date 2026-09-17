package com.example.notas.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class FiltroTituloTest {
    private static final FiltroTitulo.TituloProvider<String> PROVIDER = new FiltroTitulo.TituloProvider<String>() {
        @Override
        public String titulo(String item) {
            return item;
        }
    };

    private List<String> base() {
        return Arrays.asList("Compras", "Trabajo", "casa");
    }

    @Test
    public void consultaVacia_devuelveTodo() {
        assertEquals(3, FiltroTitulo.filtrar(base(), "", PROVIDER).size());
    }

    @Test
    public void consultaNula_devuelveTodo() {
        assertEquals(3, FiltroTitulo.filtrar(base(), null, PROVIDER).size());
    }

    @Test
    public void ignoraMayusculasYMinusculas() {
        List<String> resultado = FiltroTitulo.filtrar(base(), "TRABAJO", PROVIDER);
        assertEquals(1, resultado.size());
        assertEquals("Trabajo", resultado.get(0));
    }

    @Test
    public void filtraPorSubcadena() {
        assertEquals(2, FiltroTitulo.filtrar(base(), "o", PROVIDER).size());
    }

    @Test
    public void sinCoincidencias_devuelveVacio() {
        assertTrue(FiltroTitulo.filtrar(base(), "zzz", PROVIDER).isEmpty());
    }

    @Test
    public void conservaElOrdenOriginal() {
        List<String> resultado = FiltroTitulo.filtrar(base(), "o", PROVIDER);
        assertEquals("Compras", resultado.get(0));
        assertEquals("Trabajo", resultado.get(1));
    }
}
