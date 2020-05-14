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
    public void createLibreta(String titulo) {
        db.execSQL("INSERT INTO libretas (titulo, fecha_creacion) VALUES ('" + titulo + "','" +
                dtf.format(Calendar.getInstance().getTime()) + "')"); // Añadir nueva libreta
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
        db.execSQL("UPDATE libretas SET titulo = '" + titulo + "',"  +  "fecha_creacion = '" + dtf.format(Calendar.getInstance().getTime()) +
                "' WHERE libreta_id = '" + id + "'"); // Actualizar libreta
    }

    @Override
    public void getAllLibretas(List<Libreta> list) {
        list.clear();
        Cursor cursor = db.rawQuery("SELECT libreta_id, titulo, fecha_creacion FROM libretas", null);

        if (cursor.moveToFirst()) {
            do {
                Libreta libreta = new Libreta(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
                List<Nota> listNotasLibreta = new ArrayList<>();
                getAllNotasFrom(libreta.getId(), listNotasLibreta);
                libreta.setNotas(listNotasLibreta);
                list.add(libreta);
            } while (cursor.moveToNext());
        }
    }

    @Override
    public void getAllNotasFrom(int idLibreta, List<Nota> list) {
        list.clear();
        Cursor cursor = db.rawQuery("SELECT DISTINCT notas.nota_id, notas.titulo, notas.texto, notas.fecha_creacion FROM " +
                "libretaNotas NATURAL JOIN notas WHERE libreta_id = ? ", new String[]{Integer.toString(idLibreta)});

        if (cursor.moveToFirst()) {
            do {
                list.add(new Nota(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)));
            } while (cursor.moveToNext());
        }
    }
}
