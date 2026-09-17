package com.example.notas.data.room;

import android.content.Context;

import com.example.notas.data.Etiqueta;
import com.example.notas.data.IEtiquetaDAO;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EtiquetaDAORoom implements IEtiquetaDAO {
    private final Context context;
    private final String name;
    private final SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

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

    private String ahora() {
        return dtf.format(Calendar.getInstance().getTime());
    }

    private Nota toNota(NotaEntity entity) {
        Libreta libreta = Mapper.toLibreta(notaDao().getLibretaDeNota(entity.id));
        List<Etiqueta> etiquetas = Mapper.toEtiquetas(notaDao().getEtiquetasDeNota(entity.id));
        return Mapper.toNota(entity, libreta, etiquetas);
    }

    @Override
    public void createEtiqueta(String titulo) {
        String ahora = ahora();
        dao().insertEtiqueta(new EtiquetaEntity(0, titulo, ahora, ahora));
    }

    @Override
    public void closeDB() {
        NotasDatabase.close(name);
    }

    @Override
    public Etiqueta getEtiqueta(int id) {
        return Mapper.toEtiqueta(dao().getEtiquetaById(id));
    }

    @Override
    public void getAllEtiquetas(List<Etiqueta> list) {
        list.clear();
        for (EtiquetaEntity entity : dao().getAllEtiquetas()) {
            Etiqueta etiqueta = Mapper.toEtiqueta(entity);
            List<Nota> notas = new ArrayList<>();
            for (NotaEntity notaEntity : dao().getNotasDeEtiqueta(entity.id)) {
                notas.add(toNota(notaEntity));
            }
            etiqueta.setNotas(notas);
            list.add(etiqueta);
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
        for (NotaEntity entity : dao().getNotasDeEtiqueta(idEtiqueta)) {
            list.add(toNota(entity));
        }
    }

    @Override
    public Boolean existTitulo(String titulo) {
        return dao().countByTitulo(titulo) > 0;
    }
}
