package com.example.notas.data;

import android.content.Context;

/**
 * Factoría abstracta de DAOs.
 *
 * <p>Permite que la aplicación pida el acceso a datos sin conocer la tecnología
 * concreta de persistencia. Hoy solo existe la implementación basada en Room,
 * pero el resto del código no depende de ella.</p>
 */
public abstract class FactoryDAO {

    /** Identificador de la factoría basada en Room. */
    public static final int ROOM_FACTORY = 1;

    /** Devuelve el DAO de notas. */
    public abstract INotaDAO getNotaDao(Context context);

    /** Devuelve el DAO de libretas. */
    public abstract ILibretaDAO getLibretaDao(Context context);

    /** Devuelve el DAO de etiquetas. */
    public abstract IEtiquetaDAO getEtiquetaDao(Context context);

    /**
     * Crea la factoría correspondiente al identificador indicado.
     *
     * @param keyFactory identificador de la factoría.
     * @return la factoría solicitada.
     * @throws IllegalArgumentException si el identificador no es válido.
     */
    public static FactoryDAO getFactory(int keyFactory) {
        switch (keyFactory) {
            case ROOM_FACTORY:
                return new FactoryDAORoom();
            default:
                throw new IllegalArgumentException();
        }
    }
}
