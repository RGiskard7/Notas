package com.example.notas.data.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EtiquetaDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertEtiqueta(EtiquetaEntity etiqueta);

    @Update
    void updateEtiqueta(EtiquetaEntity etiqueta);

    @Query("SELECT * FROM etiquetas WHERE etiqueta_id = :id LIMIT 1")
    EtiquetaEntity getEtiquetaById(int id);

    @Query("SELECT * FROM etiquetas ORDER BY etiqueta_id ASC")
    List<EtiquetaEntity> getAllEtiquetas();

    @Query("SELECT COUNT(*) FROM etiquetas WHERE titulo = :titulo")
    int countByTitulo(String titulo);

    @Query("DELETE FROM etiquetas WHERE etiqueta_id = :id")
    void deleteEtiquetaById(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertEtiquetaNota(EtiquetaNotaCrossRef ref);

    @Query("SELECT notas.* FROM notas INNER JOIN etiquetaNotas ON notas.nota_id = etiquetaNotas.nota_id WHERE etiquetaNotas.etiqueta_id = :idEtiqueta ORDER BY notas.nota_id ASC")
    List<NotaEntity> getNotasDeEtiqueta(int idEtiqueta);
}
