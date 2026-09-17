package com.example.notas.data.room;

import android.content.Context;

import com.example.notas.data.Etiqueta;
import com.example.notas.data.INotaDAO;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotaDAORoom implements INotaDAO {
    private final Context context;
    private final String name;
    private final SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

    public NotaDAORoom(Context context, String name) {
        this.context = context.getApplicationContext();
        this.name = name;
    }

    private NotaDao dao() {
        return NotasDatabase.get(context, name).notaDao();
    }

    private String ahora() {
        return dtf.format(Calendar.getInstance().getTime());
    }

    private Map<Integer, Integer> conteosLibretas() {
        return Mapper.aMapa(dao().conteosDeLibretas());
    }

    private Map<Integer, Integer> conteosEtiquetas() {
        return Mapper.aMapa(dao().conteosDeEtiquetas());
    }

    @Override
    public int createNota(String titulo, String texto) {
        String ahora = ahora();
        NotaEntity entity = new NotaEntity(0, titulo, texto, ahora, ahora);
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
        dao().deleteNotaById(id);
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
}
