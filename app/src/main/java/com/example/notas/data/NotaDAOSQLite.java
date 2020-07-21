package com.example.notas.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.CheckBox;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class NotaDAOSQLite implements INotaDAO {
    private SQLiteDatabase db;
    private final SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy - HH:mm");

    public NotaDAOSQLite(Context context, String name, SQLiteDatabase.CursorFactory factory, int versionDB) {
        DB dbNotas = new DB(context, name, factory, versionDB); // Conectar a base de datos
        db = dbNotas.getWritableDatabase(); // Poner base de datos en modo escritura
    }

    @Override
    public int createNota(String titulo, String texto) {
        titulo = titulo.replace("'", "''");
        texto = texto.replace("'", "''");
        db.execSQL("INSERT INTO notas (titulo, texto, fecha_creacion) VALUES ('" + titulo + "','" +
                texto + "','" + dtf.format(Calendar.getInstance().getTime()) + "')");

        Cursor c = db.rawQuery("SELECT last_insert_rowid();", null);
        c.moveToFirst();
        return c.getInt(0);
    }

    @Override
    public Boolean existTitulo(String titulo) {
        titulo = titulo.replace("'", "''");
        Cursor c = db.rawQuery("select count(*) from notas where titulo = ?", new String[]{titulo});
        c.moveToFirst();
        if (c.getInt(0) > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Nota getNota(int id) {
        Cursor cursor = db.rawQuery("SELECT * FROM " +
                "notas WHERE nota_id = ? ", new String[]{Integer.toString(id)});
        cursor.moveToFirst();
        return new Nota(cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                getLibreta(id), cursor.getString(3));
    }

    @Override
    public Libreta getLibreta(int idNota) {
        Cursor cursor = db.rawQuery("SELECT DISTINCT libretas.libreta_id, libretas.titulo, libretas.fecha_creacion FROM " +
                "libretas NATURAL JOIN libretaNotas WHERE nota_id = ? ", new String[]{Integer.toString(idNota)});
        cursor.moveToFirst();
        return new Libreta(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
    }

    @Override
    public void deleteLibreta(int idNota, int idLibreta) {
        db.execSQL("DELETE FROM libretaNotas WHERE nota_id = '" + idNota + "' and libreta_id = '" +  idLibreta + "'"); // Eliminar nota por id
    }

    @Override
    public void editNota(int id, String titulo, String texto) {
        titulo = titulo.replace("'", "''");
        texto = texto.replace("'", "''");
        db.execSQL("UPDATE notas SET titulo = '" + titulo + "'," + "texto = '" + texto + "'," +
                "fecha_creacion = '" + dtf.format(Calendar.getInstance().getTime()) + "' WHERE nota_id = '" + id + "'"); // Actualizar nota

    }

    @Override
    public void deleteNota(int id) {
        db.execSQL("DELETE FROM notas WHERE nota_id = '" + id + "'"); // Eliminar nota por id
    }

    @Override
    public void addCheckBoxToNota(int idNota, CheckBox checkBox) {
        int control = (checkBox.isSelected()) ? 1:0;

        db.execSQL("INSERT INTO checkBoxNotas (nota_id, texto, control) VALUES ('" + checkBox.getText() + "','" +
                idNota + "','" + control + "')");
    }

    @Override
    public void getAllCheckBoxsFrom(Context context, int idNota, List<CheckBox> list) {
        list.clear();
        Cursor cursor = db.rawQuery("SELECT DISTINCT checkBoxNotas.texto, checkBoxNotas.control FROM " +
                "checkBoxNotas NATURAL JOIN notas WHERE nota_id = ? ", new String[]{Integer.toString(idNota)});

        if (cursor.moveToFirst()) {
            do {
                CheckBox ch = new CheckBox(context);
                ch.setText(cursor.getString(0));
                if (cursor.getInt(1) == 1) {
                    ch.setSelected(true);
                } else {
                    ch.setSelected(false);
                }
                list.add(ch);
            } while (cursor.moveToNext());
        }
    }

    @Override
    public void getAllNotas(List<Nota> list) {
        list.clear();
        Cursor cursor = db.rawQuery("SELECT nota_id, titulo, texto, fecha_creacion FROM notas", null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new Nota(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)));
            } while (cursor.moveToNext());
        }

        for(Nota nota : list) {
            nota.setLibreta(getLibreta(nota.getId()));
        }
    }
}
