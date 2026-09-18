package com.inkpot.app.data;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;

import com.inkpot.app.data.room.NotasDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class NotasRepositoryAsyncTest {
    @Test
    public void consultaAsincrona_entregaElResultado() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        NotasRepository.resetParaTests();
        NotasDatabase.resetParaTests();
        NotasDatabase.permitirConsultasEnMainThreadParaTests();

        ILibretaDAO libretaDAO = FactoryDAO.getFactory(FactoryDAO.ROOM_FACTORY).getLibretaDao(context);
        libretaDAO.createLibreta("Trabajo");
        libretaDAO.closeDB();

        NotasRepository repositorio = NotasRepository.get(context);
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Libreta> resultado = new ArrayList<>();
        repositorio.libretas(new NotasRepository.Callback<List<Libreta>>() {
            @Override
            public void onResult(List<Libreta> valor) {
                resultado.addAll(valor);
                latch.countDown();
            }
        });

        long limite = System.currentTimeMillis() + 5000;
        while (latch.getCount() > 0 && System.currentTimeMillis() < limite) {
            shadowOf(Looper.getMainLooper()).idle();
            Thread.sleep(5);
        }

        assertEquals(0, latch.getCount());
        assertEquals(2, resultado.size());
        repositorio.cerrar();
    }
}
