package com.inkpot.app.UI;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ContextThemeWrapper;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.inkpot.app.R;
import com.inkpot.app.data.Etiqueta;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class EtiquetaAdapterTest {
    @Test
    public void elElementoTieneDescripcionParaLectorDePantalla() {
        Context context = new ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.AppTheme);
        List<Etiqueta> etiquetas = new ArrayList<>();
        etiquetas.add(new Etiqueta(1, "Urgente", 2, 0L));

        EtiquetaAdapter adaptador = new EtiquetaAdapter(etiquetas, null);
        RecyclerView parent = new RecyclerView(context);
        parent.setLayoutManager(new LinearLayoutManager(context));
        EtiquetaAdapter.EtiquetaViewHolder holder = adaptador.onCreateViewHolder(parent, 0);
        adaptador.onBindViewHolder(holder, 0);

        String descripcion = holder.itemView.getContentDescription().toString();
        assertTrue(descripcion.contains("Urgente"));
        assertTrue(descripcion.contains("2"));
    }
}
