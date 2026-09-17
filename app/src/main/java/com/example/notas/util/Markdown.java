package com.example.notas.util;

/**
 * Conversión entre notas y Markdown.
 *
 * <p>El formato es deliberadamente sencillo para que los ficheros se puedan
 * abrir en cualquier editor: un título como encabezado de nivel 1 y, debajo, el
 * cuerpo de la nota. Al importar, si no hay encabezado se usa el nombre del
 * fichero como título.</p>
 */
public final class Markdown {

    private Markdown() {
        // Clase de utilidades: no se instancia.
    }

    /** Título y texto obtenidos al importar un Markdown. */
    public static class NotaMarkdown {
        public final String titulo;
        public final String texto;

        public NotaMarkdown(String titulo, String texto) {
            this.titulo = titulo;
            this.texto = texto;
        }
    }

    /**
     * Convierte una nota en texto Markdown.
     *
     * @param titulo título de la nota.
     * @param texto  contenido de la nota.
     * @return el Markdown resultante.
     */
    public static String exportar(String titulo, String texto) {
        String cuerpo = texto == null ? "" : texto;
        return "# " + (titulo == null ? "" : titulo) + "\n\n" + cuerpo + "\n";
    }

    /**
     * Interpreta el contenido de un fichero Markdown como una nota.
     *
     * @param contenido     texto del fichero.
     * @param nombreFichero nombre del fichero, para usarlo si no hay encabezado.
     * @return el título y el texto de la nota.
     */
    public static NotaMarkdown importar(String contenido, String nombreFichero) {
        String recortado = contenido == null ? "" : contenido.trim();
        String tituloEncabezado = null;
        String cuerpo = recortado;

        if (recortado.startsWith("# ")) {
            int finTitulo = recortado.indexOf('\n');
            String lineaTitulo = finTitulo == -1 ? recortado.substring(2) : recortado.substring(2, finTitulo);
            tituloEncabezado = lineaTitulo.trim();
            cuerpo = finTitulo == -1 ? "" : recortado.substring(finTitulo + 1).trim();
        }

        String titulo = (tituloEncabezado != null && !tituloEncabezado.isEmpty())
                ? tituloEncabezado
                : sinExtension(nombreFichero).trim();
        if (titulo.isEmpty()) {
            titulo = "Nota importada";
        }

        return new NotaMarkdown(titulo, cuerpo);
    }

    /**
     * Propone un nombre de fichero seguro a partir del título.
     *
     * @param titulo título de la nota.
     * @return el nombre con extensión {@code .md}.
     */
    public static String nombreFichero(String titulo) {
        String limpio = titulo == null ? "" : titulo.replaceAll("[^\\p{L}\\p{N} _-]", "").trim();
        if (limpio.isEmpty()) {
            limpio = "nota";
        }
        return limpio + ".md";
    }

    private static String sinExtension(String nombreFichero) {
        if (nombreFichero == null) {
            return "";
        }
        int punto = nombreFichero.lastIndexOf('.');
        return punto > 0 ? nombreFichero.substring(0, punto) : nombreFichero;
    }
}
