package com.example.notas.util;

import com.example.notas.data.Nota;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Copia de seguridad de todas las notas.
 *
 * <p>Exporta un fichero ZIP con una nota en Markdown por entrada e importa el
 * mismo formato. Es la forma de mover todas las notas a la vez.</p>
 */
public final class Respaldo {

    private Respaldo() {
        // Clase de utilidades: no se instancia.
    }

    /** Escribe todas las notas como ficheros Markdown dentro de un ZIP. */
    public static void exportar(List<Nota> notas, OutputStream salida) throws IOException {
        Set<String> usados = new HashSet<>();
        try (ZipOutputStream zip = new ZipOutputStream(salida)) {
            for (Nota nota : notas) {
                zip.putNextEntry(new ZipEntry(nombreUnico(nota.getTitulo(), usados)));
                zip.write(Markdown.exportar(nota.getTitulo(), nota.getTexto()).getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
    }

    /** Lee un ZIP con ficheros Markdown y devuelve las notas que contiene. */
    public static List<Markdown.NotaMarkdown> importar(InputStream entrada) throws IOException {
        List<Markdown.NotaMarkdown> notas = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(entrada)) {
            ZipEntry entradaZip;
            while ((entradaZip = zip.getNextEntry()) != null) {
                if (entradaZip.isDirectory()) {
                    continue;
                }
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] datos = new byte[4096];
                int leidos;
                while ((leidos = zip.read(datos)) != -1) {
                    buffer.write(datos, 0, leidos);
                }
                String contenido = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
                notas.add(Markdown.importar(contenido, entradaZip.getName()));
            }
        }
        return notas;
    }

    /** Nombre de fichero único dentro del ZIP, sin repetir títulos. */
    private static String nombreUnico(String titulo, Set<String> usados) {
        String base = Markdown.nombreFichero(titulo);
        String nombre = base;
        int sufijo = 2;
        while (!usados.add(nombre)) {
            int punto = base.lastIndexOf('.');
            String sinExtension = punto > 0 ? base.substring(0, punto) : base;
            String extension = punto > 0 ? base.substring(punto) : "";
            nombre = sinExtension + " (" + sufijo + ")" + extension;
            sufijo++;
        }
        return nombre;
    }
}
