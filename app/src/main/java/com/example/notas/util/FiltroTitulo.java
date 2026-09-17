package com.example.notas.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Filtra listas por el título de sus elementos, sin distinguir mayúsculas de
 * minúsculas. Se usa para la búsqueda de los listados.
 */
public final class FiltroTitulo {
    private FiltroTitulo() {
    }

    /** Devuelve el título de un elemento, para poder filtrar listas de tipos distintos. */
    public interface TituloProvider<T> {
        String titulo(T item);
    }

    /**
     * Devuelve los elementos cuyo título contiene el texto buscado. Si la
     * consulta está vacía o es nula se devuelven todos.
     *
     * @param origen   lista de la que se parte.
     * @param consulta texto a buscar.
     * @param provider forma de obtener el título de cada elemento.
     * @param <T>      tipo de los elementos.
     * @return una lista nueva con los elementos que coinciden.
     */
    public static <T> List<T> filtrar(List<T> origen, String consulta, TituloProvider<T> provider) {
        List<T> resultado = new ArrayList<>();
        String busqueda = consulta == null ? "" : consulta.trim().toLowerCase(Locale.ROOT);

        if (busqueda.isEmpty()) {
            resultado.addAll(origen);
            return resultado;
        }

        for (T item : origen) {
            String titulo = provider.titulo(item);
            if (titulo != null && titulo.toLowerCase(Locale.ROOT).contains(busqueda)) {
                resultado.add(item);
            }
        }
        return resultado;
    }
}
