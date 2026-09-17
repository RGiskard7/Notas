package com.example.notas.data.room;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Base de datos Room de la aplicación.
 *
 * <p>Agrupa las entidades y expone los DAOs. Se guarda una única instancia por
 * nombre de fichero para no abrir varias conexiones.</p>
 *
 * <p>Al crearse inserta la libreta {@code Default} (id 1), que no se puede
 * borrar. Las claves foráneas se activan al abrir la base de datos.</p>
 */
@Database(
        entities = {
                LibretaEntity.class,
                NotaEntity.class,
                EtiquetaEntity.class,
                LibretaNotaCrossRef.class,
                EtiquetaNotaCrossRef.class
        },
        version = 2,
        exportSchema = true)
public abstract class NotasDatabase extends RoomDatabase {

    /**
     * Migración de la versión 1 a la 2.
     *
     * <p>La versión 1 guardaba las fechas como texto; a partir de la 2 son
     * milisegundos. Como cambia el tipo de las columnas, hay que recrear las
     * tablas y copiar los datos convirtiendo las fechas.</p>
     */
    static final Migration MIGRACION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            migrarLibretas(db);
            migrarEtiquetas(db);
            migrarNotas(db);
        }
    };

    public abstract NotaDao notaDao();

    public abstract LibretaDao libretaDao();

    public abstract EtiquetaDao etiquetaDao();

    private static final Map<String, NotasDatabase> INSTANCES = new HashMap<>();
    private static boolean permitirMainThreadParaTests = false;

    @VisibleForTesting
    public static void permitirConsultasEnMainThreadParaTests() {
        permitirMainThreadParaTests = true;
    }

    @VisibleForTesting
    public static synchronized void resetParaTests() {
        permitirMainThreadParaTests = false;
        for (NotasDatabase db : INSTANCES.values()) {
            if (db.isOpen()) {
                db.close();
            }
        }
        INSTANCES.clear();
    }

    public static synchronized NotasDatabase get(Context context, String name) {
        NotasDatabase db = INSTANCES.get(name);
        if (db == null) {
            final Context appContext = context.getApplicationContext();
            RoomDatabase.Builder<NotasDatabase> builder = Room.databaseBuilder(appContext, NotasDatabase.class, name)
                    .addMigrations(MIGRACION_1_2)
                    .addCallback(new Callback() {
                        @Override
                        public void onOpen(@NonNull SupportSQLiteDatabase database) {
                            super.onOpen(database);
                            database.execSQL("PRAGMA foreign_keys = ON");
                        }

                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase database) {
                            super.onCreate(database);
                            long ahora = System.currentTimeMillis();
                            database.execSQL(
                                    "INSERT INTO libretas (libreta_id, titulo, fecha_creacion, fecha_modificacion) " +
                                            "VALUES (1, 'Default', ?, ?)",
                                    new Object[]{ahora, ahora});
                        }
                    });
            if (permitirMainThreadParaTests) {
                builder.allowMainThreadQueries();
            }
            db = builder.build();
            INSTANCES.put(name, db);
        }
        return db;
    }

    public static synchronized void close(String name) {
        NotasDatabase db = INSTANCES.remove(name);
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    private static void migrarLibretas(SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE libretas_new (libreta_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "titulo TEXT NOT NULL, fecha_creacion INTEGER NOT NULL, fecha_modificacion INTEGER NOT NULL)");
        Cursor cursor = db.query("SELECT libreta_id, titulo, fecha_creacion, fecha_modificacion FROM libretas");
        while (cursor.moveToNext()) {
            db.execSQL("INSERT INTO libretas_new (libreta_id, titulo, fecha_creacion, fecha_modificacion) VALUES (?, ?, ?, ?)",
                    new Object[]{cursor.getInt(0), cursor.getString(1),
                            aEpoch(cursor.getString(2), false), aEpoch(cursor.getString(3), false)});
        }
        cursor.close();
        db.execSQL("DROP TABLE libretas");
        db.execSQL("ALTER TABLE libretas_new RENAME TO libretas");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_libretas_titulo ON libretas (titulo)");
    }

    private static void migrarEtiquetas(SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE etiquetas_new (etiqueta_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "titulo TEXT NOT NULL, fecha_creacion INTEGER NOT NULL, fecha_modificacion INTEGER NOT NULL)");
        Cursor cursor = db.query("SELECT etiqueta_id, titulo, fecha_creacion, fecha_modificacion FROM etiquetas");
        while (cursor.moveToNext()) {
            db.execSQL("INSERT INTO etiquetas_new (etiqueta_id, titulo, fecha_creacion, fecha_modificacion) VALUES (?, ?, ?, ?)",
                    new Object[]{cursor.getInt(0), cursor.getString(1),
                            aEpoch(cursor.getString(2), false), aEpoch(cursor.getString(3), false)});
        }
        cursor.close();
        db.execSQL("DROP TABLE etiquetas");
        db.execSQL("ALTER TABLE etiquetas_new RENAME TO etiquetas");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_etiquetas_titulo ON etiquetas (titulo)");
    }

    private static void migrarNotas(SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE notas_new (nota_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "titulo TEXT NOT NULL, texto TEXT, fecha_creacion INTEGER NOT NULL, fecha_modificacion INTEGER NOT NULL)");
        Cursor cursor = db.query("SELECT nota_id, titulo, texto, fecha_creacion, fecha_modificacion FROM notas");
        while (cursor.moveToNext()) {
            db.execSQL("INSERT INTO notas_new (nota_id, titulo, texto, fecha_creacion, fecha_modificacion) VALUES (?, ?, ?, ?, ?)",
                    new Object[]{cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                            aEpoch(cursor.getString(3), true), aEpoch(cursor.getString(4), true)});
        }
        cursor.close();
        db.execSQL("DROP TABLE notas");
        db.execSQL("ALTER TABLE notas_new RENAME TO notas");
    }

    /**
     * Convierte una fecha guardada como texto al formato antiguo a milisegundos.
     * Si el valor no se puede interpretar se usa la fecha actual.
     */
    private static long aEpoch(String valor, boolean conHora) {
        if (valor == null) {
            return System.currentTimeMillis();
        }
        try {
            String formato = conHora ? "dd/MM/yyyy - HH:mm" : "dd/MM/yyyy";
            return new SimpleDateFormat(formato, Locale.getDefault()).parse(valor).getTime();
        } catch (ParseException e) {
            return System.currentTimeMillis();
        }
    }
}
