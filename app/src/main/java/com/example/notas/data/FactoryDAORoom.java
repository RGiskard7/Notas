package com.example.notas.data;

import android.content.Context;

import com.example.notas.data.room.EtiquetaDAORoom;
import com.example.notas.data.room.LibretaDAORoom;
import com.example.notas.data.room.NotaDAORoom;

/**
 * Factoría concreta que construye los DAOs sobre Room.
 *
 * <p>Todos los DAOs comparten el mismo fichero de base de datos.</p>
 */
public class FactoryDAORoom extends FactoryDAO {

    /** Nombre del fichero de base de datos de la aplicación. */
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
