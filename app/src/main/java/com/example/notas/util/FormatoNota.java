package com.example.notas.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Formato ligero de las notas (Markdown).
 *
 * <p>Se admiten listas de tareas ({@code - [ ] } / {@code - [x] }), viñetas,
 * listas numeradas, citas ({@code > }), encabezados ({@code # }), separadores
 * ({@code ---}) y, dentro de una línea, negrita ({@code **}), cursiva ({@code *}),
 * tachado ({@code ~~}) y código ({@code `}).</p>
 *
 * <p>Aquí solo se interpreta el texto; el dibujado con estilos se hace en la capa
 * de interfaz.</p>
 */
public final class FormatoNota {

    /** Marca que se inserta al crear una tarea. */
    public static final String MARCA_TAREA = "- [ ] ";

    /** Marca que se inserta al crear una viñeta. */
    public static final String MARCA_VINIETA = "- ";

    /** Símbolo de casilla sin marcar. */
    public static final String CASILLA_VACIA = "\u2610";

    /** Símbolo de casilla marcada. */
    public static final String CASILLA_HECHA = "\u2611";

    private static final String MARCA_ENCABEZADO = "^#{1,3}\\s+";

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
        public final boolean tachado;
        public final boolean codigo;

        Fragmento(String texto, boolean negrita, boolean cursiva, boolean tachado, boolean codigo) {
            this.texto = texto;
            this.negrita = negrita;
            this.cursiva = cursiva;
            this.tachado = tachado;
            this.codigo = codigo;
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

    /** Indica si la línea es un encabezado ({@code #}, {@code ##} o {@code ###}). */
    public static boolean esEncabezado(String linea) {
        String limpia = linea == null ? "" : linea.trim();
        return limpia.startsWith("# ") || limpia.startsWith("## ") || limpia.startsWith("### ");
    }

    /** Devuelve el texto del encabezado sin las almohadillas. */
    public static String textoEncabezado(String linea) {
        return linea == null ? "" : linea.trim().replaceFirst(MARCA_ENCABEZADO, "");
    }

    /** Indica si la línea es una cita. */
    public static boolean esCita(String linea) {
        return linea != null && linea.trim().startsWith("> ");
    }

    /** Devuelve el texto de la cita sin el marcador. */
    public static String textoCita(String linea) {
        String limpia = linea == null ? "" : linea.trim();
        return limpia.length() > 2 ? limpia.substring(2).trim() : "";
    }

    /** Indica si la línea es un separador. */
    public static boolean esSeparador(String linea) {
        return linea != null && linea.trim().equals("---");
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
        return insertarAlInicioDeLinea(texto, cursor, MARCA_TAREA);
    }

    /**
     * Inserta una marca al principio de la línea del cursor (por ejemplo una
     * viñeta, una lista numerada o una cita), añadiendo un salto si hace falta.
     *
     * @param texto  nota completa.
     * @param cursor posición del cursor.
     * @param marca  marca a insertar (incluye su espacio si lo lleva).
     * @return la nota con la marca insertada.
     */
    public static String insertarAlInicioDeLinea(String texto, int cursor, String marca) {
        String original = texto == null ? "" : texto;
        int posicion = Math.max(0, Math.min(cursor, original.length()));
        boolean alPrincipio = posicion == 0 || original.charAt(posicion - 1) == '\n';
        String insercion = (alPrincipio ? "" : "\n") + marca;
        return original.substring(0, posicion) + insercion + original.substring(posicion);
    }

    /**
     * Separa una línea en trozos con negrita, cursiva, tachado y código.
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
                    fragmentos.add(new Fragmento(texto.substring(i + 2, fin), true, false, false, false));
                    i = fin + 2;
                    continue;
                }
            }
            if (texto.startsWith("~~", i)) {
                int fin = texto.indexOf("~~", i + 2);
                if (fin > i + 2) {
                    fragmentos.add(new Fragmento(texto.substring(i + 2, fin), false, false, true, false));
                    i = fin + 2;
                    continue;
                }
            }
            if (texto.charAt(i) == '`') {
                int fin = texto.indexOf('`', i + 1);
                if (fin > i + 1) {
                    fragmentos.add(new Fragmento(texto.substring(i + 1, fin), false, false, false, true));
                    i = fin + 1;
                    continue;
                }
            }
            if (texto.charAt(i) == '*') {
                int fin = texto.indexOf('*', i + 1);
                if (fin > i + 1) {
                    fragmentos.add(new Fragmento(texto.substring(i + 1, fin), false, true, false, false));
                    i = fin + 1;
                    continue;
                }
            }

            int siguiente = siguienteMarca(texto, i);
            if (siguiente == i) {
                fragmentos.add(new Fragmento(String.valueOf(texto.charAt(i)), false, false, false, false));
                i++;
            } else {
                fragmentos.add(new Fragmento(texto.substring(i, siguiente), false, false, false, false));
                i = siguiente;
            }
        }
        return fragmentos;
    }

    private static int siguienteMarca(String texto, int desde) {
        int fin = texto.length();
        for (char marca : new char[]{'*', '~', '`'}) {
            int posicion = texto.indexOf(marca, desde);
            if (posicion != -1 && posicion < fin) {
                fin = posicion;
            }
        }
        return fin;
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
