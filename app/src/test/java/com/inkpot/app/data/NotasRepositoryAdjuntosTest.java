package com.inkpot.app.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.inkpot.app.data.room.NotasDatabase;
import com.inkpot.app.util.Adjuntos;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Comprueba que los ficheros de los adjuntos no se quedan huérfanos al borrarlos.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class NotasRepositoryAdjuntosTest {
    private Context context;
    private NotasRepository repositorio;
    private INotaDAO notaDAO;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        NotasRepository.resetParaTests();
        NotasDatabase.resetParaTests();
        NotasDatabase.permitirConsultasEnMainThreadParaTests();
        NotasRepository.modoSincronoParaTests();
        repositorio = NotasRepository.get(context);
        notaDAO = FactoryDAO.getFactory(FactoryDAO.ROOM_FACTORY).getNotaDao(context);
    }

    private File crearAdjunto(int idNota) throws IOException {
        File carpeta = Adjuntos.carpeta(context);
        assertTrue(carpeta.exists() || carpeta.mkdirs());
        File fichero = new File(carpeta, "prueba.png");
        try (FileOutputStream salida = new FileOutputStream(fichero)) {
            salida.write(new byte[]{1, 2, 3});
        }
        notaDAO.addAdjunto(idNota, fichero.getName(), "prueba.png", "image/png");
        return fichero;
    }

    private Adjunto primerAdjunto(int idNota) {
        final Adjunto[] guardado = new Adjunto[1];
        repositorio.adjuntosDeNota(idNota, new NotasRepository.Callback<List<Adjunto>>() {
            @Override
            public void onResult(List<Adjunto> valor) {
                guardado[0] = valor.get(0);
            }
        });
        return guardado[0];
    }

    @Test
    public void borrarNotaDefinitivamente_eliminaElFicheroDelAdjunto() throws IOException {
        int idNota = notaDAO.createNota("Con imagen", "x");
        File fichero = crearAdjunto(idNota);
        assertTrue(fichero.exists());

        repositorio.borrarNotaDefinitivamente(idNota, null);

        assertFalse(fichero.exists());
    }

    @Test
    public void eliminarAdjunto_eliminaElFichero() throws IOException {
        int idNota = notaDAO.createNota("Con imagen", "x");
        File fichero = crearAdjunto(idNota);

        repositorio.eliminarAdjunto(primerAdjunto(idNota), null);

        assertFalse(fichero.exists());
    }
}
