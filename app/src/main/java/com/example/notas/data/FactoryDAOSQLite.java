package com.example.notas.data;

import android.content.Context;

public class FactoryDAOSQLite extends FactoryDAO {
    @Override
    public INotaDAO getNotaDao(Context context) {
        return new NotaDAOSQLite(context, "DBNevernote", null, 1);
    }

    @Override
    public ILibretaDAO getLibretaDao(Context context) {
        return new LibretaDAOSQLite(context, "DBNevernote", null, 1);
    }

    @Override
    public IEtiquetaDAO getEtiquetaDao(Context context) {
        return new EtiquetaDAOSQLite(context, "DBNevernote", null, 1);
    }
}
