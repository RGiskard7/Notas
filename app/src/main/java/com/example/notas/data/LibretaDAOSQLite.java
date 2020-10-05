package com.example.notas.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LibretaDAOSQLite implements ILibretaDAO {
    private SQLiteDatabase db;
    private final SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy");

    public LibretaDAOSQLite(Context context, String name, SQLiteDatabase.CursorFactory factory, int versionDB) {
        DB dbNotas = new DB(context, name, factory, versionDB); // Conectar a base de datos
        db = dbNotas.getWritableDatabase(); // Poner base de datos en modo escritura
    }

    @Override
    public void closeDB() {
        db.close();
    }

    @Override
    public void createLibreta(String titulo) {
        titulo = titulo.replace("'", "''");
        db.execSQL("INSERT INTO libretas (titulo, fecha_creacion) VALUES ('" + titulo + "','" +
                dtf.format(Calendar.getInstance().getTime()) + "')"); // Añadir nueva libreta
    }

    @Override
    public Boolean existTitulo(String titulo) {
        titulo = titulo.replace("'", "''");
        Cursor c = db.rawQuery("select count(*) from libretas where titulo = ?", new String[]{titulo});
        c.moveToFirst();
        int num = c.getInt(0);
        c.close();

        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Libreta getLibreta(int id) {
        Cursor cursor = db.rawQuery("SELECT * FROM " +
                "libretas WHERE libreta_id = ? ", new String[]{Integer.toString(id)});
        cursor.moveToFirst();

        String titulo = cursor.getString(1);
        String fechaCreacion = cursor.getString(2);

        cursor.close();

        return new Libreta(id, titulo, fechaCreacion);
    }

    @Override
    public void deleteLibreta(int id) {
        db.execSQL("DELETE FROM libretas WHERE libreta_id = '" + id + "'"); // Eliminar libreta por id
        db.execSQL("DELETE FROM libretaNotas WHERE libreta_id = '" + id + "'");
    }

    @Override
    public void addNotaToLibreta(int idLibreta, int idNota) {
        db.execSQL("INSERT INTO libretaNotas (libreta_id, nota_id) VALUES ('" + idLibreta + "','" + idNota + "')");
    }

    @Override
    public void editLibreta(int id, String titulo) {
        titulo = titulo.replace("'", "''");
        db.execSQL("UPDATE libretas SET titulo = '" + titulo + "',"  +  "fecha_creacion = '" + dtf.format(Calendar.getInstance().getTime()) +
                "' WHERE libreta_id = '" + id + "'"); // Actualizar libreta
    }

    @Override
    public void getAllLibretas(List<Libreta> list) {
        list.clear();
        Libreta libreta;
        List<Nota> listNotasLibreta;
        Cursor cursor = db.rawQuery("SELECT libreta_id, titulo, fecha_creacion FROM libretas", null);

        if (cursor.moveToFirst()) {
            do {
                libreta = new Libreta(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
                listNotasLibreta = new ArrayList<>();
                getAllNotasFrom(libreta.getId(), listNotasLibreta);
                libreta.setNotas(listNotasLibreta);
                list.add(libreta);
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    @Override
    public void getAllNotasFrom(int idLibreta, List<Nota> list) {
        list.clear();
        List<Etiqueta> etiquetasNota;
        Cursor cursor2;
        Cursor cursor = db.rawQuery("SELECT DISTINCT notas.nota_id, notas.titulo, notas.texto, notas.fecha_creacion FROM " +
                "libretaNotas NATURAL JOIN notas WHERE libreta_id = ? ", new String[]{Integer.toString(idLibreta)});

        if (cursor.moveToFirst()) {
            do {
                etiquetasNota = new ArrayList<>();
                cursor2 = db.rawQuery("SELECT DISTINCT etiquetas.etiqueta_id, etiquetas.titulo, etiquetas.fecha_creacion FROM " +
                        "etiquetaNotas NATURAL JOIN etiquetas WHERE nota_id = ? ", new String[]{Integer.toString(cursor.getInt(0))});

                if (cursor2.moveToFirst()) {
                    do {
                        etiquetasNota.add(new Etiqueta(cursor2.getInt(0), cursor2.getString(1), cursor2.getString(2)));
                    } while (cursor2.moveToNext());
                }

                cursor2.close();
                list.add(new Nota(cursor.getInt(0), cursor.getString(1), cursor.getString(2), getLibreta(idLibreta), etiquetasNota, cursor.getString(3)));
            } while (cursor.moveToNext());

            cursor.close();
        }
    }
}
