package com.example.notas.util;

import com.example.notas.data.Etiqueta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Calcula qué etiquetas hay que añadir y quitar de una nota a partir del estado
 * de las casillas del diálogo de selección.
 */
public final class EtiquetaSelection {
    private EtiquetaSelection() {
    }

    /** Etiquetas que se añaden y que se quitan respecto al estado actual. */
    public static class Diff {
        public final List<Etiqueta> anadidas = new ArrayList<>();
        public final List<Etiqueta> quitadas = new ArrayList<>();
    }

    /**
     * Compara las etiquetas actuales con las marcadas en el diálogo.
     *
     * @param actuales etiquetas que la nota tiene ahora.
     * @param todas    todas las etiquetas disponibles.
     * @param marcadas casillas marcadas, en el mismo orden que {@code todas}.
     * @return las etiquetas que hay que añadir y las que hay que quitar.
     */
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
