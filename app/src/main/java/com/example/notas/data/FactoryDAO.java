package com.example.notas.data;

import android.content.Context;

public abstract class FactoryDAO {
    public static final int ROOM_FACTORY = 1;

    public abstract INotaDAO getNotaDao(Context context);

    public abstract  ILibretaDAO getLibretaDao(Context context);

    public abstract IEtiquetaDAO getEtiquetaDao(Context context);

    public static FactoryDAO getFactory(int keyFactory) {
        switch(keyFactory) {
            case ROOM_FACTORY:
                return new FactoryDAORoom();
            default:
                throw new IllegalArgumentException();
        }
    }
}
