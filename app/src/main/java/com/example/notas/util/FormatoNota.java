package com.example.notas.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Formato ligero de las notas: listas de tareas y negrita/cursiva.
 *
 * <p>Las tareas se escriben como en Markdown ({@code - [ ] } para pendientes y
 * {@code - [x] } para hechas) y la negrita/cursiva con {@code **} y {@code *}.
 * Aquí solo se interpreta el texto; el dibujado con estilos se hace en la capa
 * de interfaz.</p>
 */
public final class FormatoNota {

    /** Marca que se inserta al crear una tarea. */
    public static final String MARCA_TAREA = "- [ ] ";

    /** Símbolo de casilla sin marcar. */
    public static final String CASILLA_VACIA = "\u2610";

    /** Símbolo de casilla marcada. */
    public static final String CASILLA_HECHA = "\u2611";

    private FormatoNota() {
        // Clase de utilidades: no se instancia.
    }

    /** Una línea de la nota, ya interpretada. */
    public static class Linea {
        public final boolean tarea;
        public final boolean hecha;
        public final String contenido;

        Linea(boolean tarea, boolean hecha, String contenido) {
            this.tarea = tarea;
            this.hecha = hecha;
            this.contenido = contenido;
        }
    }

    /** Trozo de texto con el estilo que le corresponde. */
    public static class Fragmento {
        public final String texto;
        public final boolean negrita;
        public final boolean cursiva;

        Fragmento(String texto, boolean negrita, boolean cursiva) {
            this.texto = texto;
            this.negrita = negrita;
            this.cursiva = cursiva;
        }
    }

    /** Indica si la línea es una tarea. */
    public static boolean esTarea(String linea) {
        String limpia = linea == null ? "" : linea.trim().toLowerCase(Locale.ROOT);
        return limpia.startsWith("- [ ]") || limpia.startsWith("- [x]");
    }

    /** Indica si la tarea está hecha. */
    public static boolean estaHecha(String linea) {
        return linea != null && linea.trim().toLowerCase(Locale.ROOT).startsWith("- [x]");
    }

    /** Devuelve el texto de la tarea sin el marcador. */
    public static String contenidoDeTarea(String linea) {
        String limpia = linea == null ? "" : linea.trim();
        return limpia.length() > 6 ? limpia.substring(6).trim() : "";
    }

    /** Descompone la nota en líneas interpretadas. */
    public static List<Linea> parsear(String texto) {
        List<Linea> lineas = new ArrayList<>();
        if (texto == null) {
            return lineas;
        }
        for (String linea : texto.split("\n", -1)) {
            if (esTarea(linea)) {
                lineas.add(new Linea(true, estaHecha(linea), contenidoDeTarea(linea)));
            } else {
                lineas.add(new Linea(false, false, linea));
            }
        }
        return lineas;
    }

    /**
     * Cambia el estado de una tarea (hecha / pendiente).
     *
     * @param texto        nota completa.
     * @param numeroLinea  índice de la línea, empezando en 0.
     * @return la nota con la tarea cambiada, o la misma si no es una tarea.
     */
    public static String alternarTarea(String texto, int numeroLinea) {
        if (texto == null) {
            return "";
        }
        String[] lineas = texto.split("\n", -1);
        if (numeroLinea < 0 || numeroLinea >= lineas.length || !esTarea(lineas[numeroLinea])) {
            return texto;
        }

        String prefijo = estaHecha(lineas[numeroLinea]) ? "- [ ] " : "- [x] ";
        lineas[numeroLinea] = prefijo + contenidoDeTarea(lineas[numeroLinea]);
        return unir(lineas);
    }

    /**
     * Inserta una casilla nueva en la posición del cursor, añadiendo un salto de
     * línea si no está al principio de una línea.
     *
     * @param texto  nota completa.
     * @param cursor posición del cursor.
     * @return la nota con la casilla insertada.
     */
    public static String insertarCasilla(String texto, int cursor) {
        String original = texto == null ? "" : texto;
        int posicion = Math.max(0, Math.min(cursor, original.length()));
        boolean alPrincipio = posicion == 0 || original.charAt(posicion - 1) == '\n';
        String insercion = (alPrincipio ? "" : "\n") + MARCA_TAREA;
        return original.substring(0, posicion) + insercion + original.substring(posicion);
    }

    /**
     * Separa una línea en trozos con negrita y cursiva.
     *
     * @param texto texto de una línea.
     * @return los fragmentos en orden.
     */
    public static List<Fragmento> formatearEnLinea(String texto) {
        List<Fragmento> fragmentos = new ArrayList<>();
        if (texto == null || texto.isEmpty()) {
            return fragmentos;
        }

        int i = 0;
        while (i < texto.length()) {
            if (texto.startsWith("**", i)) {
                int fin = texto.indexOf("**", i + 2);
                if (fin > i + 2) {
                    fragmentos.add(new Fragmento(texto.substring(i + 2, fin), true, false));
                    i = fin + 2;
                    continue;
                }
            }
            if (texto.charAt(i) == '*') {
                int fin = texto.indexOf('*', i + 1);
                if (fin > i + 1) {
                    fragmentos.add(new Fragmento(texto.substring(i + 1, fin), false, true));
                    i = fin + 1;
                    continue;
                }
            }

            int siguiente = texto.indexOf('*', i);
            if (siguiente == -1 || siguiente == i) {
                siguiente = texto.length();
            }
            fragmentos.add(new Fragmento(texto.substring(i, siguiente), false, false));
            i = siguiente;
        }
        return fragmentos;
    }

    private static String unir(String[] lineas) {
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < lineas.length; i++) {
            if (i > 0) {
                resultado.append('\n');
            }
            resultado.append(lineas[i]);
        }
        return resultado.toString();
    }
}
