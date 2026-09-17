package com.example.notas.data;

import android.content.Context;

import com.example.notas.data.room.EtiquetaDAORoom;
import com.example.notas.data.room.LibretaDAORoom;
import com.example.notas.data.room.NotaDAORoom;

public class FactoryDAORoom extends FactoryDAO {
    private static final String DB_NAME = "DBNevernote";

    @Override
    public INotaDAO getNotaDao(Context context) {
        return new NotaDAORoom(context, DB_NAME);
    }

    @Override
    public ILibretaDAO getLibretaDao(Context context) {
        return new LibretaDAORoom(context, DB_NAME);
    }

    @Override
    public IEtiquetaDAO getEtiquetaDao(Context context) {
        return new EtiquetaDAORoom(context, DB_NAME);
    }
}
