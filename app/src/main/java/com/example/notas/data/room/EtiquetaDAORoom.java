package com.example.notas.data.room;

import android.content.Context;

import com.example.notas.data.Etiqueta;
import com.example.notas.data.IEtiquetaDAO;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementación de {@link IEtiquetaDAO} sobre Room.
 */
public class EtiquetaDAORoom implements IEtiquetaDAO {
    private final Context context;
    private final String name;

    public EtiquetaDAORoom(Context context, String name) {
        this.context = context.getApplicationContext();
        this.name = name;
    }

    private EtiquetaDao dao() {
        return NotasDatabase.get(context, name).etiquetaDao();
    }

    private NotaDao notaDao() {
        return NotasDatabase.get(context, name).notaDao();
    }

    private long ahora() {
        return System.currentTimeMillis();
    }

    @Override
    public void createEtiqueta(String titulo) {
        long ahora = ahora();
        dao().insertEtiqueta(new EtiquetaEntity(0, titulo, ahora, ahora));
    }

    @Override
    public void closeDB() {
        NotasDatabase.close(name);
    }

    @Override
    public Etiqueta getEtiqueta(int id) {
        EtiquetaConRelaciones relacion = dao().getEtiquetaConRelaciones(id);
        return relacion == null ? null : Mapper.toEtiqueta(relacion);
    }

    @Override
    public void getAllEtiquetas(List<Etiqueta> list) {
        list.clear();
        for (EtiquetaConRelaciones relacion : dao().getAllEtiquetasConRelaciones()) {
            list.add(Mapper.toEtiqueta(relacion));
        }
    }

    @Override
    public void deleteEtiqueta(int id) {
        dao().deleteEtiquetaById(id);
    }

    @Override
    public void editEtiqueta(int id, String titulo) {
        EtiquetaEntity entity = dao().getEtiquetaById(id);
        if (entity == null) {
            return;
        }
        entity.titulo = titulo;
        entity.fechaModificacion = ahora();
        dao().updateEtiqueta(entity);
    }

    @Override
    public void getAllNotasFrom(int idEtiqueta, List<Nota> list) {
        list.clear();
        Map<Integer, Integer> conteoLibretas = Mapper.aMapa(notaDao().conteosDeLibretas());
        Map<Integer, Integer> conteoEtiquetas = Mapper.aMapa(notaDao().conteosDeEtiquetas());
        for (NotaConRelaciones relacion : notaDao().getNotasDeEtiquetaConRelaciones(idEtiqueta)) {
            list.add(Mapper.toNota(relacion, conteoLibretas, conteoEtiquetas));
        }
    }

    @Override
    public Boolean existTitulo(String titulo) {
        return dao().countByTitulo(titulo) > 0;
    }
}
