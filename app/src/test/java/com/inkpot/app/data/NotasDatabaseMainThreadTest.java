package com.inkpot.app.data;

import static org.junit.Assert.fail;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.inkpot.app.data.room.NotaDAORoom;
import com.inkpot.app.data.room.NotasDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class NotasDatabaseMainThreadTest {
    @Test
    public void consultaEnMainThread_lanzaIllegalStateException() {
        Context context = ApplicationProvider.getApplicationContext();
        NotasDatabase.resetParaTests();
        NotaDAORoom notaDAO = new NotaDAORoom(context, "test_mainthread");
        try {
            List<Nota> notas = new ArrayList<>();
            notaDAO.getAllNotas(notas);
            fail("Se esperaba IllegalStateException por consultar en el hilo principal");
        } catch (IllegalStateException esperada) {
            // producción no permite consultas en el hilo principal
        } finally {
            notaDAO.closeDB();
            context.deleteDatabase("test_mainthread");
        }
    }
}
