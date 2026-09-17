package com.example.notas.data.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

/**
 * Consultas Room sobre las notas.
 *
 * <p>Incluye los métodos con relaciones ({@code @Transaction}) que traen la
 * libreta y las etiquetas de cada nota en un número constante de consultas, y
 * las consultas agregadas de recuento.</p>
 */
@Dao
public interface NotaDao {
    @Insert
    long insertNota(NotaEntity nota);

    @Update
    void updateNota(NotaEntity nota);

    @Query("SELECT * FROM notas WHERE nota_id = :id LIMIT 1")
    NotaEntity getNotaById(int id);

    @Transaction
    @Query("SELECT * FROM notas WHERE nota_id = :id LIMIT 1")
    NotaConRelaciones getNotaConRelaciones(int id);

    @Query("SELECT * FROM notas ORDER BY nota_id ASC")
    List<NotaEntity> getAllNotas();

    @Transaction
    @Query("SELECT * FROM notas ORDER BY nota_id ASC")
    List<NotaConRelaciones> getAllNotasConRelaciones();

    @Transaction
    @Query("SELECT notas.* FROM notas INNER JOIN libretaNotas ON notas.nota_id = libretaNotas.nota_id WHERE libretaNotas.libreta_id = :idLibreta ORDER BY notas.nota_id ASC")
    List<NotaConRelaciones> getNotasDeLibretaConRelaciones(int idLibreta);

    @Transaction
    @Query("SELECT notas.* FROM notas INNER JOIN etiquetaNotas ON notas.nota_id = etiquetaNotas.nota_id WHERE etiquetaNotas.etiqueta_id = :idEtiqueta ORDER BY notas.nota_id ASC")
    List<NotaConRelaciones> getNotasDeEtiquetaConRelaciones(int idEtiqueta);

    @Transaction
    @Query("SELECT notas.* FROM notas JOIN notas_fts ON notas.nota_id = notas_fts.rowid WHERE notas_fts MATCH :consulta ORDER BY notas.nota_id ASC")
    List<NotaConRelaciones> buscarNotas(String consulta);

    @Transaction
    @Query("SELECT notas.* FROM notas JOIN notas_fts ON notas.nota_id = notas_fts.rowid " +
            "INNER JOIN libretaNotas ON notas.nota_id = libretaNotas.nota_id " +
            "WHERE libretaNotas.libreta_id = :idLibreta AND notas_fts MATCH :consulta ORDER BY notas.nota_id ASC")
    List<NotaConRelaciones> buscarNotasDeLibreta(int idLibreta, String consulta);

    @Transaction
    @Query("SELECT notas.* FROM notas JOIN notas_fts ON notas.nota_id = notas_fts.rowid " +
            "INNER JOIN etiquetaNotas ON notas.nota_id = etiquetaNotas.nota_id " +
            "WHERE etiquetaNotas.etiqueta_id = :idEtiqueta AND notas_fts MATCH :consulta ORDER BY notas.nota_id ASC")
    List<NotaConRelaciones> buscarNotasDeEtiqueta(int idEtiqueta, String consulta);

    @Query("DELETE FROM notas WHERE nota_id = :id")
    void deleteNotaById(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertLibretaNota(LibretaNotaCrossRef ref);

    @Query("DELETE FROM libretaNotas WHERE nota_id = :idNota AND libreta_id = :idLibreta")
    void deleteLibretaNota(int idNota, int idLibreta);

    @Query("SELECT libretas.* FROM libretas INNER JOIN libretaNotas ON libretas.libreta_id = libretaNotas.libreta_id WHERE libretaNotas.nota_id = :idNota LIMIT 1")
    LibretaEntity getLibretaDeNota(int idNota);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertEtiquetaNota(EtiquetaNotaCrossRef ref);

    @Query("DELETE FROM etiquetaNotas WHERE nota_id = :idNota AND etiqueta_id = :idEtiqueta")
    void deleteEtiquetaNota(int idNota, int idEtiqueta);

    @Query("SELECT etiquetas.* FROM etiquetas INNER JOIN etiquetaNotas ON etiquetas.etiqueta_id = etiquetaNotas.etiqueta_id WHERE etiquetaNotas.nota_id = :idNota ORDER BY etiquetas.etiqueta_id ASC")
    List<EtiquetaEntity> getEtiquetasDeNota(int idNota);

    @Query("SELECT libreta_id AS id, COUNT(*) AS total FROM libretaNotas GROUP BY libreta_id")
    List<Conteo> conteosDeLibretas();

    @Query("SELECT etiqueta_id AS id, COUNT(*) AS total FROM etiquetaNotas GROUP BY etiqueta_id")
    List<Conteo> conteosDeEtiquetas();
}
