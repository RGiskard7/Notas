package com.inkpot.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utilidades para dar formato a las fechas almacenadas como milisegundos
 * (epoch). En la base de datos se guardan como enteros y aquí se convierten
 * a texto para mostrarlas en la interfaz.
 */
public final class Fechas {

    /** Formato completo usado por las notas (día, mes, año, hora y minutos). */
    private static final String FORMATO_NOTA = "dd/MM/yyyy - HH:mm";

    /** Formato corto usado por libretas y etiquetas. */
    private static final String FORMATO_FECHA = "dd/MM/yyyy";

    private Fechas() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * Da formato de nota (fecha y hora) a una marca de tiempo.
     *
     * @param millis milisegundos desde el 1 de enero de 1970.
     * @return la fecha formateada como "dd/MM/yyyy - HH:mm".
     */
    public static String formatearNota(long millis) {
        return new SimpleDateFormat(FORMATO_NOTA, Locale.getDefault()).format(new Date(millis));
    }

    /**
     * Da formato corto (solo fecha) a una marca de tiempo.
     *
     * @param millis milisegundos desde el 1 de enero de 1970.
     * @return la fecha formateada como "dd/MM/yyyy".
     */
    public static String formatearFecha(long millis) {
        return new SimpleDateFormat(FORMATO_FECHA, Locale.getDefault()).format(new Date(millis));
    }
}
