package com.example.notas;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.notas.ajustes.Preferencias;
import com.google.android.material.color.DynamicColors;

/**
 * Aplicación: aplica el tema guardado y el color dinámico antes de abrir
 * ninguna pantalla.
 */
public class NotasApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(Preferencias.getTema(this));
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
