package com.example.notas.ajustes;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Preferencias de la aplicación.
 *
 * <p>De momento solo guarda el tema elegido (según el sistema, claro u oscuro).
 * Los valores coinciden con las constantes de {@link AppCompatDelegate}.</p>
 */
public final class Preferencias {

    private static final String PREFS = "ajustes";
    private static final String CLAVE_TEMA = "tema";

    private Preferencias() {
        // Clase de utilidades: no se instancia.
    }

    /** Tema elegido, o "según el sistema" si no se ha cambiado nunca. */
    public static int getTema(Context context) {
        return prefs(context).getInt(CLAVE_TEMA, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    /** Guarda el tema elegido. */
    public static void setTema(Context context, int modo) {
        prefs(context).edit().putInt(CLAVE_TEMA, modo).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
