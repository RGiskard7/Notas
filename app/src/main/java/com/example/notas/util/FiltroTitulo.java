package com.example.notas.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FiltroTitulo {
    private FiltroTitulo() {
    }

    public interface TituloProvider<T> {
        String titulo(T item);
    }

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
