package com.example.notas.UI;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.View;

import com.example.notas.util.FormatoNota;

import java.util.List;

/**
 * Convierte el texto de una nota en texto con estilo.
 *
 * <p>Las tareas se dibujan con una casilla (marcada o no, tachadas si están
 * hechas) y se pueden pulsar para cambiar su estado. La negrita y la cursiva se
 * aplican con estilos de texto.</p>
 */
public final class RenderizadorNota {

    /** Aviso de que el usuario ha pulsado la tarea de una línea. */
    public interface OnTareaPulsada {
        void onTareaPulsada(int numeroLinea);
    }

    private RenderizadorNota() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * Da formato al texto de una nota.
     *
     * @param texto    contenido de la nota.
     * @param listener se avisa al pulsar una tarea (puede ser null).
     * @return el texto listo para mostrar.
     */
    public static CharSequence renderizar(String texto, final OnTareaPulsada listener) {
        SpannableStringBuilder resultado = new SpannableStringBuilder();
        List<FormatoNota.Linea> lineas = FormatoNota.parsear(texto);

        for (int i = 0; i < lineas.size(); i++) {
            if (i > 0) {
                resultado.append('\n');
            }
            FormatoNota.Linea linea = lineas.get(i);
            if (linea.tarea) {
                agregarTarea(resultado, linea, i, listener);
            } else {
                agregarFormato(resultado, linea.contenido);
            }
        }
        return resultado;
    }

    private static void agregarTarea(SpannableStringBuilder builder, FormatoNota.Linea linea,
                                     final int numeroLinea, final OnTareaPulsada listener) {
        int inicio = builder.length();
        builder.append(linea.hecha ? FormatoNota.CASILLA_HECHA : FormatoNota.CASILLA_VACIA).append(' ');
        agregarFormato(builder, linea.contenido);
        int fin = builder.length();

        if (linea.hecha) {
            builder.setSpan(new StrikethroughSpan(), inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (listener != null) {
            builder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    listener.onTareaPulsada(numeroLinea);
                }
            }, inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static void agregarFormato(SpannableStringBuilder builder, String texto) {
        for (FormatoNota.Fragmento fragmento : FormatoNota.formatearEnLinea(texto)) {
            int inicio = builder.length();
            builder.append(fragmento.texto);
            if (fragmento.negrita) {
                builder.setSpan(new StyleSpan(Typeface.BOLD), inicio, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (fragmento.cursiva) {
                builder.setSpan(new StyleSpan(Typeface.ITALIC), inicio, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
}
