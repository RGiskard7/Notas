package com.example.notas.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.core.app.ApplicationProvider;

import com.example.notas.data.room.EtiquetaDAORoom;
import com.example.notas.data.room.LibretaDAORoom;
import com.example.notas.data.room.NotaDAORoom;
import com.example.notas.data.room.NotasDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprueba que la migración 1 -> 2 conserva los datos y convierte las fechas
 * que antes se guardaban como texto.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class Migracion1a2Test {
    private static final String DB_NAME = "test_migracion";
    private Context context;

    @Before
    public void setUp() throws IOException {
        context = ApplicationProvider.getApplicationContext();
        NotasDatabase.resetParaTests();
        NotasDatabase.permitirConsultasEnMainThreadParaTests();
        context.deleteDatabase(DB_NAME);
        crearBaseDeDatosVersion1();
    }

    @After
    public void tearDown() {
        NotasDatabase.resetParaTests();
        context.deleteDatabase(DB_NAME);
    }

    /** Crea a mano una base de datos tal y como la generaba la versión 1. */
    private void crearBaseDeDatosVersion1() throws IOException {
        SupportSQLiteOpenHelper.Configuration config = SupportSQLiteOpenHelper.Configuration
                .builder(context)
                .name(DB_NAME)
                .callback(new SupportSQLiteOpenHelper.Callback(1) {
                    @Override
                    public void onCreate(SupportSQLiteDatabase db) {
                        db.execSQL("CREATE TABLE libretas (libreta_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "titulo TEXT NOT NULL, fecha_creacion TEXT, fecha_modificacion TEXT)");
                        db.execSQL("CREATE TABLE notas (nota_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "titulo TEXT NOT NULL, texto TEXT, fecha_creacion TEXT, fecha_modificacion TEXT)");
                        db.execSQL("CREATE TABLE etiquetas (etiqueta_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "titulo TEXT NOT NULL, fecha_creacion TEXT, fecha_modificacion TEXT)");
                        db.execSQL("CREATE TABLE libretaNotas (libreta_id INTEGER NOT NULL, nota_id INTEGER NOT NULL, " +
                                "PRIMARY KEY(libreta_id, nota_id), " +
                                "FOREIGN KEY(libreta_id) REFERENCES libretas(libreta_id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                                "FOREIGN KEY(nota_id) REFERENCES notas(nota_id) ON UPDATE CASCADE ON DELETE CASCADE)");
                        db.execSQL("CREATE TABLE etiquetaNotas (etiqueta_id INTEGER NOT NULL, nota_id INTEGER NOT NULL, " +
                                "PRIMARY KEY(etiqueta_id, nota_id), " +
                                "FOREIGN KEY(etiqueta_id) REFERENCES etiquetas(etiqueta_id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                                "FOREIGN KEY(nota_id) REFERENCES notas(nota_id) ON UPDATE CASCADE ON DELETE CASCADE)");
                        db.execSQL("CREATE UNIQUE INDEX index_libretas_titulo ON libretas (titulo)");
                        db.execSQL("CREATE UNIQUE INDEX index_etiquetas_titulo ON etiquetas (titulo)");
                        db.execSQL("CREATE INDEX index_libretaNotas_nota_id ON libretaNotas (nota_id)");
                        db.execSQL("CREATE INDEX index_libretaNotas_libreta_id ON libretaNotas (libreta_id)");
                        db.execSQL("CREATE INDEX index_etiquetaNotas_nota_id ON etiquetaNotas (nota_id)");
                        db.execSQL("CREATE INDEX index_etiquetaNotas_etiqueta_id ON etiquetaNotas (etiqueta_id)");

                        db.execSQL("INSERT INTO libretas (libreta_id, titulo, fecha_creacion, fecha_modificacion) " +
                                "VALUES (1, 'Default', '01/01/2000', '01/01/2000')");
                        db.execSQL("INSERT INTO etiquetas (etiqueta_id, titulo, fecha_creacion, fecha_modificacion) " +
                                "VALUES (1, 'Urgente', '02/01/2000', '02/01/2000')");
                        db.execSQL("INSERT INTO notas (nota_id, titulo, texto, fecha_creacion, fecha_modificacion) " +
                                "VALUES (1, 'Compra', 'leche', '03/01/2000 - 10:30', '04/01/2000 - 11:00')");
                        db.execSQL("INSERT INTO libretaNotas (libreta_id, nota_id) VALUES (1, 1)");
                        db.execSQL("INSERT INTO etiquetaNotas (etiqueta_id, nota_id) VALUES (1, 1)");
                    }

                    @Override
                    public void onUpgrade(SupportSQLiteDatabase db, int oldVersion, int newVersion) {
                    }
                })
                .build();

        SupportSQLiteOpenHelper helper = new FrameworkSQLiteOpenHelperFactory().create(config);
        helper.getWritableDatabase().close();
    }

    @Test
    public void laMigracionConservaLosDatosYConvierteLasFechas() {
        NotaDAORoom notaDAO = new NotaDAORoom(context, DB_NAME);
        LibretaDAORoom libretaDAO = new LibretaDAORoom(context, DB_NAME);
        EtiquetaDAORoom etiquetaDAO = new EtiquetaDAORoom(context, DB_NAME);

        Nota nota = notaDAO.getNota(1);
        assertNotNull(nota);
        assertEquals("Compra", nota.getTitulo());
        assertEquals("leche", nota.getTexto());
        assertEquals(1, nota.getLibreta().getId());
        assertTrue(nota.getFechaCreacion() > 0);
        assertTrue(nota.getFechaModificacion() > 0);
        assertEquals(1, nota.getEtiquetas().size());

        Libreta libreta = libretaDAO.getLibreta(1);
        assertEquals("Default", libreta.getTitulo());
        assertTrue(libreta.getFechaCreacion() > 0);

        Etiqueta etiqueta = etiquetaDAO.getEtiqueta(1);
        assertEquals("Urgente", etiqueta.getTitulo());
        assertTrue(etiqueta.getFechaCreacion() > 0);
    }

    @Test
    public void laMigracionRellenaElIndiceDeBusqueda() {
        NotaDAORoom notaDAO = new NotaDAORoom(context, DB_NAME);

        List<Nota> encontradas = new ArrayList<>();
        notaDAO.buscarNotas("leche*", -1, -1, encontradas);

        assertEquals(1, encontradas.size());
        assertEquals("Compra", encontradas.get(0).getTitulo());
    }
}
