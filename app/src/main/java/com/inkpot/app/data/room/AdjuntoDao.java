package com.inkpot.app.data.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Consultas Room sobre los adjuntos de las notas.
 */
@Dao
public interface AdjuntoDao {
    @Insert
    long insertar(AdjuntoEntity adjunto);

    @Query("SELECT * FROM adjuntos WHERE nota_id = :idNota ORDER BY adjunto_id ASC")
    List<AdjuntoEntity> getDeNota(int idNota);

    @Query("DELETE FROM adjuntos WHERE adjunto_id = :id")
    void eliminar(int id);
}
