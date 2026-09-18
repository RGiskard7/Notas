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
                EtiquetaNotaCrossRef.class,
                NotaFts.class,
                AdjuntoEntity.class
        },
        version = 7,
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

    /**
     * Migración de la versión 2 a la 3: añade el índice FTS de búsqueda.
     *
     * <p>La tabla FTS es de contenido externo (apunta a {@code notas}), así que
     * hay que crearla, crear los disparadores que la mantienen sincronizada y
     * rellenarla con las notas que ya existían.</p>
     */
    static final Migration MIGRACION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS notas_fts USING FTS4(`titulo` TEXT, `texto` TEXT, content=`notas`)");
            db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_notas_fts_BEFORE_UPDATE " +
                    "BEFORE UPDATE ON `notas` BEGIN DELETE FROM `notas_fts` WHERE `docid`=OLD.`rowid`; END");
            db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_notas_fts_BEFORE_DELETE " +
                    "BEFORE DELETE ON `notas` BEGIN DELETE FROM `notas_fts` WHERE `docid`=OLD.`rowid`; END");
            db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_notas_fts_AFTER_UPDATE " +
                    "AFTER UPDATE ON `notas` BEGIN INSERT INTO `notas_fts`(`docid`, `titulo`, `texto`) " +
                    "VALUES (NEW.`rowid`, NEW.`titulo`, NEW.`texto`); END");
            db.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_notas_fts_AFTER_INSERT " +
                    "AFTER INSERT ON `notas` BEGIN INSERT INTO `notas_fts`(`docid`, `titulo`, `texto`) " +
                    "VALUES (NEW.`rowid`, NEW.`titulo`, NEW.`texto`); END");
            db.execSQL("INSERT INTO notas_fts(notas_fts) VALUES('rebuild')");
        }
    };

    /**
     * Migración de la versión 3 a la 4: añade la papelera.
     *
     * <p>Las notas no se borran de golpe; se marcan con la fecha en que se
     * enviaron a la papelera. El valor 0 significa que están activas.</p>
     */
    static final Migration MIGRACION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE notas ADD COLUMN eliminada_en INTEGER NOT NULL DEFAULT 0");
        }
    };

    /**
     * Migración de la versión 4 a la 5: añade los adjuntos de las notas.
     */
    static final Migration MIGRACION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `adjuntos` (" +
                    "`adjunto_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`nota_id` INTEGER NOT NULL, `ruta` TEXT NOT NULL, `nombre` TEXT, `mime` TEXT, " +
                    "`fecha` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`nota_id`) REFERENCES `notas`(`nota_id`) ON UPDATE CASCADE ON DELETE CASCADE )");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_adjuntos_nota_id` ON `adjuntos` (`nota_id`)");
        }
    };

    /**
     * Migración de la versión 5 a la 6: añade los recordatorios de las notas.
     *
     * <p>0 significa que la nota no tiene recordatorio.</p>
     */
    static final Migration MIGRACION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE notas ADD COLUMN recordatorio INTEGER NOT NULL DEFAULT 0");
        }
    };

    /**
     * Migración de la versión 6 a la 7: añade las notas fijadas y su color.
     *
     * <p>{@code fijada} vale 1 en las notas que se quedan arriba del listado y
     * {@code color} es el índice del color de fondo (0 = sin color).</p>
     */
    static final Migration MIGRACION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE notas ADD COLUMN fijada INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE notas ADD COLUMN color INTEGER NOT NULL DEFAULT 0");
        }
    };

    public abstract NotaDao notaDao();

    public abstract LibretaDao libretaDao();

    public abstract EtiquetaDao etiquetaDao();

    public abstract AdjuntoDao adjuntoDao();

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
                    .addMigrations(MIGRACION_1_2, MIGRACION_2_3, MIGRACION_3_4, MIGRACION_4_5, MIGRACION_5_6, MIGRACION_6_7)
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
