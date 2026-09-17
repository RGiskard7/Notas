package com.example.notas.data.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Database(
        entities = {
                LibretaEntity.class,
                NotaEntity.class,
                EtiquetaEntity.class,
                LibretaNotaCrossRef.class,
                EtiquetaNotaCrossRef.class
        },
        version = 1,
        exportSchema = false)
public abstract class NotasDatabase extends RoomDatabase {
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
                    .fallbackToDestructiveMigration()
                    .addCallback(new Callback() {
                        @Override
                        public void onOpen(@NonNull SupportSQLiteDatabase database) {
                            super.onOpen(database);
                            database.execSQL("PRAGMA foreign_keys = ON");
                        }

                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase database) {
                            super.onCreate(database);
                            String hoy = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    .format(Calendar.getInstance().getTime());
                            database.execSQL(
                                    "INSERT INTO libretas (libreta_id, titulo, fecha_creacion, fecha_modificacion) " +
                                            "VALUES (1, 'Default', ?, ?)",
                                    new Object[]{hoy, hoy});
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
}
