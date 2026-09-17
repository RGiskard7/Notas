package com.example.notas.util;

import android.content.Context;

import java.io.File;

/**
 * Utilidades de los ficheros adjuntos.
 *
 * <p>Los adjuntos se copian a la carpeta privada de la aplicación para no
 * depender del permiso ni de la ubicación original del fichero.</p>
 */
public final class Adjuntos {

    private static final String CARPETA = "adjuntos";

    private Adjuntos() {
        // Clase de utilidades: no se instancia.
    }

    /** Carpeta donde se guardan los adjuntos. */
    public static File carpeta(Context context) {
        return new File(context.getFilesDir(), CARPETA);
    }

    /** Fichero de un adjunto a partir de su nombre. */
    public static File fichero(Context context, String ruta) {
        return new File(carpeta(context), ruta);
    }

    /** Extensión del fichero (incluido el punto), o cadena vacía. */
    public static String extension(String nombre) {
        if (nombre == null) {
            return "";
        }
        int punto = nombre.lastIndexOf('.');
        return punto > 0 ? nombre.substring(punto) : "";
    }
}
