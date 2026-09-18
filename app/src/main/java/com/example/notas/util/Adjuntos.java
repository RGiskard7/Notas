package com.example.notas.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

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

    /** Nombre original del fichero elegido, o "imagen" si no se puede leer. */
    public static String nombreFichero(Context context, Uri uri) {
        String nombre = null;
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columna = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (columna >= 0) {
                    nombre = cursor.getString(columna);
                }
            }
        }
        return nombre == null ? "imagen" : nombre;
    }

    /** Tipo de contenido del fichero elegido, o "image/*" si no se puede leer. */
    public static String mime(Context context, Uri uri) {
        String tipo = context.getContentResolver().getType(uri);
        return tipo == null ? "image/*" : tipo;
    }
}
