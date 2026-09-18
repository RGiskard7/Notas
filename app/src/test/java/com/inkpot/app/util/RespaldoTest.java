package com.inkpot.app.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.inkpot.app.data.Etiqueta;
import com.inkpot.app.data.Nota;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class RespaldoTest {
    private Nota nota(int id, String titulo, String texto) {
        return new Nota(id, titulo, texto, null, new ArrayList<Etiqueta>(), 0L);
    }

    @Test
    public void exportarEImportar_conservaLasNotas() throws Exception {
        List<Nota> originales = new ArrayList<>();
        originales.add(nota(1, "Compra", "leche"));
        originales.add(nota(2, "Trabajo", "informe"));

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Respaldo.exportar(originales, salida);

        List<Markdown.NotaMarkdown> importadas = Respaldo.importar(new ByteArrayInputStream(salida.toByteArray()));

        assertEquals(2, importadas.size());
        assertTrue(contiene(importadas, "Compra", "leche"));
        assertTrue(contiene(importadas, "Trabajo", "informe"));
    }

    @Test
    public void exportar_conTitulosRepetidos_noSobrescribeEntradas() throws Exception {
        List<Nota> originales = new ArrayList<>();
        originales.add(nota(1, "Igual", "uno"));
        originales.add(nota(2, "Igual", "dos"));

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Respaldo.exportar(originales, salida);

        List<Markdown.NotaMarkdown> importadas = Respaldo.importar(new ByteArrayInputStream(salida.toByteArray()));

        assertEquals(2, importadas.size());
    }

    private boolean contiene(List<Markdown.NotaMarkdown> notas, String titulo, String texto) {
        for (Markdown.NotaMarkdown nota : notas) {
            if (titulo.equals(nota.titulo) && texto.equals(nota.texto)) {
                return true;
            }
        }
        return false;
    }
}
