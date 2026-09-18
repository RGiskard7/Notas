package com.inkpot.app.data.room;

import androidx.room.ColumnInfo;

/**
 * Resultado auxiliar de las consultas de recuento: un identificador y su total.
 */
public class Conteo {
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "total")
    public int total;
}
