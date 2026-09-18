package com.example.notas.UI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.graphics.Typeface;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class RenderizadorNotaTest {
    @Test
    public void lasTareasSeDibujanConCasilla() {
        assertEquals("\u2610 comprar", RenderizadorNota.renderizar("- [ ] comprar", null).toString());
    }

    @Test
    public void lasTareasHechasSeDibujanMarcadas() {
        assertEquals("\u2611 hecho", RenderizadorNota.renderizar("- [x] hecho", null).toString());
    }

    @Test
    public void laNegritaAplicaEstilo() {
        Spanned texto = (Spanned) RenderizadorNota.renderizar("**hola**", null);

        StyleSpan[] spans = texto.getSpans(0, texto.length(), StyleSpan.class);
        assertEquals(1, spans.length);
        assertEquals(Typeface.BOLD, spans[0].getStyle());
        assertEquals("hola", texto.toString());
    }

    @Test
    public void pulsarUnaTareaAvisaConSuLinea() {
        final int[] pulsada = {-1};
        Spanned texto = (Spanned) RenderizadorNota.renderizar("a\n- [ ] b", new RenderizadorNota.OnTareaPulsada() {
            @Override
            public void onTareaPulsada(int numeroLinea) {
                pulsada[0] = numeroLinea;
            }
        });

        ClickableSpan[] spans = texto.getSpans(0, texto.length(), ClickableSpan.class);
        assertEquals(1, spans.length);
        spans[0].onClick(null);
        assertEquals(1, pulsada[0]);
    }

    @Test
    public void elTachadoSeAplica() {
        Spanned texto = (Spanned) RenderizadorNota.renderizar("~~hola~~", null);

        assertEquals("hola", texto.toString());
        assertEquals(1, texto.getSpans(0, texto.length(), StrikethroughSpan.class).length);
    }

    @Test
    public void elEncabezadoSeAgranda() {
        Spanned texto = (Spanned) RenderizadorNota.renderizar("## Titulo", null);

        assertEquals("Titulo", texto.toString());
        assertTrue(texto.getSpans(0, texto.length(), RelativeSizeSpan.class).length > 0);
    }
}
