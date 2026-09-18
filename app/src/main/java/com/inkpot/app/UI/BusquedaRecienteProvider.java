package com.inkpot.app.UI;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Guarda y ofrece las últimas búsquedas como sugerencias del buscador.
 */
public class BusquedaRecienteProvider extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = "com.inkpot.app.busquedas";
    public static final int MODO = DATABASE_MODE_QUERIES;

    public BusquedaRecienteProvider() {
        setupSuggestions(AUTHORITY, MODO);
    }
}
