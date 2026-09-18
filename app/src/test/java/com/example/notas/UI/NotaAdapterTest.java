package com.example.notas.UI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.ContextThemeWrapper;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.example.notas.R;
import com.example.notas.data.Etiqueta;
import com.example.notas.data.Nota;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class NotaAdapterTest {
    @Test
    public void laVistaPreviaRenderizaElFormato() {
        Context context = new ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.AppTheme);
        List<Nota> notas = new ArrayList<>();
        notas.add(new Nota(1, "Titulo", "**hola**", null, new ArrayList<Etiqueta>(), 0L));

        NotaAdapter adaptador = new NotaAdapter(notas, null);
        RecyclerView parent = new RecyclerView(context);
        parent.setLayoutManager(new LinearLayoutManager(context));
        NotaAdapter.NotaViewHolder holder = adaptador.onCreateViewHolder(parent, 0);
        adaptador.onBindViewHolder(holder, 0);

        TextView texto = holder.itemView.findViewById(R.id.textViewTexto);
        assertEquals("hola", texto.getText().toString());
        Spanned spanned = (Spanned) texto.getText();
        assertTrue(spanned.getSpans(0, spanned.length(), StyleSpan.class).length > 0);
    }
}
