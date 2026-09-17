package com.example.notas.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Fechas {
    private static final String FORMATO_NOTA = "dd/MM/yyyy - HH:mm";
    private static final String FORMATO_FECHA = "dd/MM/yyyy";

    private Fechas() {
    }

    public static Date parse(String valor) {
        if (valor == null) {
            return new Date(0);
        }
        for (String formato : new String[]{FORMATO_NOTA, FORMATO_FECHA}) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(formato, Locale.getDefault());
                sdf.setLenient(false);
                return sdf.parse(valor);
            } catch (ParseException ignorada) {
                // se prueba el siguiente formato
            }
        }
        return new Date(0);
    }
}
