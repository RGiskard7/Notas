package com.inkpot.app.util;

import android.content.Context;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.inkpot.app.R;

/**
 * Paleta de colores de fondo de las notas.
 *
 * <p>El índice 0 significa "sin color"; los demás apuntan a los recursos
 * {@code nota_*} (pasteles en modo claro, tonos apagados en oscuro).</p>
 */
public final class PaletaNotas {

    private static final int[] RECURSOS = {
            R.color.nota_amarillo,
            R.color.nota_verde,
            R.color.nota_azul,
            R.color.nota_rosa,
            R.color.nota_morado,
            R.color.nota_naranja
    };

    private PaletaNotas() {
        // Clase de utilidades: no se instancia.
    }

    /** Número de colores disponibles, sin contar "sin color". */
    public static int total() {
        return RECURSOS.length;
    }

    /** Recurso de color del índice (1..total), o 0 si no es válido. */
    @ColorRes
    public static int recurso(int indice) {
        return indice >= 1 && indice <= RECURSOS.length ? RECURSOS[indice - 1] : 0;
    }

    /** Color resuelto del índice, o 0 si el índice es "sin color" o no es válido. */
    public static int color(Context context, int indice) {
        int recurso = recurso(indice);
        return recurso == 0 ? 0 : ContextCompat.getColor(context, recurso);
    }
}
