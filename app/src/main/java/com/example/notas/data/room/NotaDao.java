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
 *
 * <p>Las consultas de listados y búsqueda excluyen las notas de la papelera
 * ({@code eliminada_en > 0}).</p>
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

    @Query("SELECT * FROM notas WHERE eliminada_en = 0 ORDER BY nota_id ASC")
    List<NotaEntity> getAllNotas();

    @Transaction
    @Query("SELECT * FROM notas WHERE eliminada_en = 0 ORDER BY nota_id ASC")
    List<NotaConRelaciones> getAllNotasConRelaciones();

    @Transaction
    @Query("SELECT notas.* FROM notas INNER JOIN libretaNotas ON notas.nota_id = libretaNotas.nota_id " +
            "WHERE libretaNotas.libreta_id = :idLibreta AND notas.eliminada_en = 0 ORDER BY notas.nota_id ASC")
    List<NotaConRelaciones> getNotasDeLibretaConRelaciones(int idLibreta);

    @Transaction
    @Query("SELECT notas.* FROM notas INNER JOIN etiquetaNotas ON notas.nota_id = etiquetaNotas.nota_id " +
            "WHERE etiquetaNotas.etiqueta_id = :idEtiqueta AND notas.eliminada_en = 0 ORDER BY notas.nota_id ASC")
    List<NotaConRelaciones> getNotasDeEtiquetaConRelaciones(int idEtiqueta);

    @Transaction
    @Query("SELECT * FROM notas WHERE eliminada_en > 0 ORDER BY eliminada_en DESC")
    List<NotaConRelaciones> getNotasEliminadasConRelaciones();

    @Transaction
    @Query("SELECT notas.* FROM notas JOIN notas_fts ON notas.nota_id = notas_fts.rowid " +
            "WHERE notas_fts MATCH :consulta AND notas.eliminada_en = 0 ORDER BY notas.nota_id ASC")
    List<NotaConRelaciones> buscarNotas(String consulta);

    @Transaction
    @Query("SELECT notas.* FROM notas JOIN notas_fts ON notas.nota_id = notas_fts.rowid " +
            "INNER JOIN libretaNotas ON notas.nota_id = libretaNotas.nota_id " +
            "WHERE libretaNotas.libreta_id = :idLibreta AND notas_fts MATCH :consulta AND notas.eliminada_en = 0 " +
            "ORDER BY notas.nota_id ASC")
    List<NotaConRelaciones> buscarNotasDeLibreta(int idLibreta, String consulta);

    @Transaction
    @Query("SELECT notas.* FROM notas JOIN notas_fts ON notas.nota_id = notas_fts.rowid " +
            "INNER JOIN etiquetaNotas ON notas.nota_id = etiquetaNotas.nota_id " +
            "WHERE etiquetaNotas.etiqueta_id = :idEtiqueta AND notas_fts MATCH :consulta AND notas.eliminada_en = 0 " +
            "ORDER BY notas.nota_id ASC")
    List<NotaConRelaciones> buscarNotasDeEtiqueta(int idEtiqueta, String consulta);

    @Query("UPDATE notas SET eliminada_en = :fecha WHERE nota_id = :id")
    void marcarEliminada(int id, long fecha);

    @Query("UPDATE notas SET eliminada_en = 0 WHERE nota_id = :id")
    void restaurarNota(int id);

    @Query("UPDATE notas SET recordatorio = :cuando WHERE nota_id = :id")
    void setRecordatorio(int id, long cuando);

    @Transaction
    @Query("SELECT * FROM notas WHERE recordatorio > 0 AND eliminada_en = 0 ORDER BY recordatorio ASC")
    List<NotaConRelaciones> getNotasConRecordatorio();

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

    @Query("SELECT libretaNotas.libreta_id AS id, COUNT(*) AS total FROM libretaNotas " +
            "INNER JOIN notas ON libretaNotas.nota_id = notas.nota_id WHERE notas.eliminada_en = 0 " +
            "GROUP BY libretaNotas.libreta_id")
    List<Conteo> conteosDeLibretas();

    @Query("SELECT etiquetaNotas.etiqueta_id AS id, COUNT(*) AS total FROM etiquetaNotas " +
            "INNER JOIN notas ON etiquetaNotas.nota_id = notas.nota_id WHERE notas.eliminada_en = 0 " +
            "GROUP BY etiquetaNotas.etiqueta_id")
    List<Conteo> conteosDeEtiquetas();
}
