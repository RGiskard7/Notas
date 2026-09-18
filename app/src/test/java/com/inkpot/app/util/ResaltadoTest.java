package com.inkpot.app.util;

import static org.junit.Assert.assertEquals;

import android.text.Spanned;
import android.text.style.BackgroundColorSpan;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ResaltadoTest {
    @Test
    public void resaltaLasCoincidencias() {
        CharSequence resultado = Resaltado.resaltar("Compra de leche y leche", "leche", 0x66FFEB3B);

        Spanned spanned = (Spanned) resultado;
        BackgroundColorSpan[] spans = spanned.getSpans(0, spanned.length(), BackgroundColorSpan.class);
        assertEquals(2, spans.length);
        assertEquals("leche", spanned.subSequence(spanned.getSpanStart(spans[0]), spanned.getSpanEnd(spans[0])).toString());
    }

    @Test
    public void sinConsultaDevuelveElTextoOriginal() {
        CharSequence resultado = Resaltado.resaltar("Compra", "  ", 0x66FFEB3B);

        assertEquals("Compra", resultado.toString());
    }
}
