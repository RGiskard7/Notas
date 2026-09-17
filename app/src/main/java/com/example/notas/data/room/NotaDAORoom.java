package com.example.notas.data.room;

import android.content.Context;

import com.example.notas.data.Etiqueta;
import com.example.notas.data.INotaDAO;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;

import java.util.List;
import java.util.Map;

/**
 * Implementación de {@link INotaDAO} sobre Room.
 *
 * <p>Traduce entre el modelo de dominio y las entidades, y aprovecha las
 * relaciones para no lanzar una consulta por cada nota.</p>
 */
public class NotaDAORoom implements INotaDAO {
    private final Context context;
    private final String name;

    public NotaDAORoom(Context context, String name) {
        this.context = context.getApplicationContext();
        this.name = name;
    }

    private NotaDao dao() {
        return NotasDatabase.get(context, name).notaDao();
    }

    private long ahora() {
        return System.currentTimeMillis();
    }

    private Map<Integer, Integer> conteosLibretas() {
        return Mapper.aMapa(dao().conteosDeLibretas());
    }

    private Map<Integer, Integer> conteosEtiquetas() {
        return Mapper.aMapa(dao().conteosDeEtiquetas());
    }

    @Override
    public int createNota(String titulo, String texto) {
        long ahora = ahora();
        NotaEntity entity = new NotaEntity(0, titulo, texto, ahora, ahora, 0);
        return (int) dao().insertNota(entity);
    }

    @Override
    public void closeDB() {
        NotasDatabase.close(name);
    }

    @Override
    public Nota getNota(int id) {
        NotaConRelaciones relacion = dao().getNotaConRelaciones(id);
        return relacion == null ? null : Mapper.toNota(relacion, conteosLibretas(), conteosEtiquetas());
    }

    @Override
    public void editNota(int id, String titulo, String texto) {
        NotaEntity entity = dao().getNotaById(id);
        if (entity == null) {
            return;
        }
        entity.titulo = titulo;
        entity.texto = texto;
        entity.fechaModificacion = ahora();
        dao().updateNota(entity);
    }

    @Override
    public Libreta getLibreta(int idNota) {
        LibretaEntity entity = dao().getLibretaDeNota(idNota);
        if (entity == null) {
            return null;
        }
        Libreta libreta = Mapper.toLibreta(entity);
        Integer total = conteosLibretas().get(libreta.getId());
        libreta.setNumNotas(total == null ? 0 : total);
        return libreta;
    }

    @Override
    public void deleteLibreta(int idNota, int idLibreta) {
        dao().deleteLibretaNota(idNota, idLibreta);
    }

    @Override
    public void deleteNota(int id) {
        dao().marcarEliminada(id, ahora());
    }

    @Override
    public void restaurarNota(int id) {
        dao().restaurarNota(id);
    }

    @Override
    public void borrarNotaDefinitivamente(int id) {
        dao().deleteNotaById(id);
    }

    @Override
    public void getNotasEliminadas(List<Nota> list) {
        list.clear();
        Map<Integer, Integer> conteoLibretas = conteosLibretas();
        Map<Integer, Integer> conteoEtiquetas = conteosEtiquetas();
        for (NotaConRelaciones relacion : dao().getNotasEliminadasConRelaciones()) {
            list.add(Mapper.toNota(relacion, conteoLibretas, conteoEtiquetas));
        }
    }

    @Override
    public void getAllNotas(List<Nota> list) {
        list.clear();
        Map<Integer, Integer> conteoLibretas = conteosLibretas();
        Map<Integer, Integer> conteoEtiquetas = conteosEtiquetas();
        for (NotaConRelaciones relacion : dao().getAllNotasConRelaciones()) {
            list.add(Mapper.toNota(relacion, conteoLibretas, conteoEtiquetas));
        }
    }

    @Override
    public void addEtiquetasToNota(int idNota, List<Etiqueta> etiquetas) {
        for (Etiqueta etiqueta : etiquetas) {
            dao().insertEtiquetaNota(new EtiquetaNotaCrossRef(etiqueta.getId(), idNota));
        }
    }

    @Override
    public void deletedEtiquetasFromNota(int idNota, List<Etiqueta> etiquetas) {
        for (Etiqueta etiqueta : etiquetas) {
            dao().deleteEtiquetaNota(idNota, etiqueta.getId());
        }
    }

    @Override
    public void getAllEtiquetasFrom(int idNota, List<Etiqueta> list) {
        list.clear();
        Map<Integer, Integer> conteoEtiquetas = conteosEtiquetas();
        for (Etiqueta etiqueta : Mapper.toEtiquetas(dao().getEtiquetasDeNota(idNota))) {
            Integer total = conteoEtiquetas.get(etiqueta.getId());
            etiqueta.setNumNotas(total == null ? 0 : total);
            list.add(etiqueta);
        }
    }

    @Override
    public void buscarNotas(String consulta, int idLibreta, int idEtiqueta, List<Nota> list) {
        list.clear();
        List<NotaConRelaciones> relaciones;
        if (idLibreta != -1) {
            relaciones = dao().buscarNotasDeLibreta(idLibreta, consulta);
        } else if (idEtiqueta != -1) {
            relaciones = dao().buscarNotasDeEtiqueta(idEtiqueta, consulta);
        } else {
            relaciones = dao().buscarNotas(consulta);
        }

        Map<Integer, Integer> conteoLibretas = conteosLibretas();
        Map<Integer, Integer> conteoEtiquetas = conteosEtiquetas();
        for (NotaConRelaciones relacion : relaciones) {
            list.add(Mapper.toNota(relacion, conteoLibretas, conteoEtiquetas));
        }
    }
}
