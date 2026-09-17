package com.example.notas.data.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

/**
 * Consultas Room sobre las libretas y sus vínculos con notas.
 */
@Dao
public interface LibretaDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertLibreta(LibretaEntity libreta);

    @Update
    void updateLibreta(LibretaEntity libreta);

    @Query("SELECT * FROM libretas WHERE libreta_id = :id LIMIT 1")
    LibretaEntity getLibretaById(int id);

    @Transaction
    @Query("SELECT * FROM libretas WHERE libreta_id = :id LIMIT 1")
    LibretaConRelaciones getLibretaConRelaciones(int id);

    @Query("SELECT * FROM libretas ORDER BY libreta_id ASC")
    List<LibretaEntity> getAllLibretas();

    @Transaction
    @Query("SELECT * FROM libretas ORDER BY libreta_id ASC")
    List<LibretaConRelaciones> getAllLibretasConRelaciones();

    @Query("SELECT COUNT(*) FROM libretas WHERE titulo = :titulo")
    int countByTitulo(String titulo);

    @Query("DELETE FROM libretas WHERE libreta_id = :id")
    void deleteLibretaById(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertLibretaNota(LibretaNotaCrossRef ref);

    @Query("SELECT notas.* FROM notas INNER JOIN libretaNotas ON notas.nota_id = libretaNotas.nota_id WHERE libretaNotas.libreta_id = :idLibreta ORDER BY notas.nota_id ASC")
    List<NotaEntity> getNotasDeLibreta(int idLibreta);
}
