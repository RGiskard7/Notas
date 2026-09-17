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
}
