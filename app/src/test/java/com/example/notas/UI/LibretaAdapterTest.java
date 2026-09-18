package com.example.notas.UI;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ContextThemeWrapper;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.example.notas.R;
import com.example.notas.data.Libreta;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class LibretaAdapterTest {
    @Test
    public void elElementoTieneDescripcionParaLectorDePantalla() {
        Context context = new ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.AppTheme);
        List<Libreta> libretas = new ArrayList<>();
        libretas.add(new Libreta(1, "Trabajo", 3, 0L));

        LibretaAdapter adaptador = new LibretaAdapter(libretas, null);
        RecyclerView parent = new RecyclerView(context);
        parent.setLayoutManager(new LinearLayoutManager(context));
        LibretaAdapter.LibretaViewHolder holder = adaptador.onCreateViewHolder(parent, 0);
        adaptador.onBindViewHolder(holder, 0);

        String descripcion = holder.itemView.getContentDescription().toString();
        assertTrue(descripcion.contains("Trabajo"));
        assertTrue(descripcion.contains("3"));
    }
}
