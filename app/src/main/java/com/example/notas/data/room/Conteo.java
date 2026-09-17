package com.example.notas.data.room;

import androidx.room.ColumnInfo;

public class Conteo {
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "total")
    public int total;
}
