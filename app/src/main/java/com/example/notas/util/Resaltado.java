package com.example.notas.util;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

import java.util.Locale;

/**
 * Resalta las coincidencias de una búsqueda dentro de un texto.
 *
 * <p>Si el texto ya trae formato (por ejemplo el de la vista previa), los
 * intervalos se conservan y solo se añade el resaltado encima.</p>
 */
public final class Resaltado {

    private Resaltado() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * Devuelve el texto con las apariciones de la consulta resaltadas.
     *
     * @param texto    texto original (puede tener spans).
     * @param consulta texto buscado; si está vacío se devuelve el original.
     * @param color    color de fondo del resaltado.
     */
    public static CharSequence resaltar(CharSequence texto, String consulta, int color) {
        if (texto == null || consulta == null) {
            return texto;
        }
        String aguja = consulta.trim().toLowerCase(Locale.ROOT);
        if (aguja.isEmpty()) {
            return texto;
        }

        SpannableString resultado = new SpannableString(texto);
        String base = texto.toString().toLowerCase(Locale.ROOT);
        int desde = 0;
        int indice;
        while ((indice = base.indexOf(aguja, desde)) >= 0) {
            int fin = indice + aguja.length();
            resultado.setSpan(new StyleSpan(Typeface.BOLD), indice, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            resultado.setSpan(new BackgroundColorSpan(color), indice, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            desde = fin;
        }
        return resultado;
    }
}
