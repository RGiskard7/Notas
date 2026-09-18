package com.inkpot.app.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MarkdownTest {
    @Test
    public void exportaConEncabezado() {
        assertEquals("# Compra\n\nleche y pan\n", Markdown.exportar("Compra", "leche y pan"));
    }

    @Test
    public void exportaNotaSinTexto() {
        assertEquals("# Solo titulo\n\n\n", Markdown.exportar("Solo titulo", null));
    }

    @Test
    public void importaConEncabezado() {
        Markdown.NotaMarkdown nota = Markdown.importar("# Compra\n\nleche y pan", "fichero.md");

        assertEquals("Compra", nota.titulo);
        assertEquals("leche y pan", nota.texto);
    }

    @Test
    public void importaSinEncabezado_usaElNombreDelFichero() {
        Markdown.NotaMarkdown nota = Markdown.importar("leche y pan", "lista.md");

        assertEquals("lista", nota.titulo);
        assertEquals("leche y pan", nota.texto);
    }

    @Test
    public void importaEncabezadoVacio_usaElNombreDelFichero() {
        Markdown.NotaMarkdown nota = Markdown.importar("#   \n\ncuerpo", "respaldo.md");

        assertEquals("respaldo", nota.titulo);
        assertEquals("cuerpo", nota.texto);
    }

    @Test
    public void importaFicheroVacio() {
        Markdown.NotaMarkdown nota = Markdown.importar("", "vacia.md");

        assertEquals("vacia", nota.titulo);
        assertEquals("", nota.texto);
    }

    @Test
    public void elViajeDeIdaYVueltaConservaLaNota() {
        String markdown = Markdown.exportar("Receta", "harina\nhuevos");

        Markdown.NotaMarkdown nota = Markdown.importar(markdown, "x.md");

        assertEquals("Receta", nota.titulo);
        assertEquals("harina\nhuevos", nota.texto);
    }

    @Test
    public void proponeUnNombreDeFicheroSeguro() {
        assertEquals("Mi nota.md", Markdown.nombreFichero("Mi nota"));
        assertEquals("nota.md", Markdown.nombreFichero("///"));
        assertEquals("abc.md", Markdown.nombreFichero("a/b:c"));
    }
}
