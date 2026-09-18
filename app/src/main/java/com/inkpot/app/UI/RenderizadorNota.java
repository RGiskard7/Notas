package com.inkpot.app.UI;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.View;

import com.inkpot.app.util.FormatoNota;

import java.util.List;

/**
 * Convierte el texto de una nota en texto con estilo.
 *
 * <p>Dibuja las tareas con casilla (marcada o no, tachadas si están hechas), los
 * encabezados y las citas, y aplica negrita, cursiva, tachado y código. Las
 * tareas se pueden pulsar para cambiar su estado si se pasa un aviso.</p>
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
            } else if (FormatoNota.esEncabezado(linea.contenido)) {
                agregarEncabezado(resultado, linea.contenido);
            } else if (FormatoNota.esCita(linea.contenido)) {
                agregarCita(resultado, linea.contenido);
            } else if (FormatoNota.esSeparador(linea.contenido)) {
                resultado.append("\u2014\u2014\u2014");
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

    private static void agregarEncabezado(SpannableStringBuilder builder, String linea) {
        int inicio = builder.length();
        agregarFormato(builder, FormatoNota.textoEncabezado(linea));
        int fin = builder.length();
        builder.setSpan(new RelativeSizeSpan(1.3f), inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new StyleSpan(Typeface.BOLD), inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void agregarCita(SpannableStringBuilder builder, String linea) {
        int inicio = builder.length();
        builder.append("\u2502 ");
        agregarFormato(builder, FormatoNota.textoCita(linea));
        int fin = builder.length();
        builder.setSpan(new StyleSpan(Typeface.ITALIC), inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void agregarFormato(SpannableStringBuilder builder, String texto) {
        for (FormatoNota.Fragmento fragmento : FormatoNota.formatearEnLinea(texto)) {
            int inicio = builder.length();
            builder.append(fragmento.texto);
            int fin = builder.length();

            if (fragmento.negrita) {
                builder.setSpan(new StyleSpan(Typeface.BOLD), inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (fragmento.cursiva) {
                builder.setSpan(new StyleSpan(Typeface.ITALIC), inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (fragmento.tachado) {
                builder.setSpan(new StrikethroughSpan(), inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (fragmento.codigo) {
                builder.setSpan(new TypefaceSpan("monospace"), inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
}
