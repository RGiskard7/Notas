package com.inkpot.app.data;

import android.content.Context;

import com.inkpot.app.data.room.EtiquetaDAORoom;
import com.inkpot.app.data.room.LibretaDAORoom;
import com.inkpot.app.data.room.NotaDAORoom;

/**
 * Factoría concreta que construye los DAOs sobre Room.
 *
 * <p>Todos los DAOs comparten el mismo fichero de base de datos.</p>
 */
public class FactoryDAORoom extends FactoryDAO {

    /** Nombre del fichero de base de datos de la aplicación. */
    private static final String DB_NAME = "inkpot";

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
