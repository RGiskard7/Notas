package com.example.notas.util;

import com.example.notas.data.Etiqueta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class EtiquetaSelection {
    private EtiquetaSelection() {
    }

    public static class Diff {
        public final List<Etiqueta> anadidas = new ArrayList<>();
        public final List<Etiqueta> quitadas = new ArrayList<>();
    }

    public static Diff calcular(Set<Etiqueta> actuales, List<Etiqueta> todas, boolean[] marcadas) {
        Diff diff = new Diff();
        for (int i = 0; i < todas.size(); i++) {
            Etiqueta etiqueta = todas.get(i);
            boolean marcada = i < marcadas.length && marcadas[i];
            boolean actual = actuales.contains(etiqueta);

            if (marcada && !actual) {
                diff.anadidas.add(etiqueta);
            } else if (!marcada && actual) {
                diff.quitadas.add(etiqueta);
            }
        }
        return diff;
    }
}
