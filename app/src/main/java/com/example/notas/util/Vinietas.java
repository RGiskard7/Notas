package com.example.notas.util;

/**
 * Lógica de inserción de viñetas en el editor de notas.
 */
public final class Vinietas {
    private static final char VINIETA = '\u2022';

    private Vinietas() {
    }

    /**
     * Indica si, al insertar una viñeta en la posición del cursor, hay que anteponer un salto
     * de línea: cuando ya hay un carácter imprimible justo antes o cuando lo que precede es
     * otra viñeta. Devuelve false si el cursor está fuera de rango, evitando accesos inválidos.
     */
    public static boolean necesitaSaltoDeLinea(CharSequence texto, int cursor) {
        if (texto == null || cursor <= 0 || cursor > texto.length()) {
            return false;
        }
        char anterior = texto.charAt(cursor - 1);
        boolean asciiImprimible = anterior >= 32 && anterior <= 255;
        boolean dosAntesEsVinieta = cursor >= 2 && texto.charAt(cursor - 2) == VINIETA;
        return asciiImprimible || dosAntesEsVinieta;
    }
}
