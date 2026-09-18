package com.inkpot.app.util;

/**
 * Prepara el texto que escribe el usuario para el índice FTS de SQLite.
 *
 * <p>FTS4 tiene su propia sintaxis y falla con ciertos caracteres, así que se
 * limpian los términos, se buscan por prefijo y se unen (AND implícito). Si la
 * consulta no contiene ningún término se devuelve cadena vacía, que el llamador
 * interpreta como "sin búsqueda".</p>
 */
public final class ConsultaFts {

    private ConsultaFts() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * Convierte el texto del usuario en una consulta válida para {@code MATCH}.
     *
     * @param consulta texto tal cual lo escribió el usuario.
     * @return la consulta preparada, o cadena vacía si no hay términos.
     */
    public static String paraMatch(String consulta) {
        if (consulta == null) {
            return "";
        }

        StringBuilder resultado = new StringBuilder();
        for (String token : consulta.trim().split("\\s+")) {
            String limpio = token.replaceAll("[^\\p{L}\\p{N}]", "");
            if (limpio.isEmpty()) {
                continue;
            }
            if (resultado.length() > 0) {
                resultado.append(' ');
            }
            resultado.append(limpio).append('*');
        }
        return resultado.toString();
    }
}
