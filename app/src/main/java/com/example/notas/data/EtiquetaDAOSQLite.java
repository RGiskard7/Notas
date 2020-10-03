package com.example.notas.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.notas.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EtiquetaDAOSQLite implements IEtiquetaDAO {
    private SQLiteDatabase db;
    private final SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy - HH:mm");

    public EtiquetaDAOSQLite(Context context, String name, SQLiteDatabase.CursorFactory factory, int versionDB) {
        DB dbNotas = new DB(context, name, factory, versionDB); // Conectar a base de datos
        db = dbNotas.getWritableDatabase(); // Poner base de datos en modo escritura
    }

    @Override
    public void createEtiqueta(String titulo) {
        titulo = titulo.replace("'", "''");
        db.execSQL("INSERT INTO etiquetas (titulo, fecha_creacion) VALUES ('" + titulo + "','" +
                dtf.format(Calendar.getInstance().getTime()) + "')"); // Añadir nueva etiqueta
    }

    @Override
    public Etiqueta getEtiqueta(int id) {
        Cursor cursor = db.rawQuery("SELECT * FROM " +
                "etiquetas WHERE etiqueta_id = ? ", new String[]{Integer.toString(id)});
        cursor.moveToFirst();
        return new Etiqueta(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
    }

    @Override
    public void getAllEtiquetas(List<Etiqueta> list) {
        list.clear();
        Cursor cursor = db.rawQuery("SELECT etiqueta_id, titulo, fecha_creacion FROM etiquetas", null);

        if (cursor.moveToFirst()) {
            do {
                Etiqueta etiqueta = new Etiqueta(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
                List<Nota> listNotasEtiqueta = new ArrayList<>();
                getAllNotasFrom(etiqueta.getId(), listNotasEtiqueta);
                etiqueta.setNotas(listNotasEtiqueta);
                list.add(etiqueta);
            } while (cursor.moveToNext());
        }
    }

    @Override
    public void deleteEtiqueta(int id) {
        db.execSQL("DELETE FROM etiquetaNotas WHERE etiqueta_id = '" + id + "'");
        db.execSQL("DELETE FROM etiquetas WHERE etiqueta_id = '" + id + "'"); // Eliminar etiqueta por id
    }

    @Override
    public void editEtiqueta(int id, String titulo) {
        titulo = titulo.replace("'", "''");
        db.execSQL("UPDATE letiquetas SET titulo = '" + titulo + "',"  +  "fecha_creacion = '" + dtf.format(Calendar.getInstance().getTime()) +
                "' WHERE etiqueta_id = '" + id + "'"); // Actualizar etiqueta
    }

    @Override
    public void getAllNotasFrom(int idEtiqueta, List<Nota> list) {
        list.clear();
        Cursor cursor = db.rawQuery("SELECT DISTINCT notas.nota_id, notas.titulo, notas.texto, notas.fecha_creacion FROM " +
                "etiquetaNotas NATURAL JOIN notas WHERE etiqueta_id = ? ", new String[]{Integer.toString(idEtiqueta)});

        if (cursor.moveToFirst()) {
            do {
                Nota nota = new Nota(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                Cursor cursor2 = db.rawQuery("SELECT DISTINCT libretas.libreta_id, libretas.titulo, libretas.fecha_creacion FROM " +
                        "libretas NATURAL JOIN libretaNotas WHERE nota_id = ? ", new String[]{Integer.toString(nota.getId())});
                cursor2.moveToFirst();
                nota.setLibreta(new Libreta(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
                list.add(nota);
            } while (cursor.moveToNext());
        }
    }

    @Override
    public Boolean existTitulo(String titulo) {
        titulo = titulo.replace("'", "''");
        Cursor c = db.rawQuery("select count(*) from etiquetas where titulo = ?", new String[]{titulo});
        c.moveToFirst();
        if (c.getInt(0) > 0) {
            return true;
        }
        return false;
    }
}
