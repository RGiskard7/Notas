package com.example.notas;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.notas.ajustes.Preferencias;

/**
 * Aplicación: aplica el tema guardado antes de abrir ninguna pantalla.
 */
public class NotasApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(Preferencias.getTema(this));
    }
}
