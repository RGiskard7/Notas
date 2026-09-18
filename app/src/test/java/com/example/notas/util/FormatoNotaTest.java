package com.example.notas.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class FormatoNotaTest {
    @Test
    public void detectaTareas() {
        assertTrue(FormatoNota.esTarea("- [ ] comprar"));
        assertTrue(FormatoNota.esTarea("- [x] hecho"));
        assertFalse(FormatoNota.esTarea("texto normal"));
    }

    @Test
    public void extraeElContenidoDeLaTarea() {
        assertEquals("comprar pan", FormatoNota.contenidoDeTarea("- [ ] comprar pan"));
        assertEquals("hecho", FormatoNota.contenidoDeTarea("- [x] hecho"));
    }

    @Test
    public void parseaLasLineas() {
        List<FormatoNota.Linea> lineas = FormatoNota.parsear("Titulo\n- [ ] pendiente\n- [x] hecha");

        assertEquals(3, lineas.size());
        assertFalse(lineas.get(0).tarea);
        assertTrue(lineas.get(1).tarea);
        assertFalse(lineas.get(1).hecha);
        assertTrue(lineas.get(2).tarea);
        assertTrue(lineas.get(2).hecha);
    }

    @Test
    public void alternaElEstadoDeUnaTarea() {
        assertEquals("- [x] comprar", FormatoNota.alternarTarea("- [ ] comprar", 0));
        assertEquals("- [ ] comprar", FormatoNota.alternarTarea("- [x] comprar", 0));
    }

    @Test
    public void alternarEnUnaLineaQueNoEsTareaNoCambiaNada() {
        assertEquals("texto normal", FormatoNota.alternarTarea("texto normal", 0));
    }

    @Test
    public void alternaLaTareaCorrecta() {
        String texto = "- [ ] primera\n- [ ] segunda";
        assertEquals("- [ ] primera\n- [x] segunda", FormatoNota.alternarTarea(texto, 1));
    }

    @Test
    public void insertaCasillaAlFinalAnadiendoSalto() {
        assertEquals("hola\n- [ ] ", FormatoNota.insertarCasilla("hola", 4));
    }

    @Test
    public void insertaCasillaAlPrincipioDeUnaLineaVacia() {
        assertEquals("a\n- [ ] ", FormatoNota.insertarCasilla("a\n", 2));
        assertEquals("- [ ] ", FormatoNota.insertarCasilla("", 0));
    }

    @Test
    public void formateaNegritaYCursiva() {
        List<FormatoNota.Fragmento> fragmentos = FormatoNota.formatearEnLinea("a **b** c *d*");

        assertEquals(4, fragmentos.size());
        assertEquals("a ", fragmentos.get(0).texto);
        assertTrue(fragmentos.get(1).negrita);
        assertEquals("b", fragmentos.get(1).texto);
        assertEquals(" c ", fragmentos.get(2).texto);
        assertTrue(fragmentos.get(3).cursiva);
        assertEquals("d", fragmentos.get(3).texto);
    }

    @Test
    public void textoPlanoEsUnSoloFragmento() {
        List<FormatoNota.Fragmento> fragmentos = FormatoNota.formatearEnLinea("sin formato");

        assertEquals(1, fragmentos.size());
        assertFalse(fragmentos.get(0).negrita);
        assertFalse(fragmentos.get(0).cursiva);
    }

    @Test
    public void formateaTachado() {
        List<FormatoNota.Fragmento> fragmentos = FormatoNota.formatearEnLinea("~~hola~~");

        assertEquals(1, fragmentos.size());
        assertTrue(fragmentos.get(0).tachado);
        assertEquals("hola", fragmentos.get(0).texto);
    }

    @Test
    public void formateaCodigo() {
        List<FormatoNota.Fragmento> fragmentos = FormatoNota.formatearEnLinea("`codigo`");

        assertEquals(1, fragmentos.size());
        assertTrue(fragmentos.get(0).codigo);
        assertEquals("codigo", fragmentos.get(0).texto);
    }

    @Test
    public void detectaEncabezadosYCitas() {
        assertTrue(FormatoNota.esEncabezado("## Titulo"));
        assertEquals("Titulo", FormatoNota.textoEncabezado("## Titulo"));
        assertTrue(FormatoNota.esCita("> cita"));
        assertEquals("cita", FormatoNota.textoCita("> cita"));
        assertTrue(FormatoNota.esSeparador("---"));
    }

    @Test
    public void progresoTareas_cuentaHechasYTotal() {
        int[] progreso = FormatoNota.progresoTareas("- [ ] una\n- [x] dos\n- [ ] tres\ntexto");

        assertEquals(1, progreso[0]);
        assertEquals(3, progreso[1]);
    }

    @Test
    public void progresoTareas_sinTareas_esCero() {
        int[] progreso = FormatoNota.progresoTareas("solo texto");

        assertEquals(0, progreso[0]);
        assertEquals(0, progreso[1]);
    }
}
