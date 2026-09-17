package com.example.notas.UI;

import static org.junit.Assert.assertEquals;

import android.app.Application;

import androidx.lifecycle.SavedStateHandle;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ListNotasViewModelTest {
    @Test
    public void laConsultaEmpiezaVacia() {
        Application app = ApplicationProvider.getApplicationContext();
        ListNotasViewModel viewModel = new ListNotasViewModel(app, new SavedStateHandle());

        assertEquals("", viewModel.getConsulta());
    }

    @Test
    public void laConsultaSeConservaAlRecrearElViewModel() {
        Application app = ApplicationProvider.getApplicationContext();
        SavedStateHandle estado = new SavedStateHandle();

        ListNotasViewModel primero = new ListNotasViewModel(app, estado);
        primero.setConsulta("compras");

        // Al recrear la pantalla se reutiliza el mismo estado guardado.
        ListNotasViewModel segundo = new ListNotasViewModel(app, estado);

        assertEquals("compras", segundo.getConsulta());
    }
}
