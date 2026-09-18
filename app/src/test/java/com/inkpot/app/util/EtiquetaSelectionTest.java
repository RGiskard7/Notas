package com.inkpot.app.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.inkpot.app.data.Etiqueta;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class EtiquetaSelectionTest {
    private Etiqueta etiqueta(String titulo) {
        return new Etiqueta(0, titulo, 0L);
    }

    @Test
    public void sinCambios_noProponeNada() {
        List<Etiqueta> todas = Arrays.asList(etiqueta("A"), etiqueta("B"));
        Set<Etiqueta> actuales = new HashSet<>(Arrays.asList(todas.get(0), todas.get(1)));

        EtiquetaSelection.Diff diff = EtiquetaSelection.calcular(actuales, todas, new boolean[]{true, true});

        assertTrue(diff.anadidas.isEmpty());
        assertTrue(diff.quitadas.isEmpty());
    }

    @Test
    public void marcarNueva_laAnade() {
        List<Etiqueta> todas = Arrays.asList(etiqueta("A"), etiqueta("B"));
        Set<Etiqueta> actuales = new HashSet<>(Arrays.asList(todas.get(0)));

        EtiquetaSelection.Diff diff = EtiquetaSelection.calcular(actuales, todas, new boolean[]{true, true});

        assertEquals(1, diff.anadidas.size());
        assertEquals("B", diff.anadidas.get(0).getTitulo());
        assertTrue(diff.quitadas.isEmpty());
    }

    @Test
    public void desmarcarExistente_laQuita() {
        List<Etiqueta> todas = Arrays.asList(etiqueta("A"), etiqueta("B"));
        Set<Etiqueta> actuales = new HashSet<>(Arrays.asList(todas.get(0), todas.get(1)));

        EtiquetaSelection.Diff diff = EtiquetaSelection.calcular(actuales, todas, new boolean[]{false, true});

        assertEquals(1, diff.quitadas.size());
        assertEquals("A", diff.quitadas.get(0).getTitulo());
        assertTrue(diff.anadidas.isEmpty());
    }

    @Test
    public void cambioParcial_calculaAmbosLados() {
        List<Etiqueta> todas = Arrays.asList(etiqueta("A"), etiqueta("B"), etiqueta("C"));
        Set<Etiqueta> actuales = new HashSet<>(Arrays.asList(todas.get(0)));

        EtiquetaSelection.Diff diff = EtiquetaSelection.calcular(actuales, todas, new boolean[]{false, true, true});

        assertEquals(1, diff.quitadas.size());
        assertEquals("A", diff.quitadas.get(0).getTitulo());
        assertEquals(2, diff.anadidas.size());
    }

    @Test
    public void arrayDeMarcadasMasCorto_cuentaComoDesmarcado() {
        List<Etiqueta> todas = Arrays.asList(etiqueta("A"), etiqueta("B"));
        Set<Etiqueta> actuales = new HashSet<>(Arrays.asList(todas.get(0), todas.get(1)));

        EtiquetaSelection.Diff diff = EtiquetaSelection.calcular(actuales, todas, new boolean[]{true});

        assertEquals(1, diff.quitadas.size());
        assertEquals("B", diff.quitadas.get(0).getTitulo());
    }
}
