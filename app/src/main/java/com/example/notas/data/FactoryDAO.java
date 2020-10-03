package com.example.notas.data;

import android.content.Context;

public abstract class FactoryDAO {
    public static final int SQLITE_FACTORY = 1;

    public abstract INotaDAO getNotaDao(Context context);

    public abstract  ILibretaDAO getLibretaDao(Context context);

    public abstract IEtiquetaDAO getEtiquetaDao(Context context);

    public static FactoryDAO getFactory(int keyFactory) {
        switch(keyFactory) {
            case SQLITE_FACTORY:
                return new FactoryDAOSQLite();
            default:
                throw new IllegalArgumentException();
        }
    }
}
