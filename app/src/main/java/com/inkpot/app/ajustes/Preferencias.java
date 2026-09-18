package com.inkpot.app.ajustes;

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
    private static final String CLAVE_ORDEN = "orden_notas";
    private static final String CLAVE_CUADRICULA = "cuadricula";

    /** Criterios de ordenación del listado de notas. */
    public static final String ORDEN_FECHA_CREACION_DESC = "fecha_creacion_desc";
    public static final String ORDEN_FECHA_CREACION_ASC = "fecha_creacion_asc";
    public static final String ORDEN_FECHA_MODIFICACION_DESC = "fecha_modificacion_desc";
    public static final String ORDEN_FECHA_MODIFICACION_ASC = "fecha_modificacion_asc";
    public static final String ORDEN_TITULO_ASC = "titulo_asc";
    public static final String ORDEN_TITULO_DESC = "titulo_desc";

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

    /** Criterio de ordenación elegido para el listado de notas. */
    public static String getOrdenNotas(Context context) {
        return prefs(context).getString(CLAVE_ORDEN, ORDEN_FECHA_CREACION_DESC);
    }

    /** Guarda el criterio de ordenación del listado de notas. */
    public static void setOrdenNotas(Context context, String orden) {
        prefs(context).edit().putString(CLAVE_ORDEN, orden).apply();
    }

    /** Indica si el listado de notas se muestra en cuadrícula. */
    public static boolean getCuadricula(Context context) {
        return prefs(context).getBoolean(CLAVE_CUADRICULA, false);
    }

    /** Guarda si el listado de notas se muestra en cuadrícula. */
    public static void setCuadricula(Context context, boolean cuadricula) {
        prefs(context).edit().putBoolean(CLAVE_CUADRICULA, cuadricula).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
