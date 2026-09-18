package com.example.notas.UI;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.GridLayout;

import androidx.core.content.ContextCompat;

import com.example.notas.R;
import com.example.notas.util.PaletaNotas;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Diálogo para elegir el color de fondo de una nota.
 *
 * <p>Muestra un círculo por color más la opción "sin color"; el elegido queda
 * marcado con un borde.</p>
 */
public final class DialogoColor {

    /** Recibe el índice del color elegido (0 = sin color). */
    public interface OnColorElegido {
        void onColor(int indice);
    }

    private DialogoColor() {
        // Clase de utilidades: no se instancia.
    }

    public static void mostrar(Context context, int seleccionado, final OnColorElegido callback) {
        float densidad = context.getResources().getDisplayMetrics().density;
        int lado = (int) (44 * densidad);
        int margen = (int) (8 * densidad);
        int padding = (int) (20 * densidad);
        int borde = (int) (2 * densidad);
        int bordeElegido = (int) (3 * densidad);
        int colorBorde = ContextCompat.getColor(context, R.color.primary);
        int colorBordeSuave = 0x33000000;

        GridLayout cuadricula = new GridLayout(context);
        cuadricula.setColumnCount(4);
        cuadricula.setPadding(padding, padding, padding, padding);

        for (int i = 0; i <= PaletaNotas.total(); i++) {
            final int indice = i;
            View circulo = new View(context);

            GradientDrawable fondo = new GradientDrawable();
            fondo.setShape(GradientDrawable.OVAL);
            if (i == 0) {
                fondo.setColor(0x00000000);
                fondo.setStroke(borde, i == seleccionado ? colorBorde : 0x889E9E9E);
            } else {
                fondo.setColor(PaletaNotas.color(context, i));
                fondo.setStroke(i == seleccionado ? bordeElegido : borde, i == seleccionado ? colorBorde : colorBordeSuave);
            }
            circulo.setBackground(fondo);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = lado;
            params.height = lado;
            params.setMargins(margen, margen, margen, margen);
            circulo.setLayoutParams(params);
            circulo.setContentDescription(i == 0
                    ? context.getString(R.string.sin_color)
                    : context.getString(R.string.color_nota) + " " + i);
            circulo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onColor(indice);
                }
            });
            cuadricula.addView(circulo);
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.color_nota)
                .setView(cuadricula)
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }
}
