package com.example.notas.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DB extends SQLiteOpenHelper {
    private SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy");

    private static final String crearTablaNotas = "CREATE TABLE notas(nota_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "titulo TEXT NOT NULL, texto TEXT, libreta INTEGER, fecha_creacion TEXT)";

    private static final String crearTablaLibretas = "CREATE TABLE libretas(libreta_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "titulo TEXT NOT NULL UNIQUE, fecha_creacion TEXT)";

    private static final String libretaNotas = "CREATE TABLE libretaNotas(id INTEGER PRIMARY KEY AUTOINCREMENT, libreta_id INTEGER, " +
            "nota_id INTEGER, FOREIGN  KEY (libreta_id) REFERENCES libretas(libreta_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE, " +
            "FOREIGN  KEY (nota_id) REFERENCES notas(nota_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE)";

    private static final String checkBoxNotas = "CREATE TABLE checkBoxNotas(id INTEGER PRIMARY KEY AUTOINCREMENT, texto TEXT NOT NULL, " +
            "nota_id INTEGER NOT NULL, control INTEGER, CHECK (control in (0,1)), FOREIGN  KEY (nota_id) REFERENCES notas(nota_id) MATCH SIMPLE " +
            "ON UPDATE CASCADE ON DELETE CASCADE)";

    public DB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(crearTablaNotas);
        db.execSQL(crearTablaLibretas);
        db.execSQL(libretaNotas);
        db.execSQL(checkBoxNotas);

        db.execSQL("INSERT INTO libretas (titulo, fecha_creacion) VALUES ('Default','" +
                dtf.format(Calendar.getInstance().getTime()) + "')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(crearTablaNotas);
        db.execSQL(crearTablaLibretas);
        db.execSQL(libretaNotas);
        db.execSQL(checkBoxNotas);
    }
}