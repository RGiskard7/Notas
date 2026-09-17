package com.example.notas.data.room;

import android.content.Context;

import com.example.notas.data.Etiqueta;
import com.example.notas.data.ILibretaDAO;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LibretaDAORoom implements ILibretaDAO {
    private final Context context;
    private final String name;
    private final SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public LibretaDAORoom(Context context, String name) {
        this.context = context.getApplicationContext();
        this.name = name;
    }

    private LibretaDao dao() {
        return NotasDatabase.get(context, name).libretaDao();
    }

    private NotaDao notaDao() {
        return NotasDatabase.get(context, name).notaDao();
    }

    private String ahora() {
        return dtf.format(Calendar.getInstance().getTime());
    }

    private Nota toNota(NotaEntity entity, Libreta libreta) {
        List<Etiqueta> etiquetas = Mapper.toEtiquetas(notaDao().getEtiquetasDeNota(entity.id));
        return Mapper.toNota(entity, libreta, etiquetas);
    }

    @Override
    public void createLibreta(String titulo) {
        String ahora = ahora();
        dao().insertLibreta(new LibretaEntity(0, titulo, ahora, ahora));
    }

    @Override
    public void closeDB() {
        NotasDatabase.close(name);
    }

    @Override
    public Boolean existTitulo(String titulo) {
        return dao().countByTitulo(titulo) > 0;
    }

    @Override
    public Libreta getLibreta(int id) {
        return Mapper.toLibreta(dao().getLibretaById(id));
    }

    @Override
    public void deleteLibreta(int id) {
        dao().deleteLibretaById(id);
    }

    @Override
    public void addNotaToLibreta(int idLibreta, int idNota) {
        dao().insertLibretaNota(new LibretaNotaCrossRef(idLibreta, idNota));
    }

    @Override
    public void editLibreta(int id, String titulo) {
        LibretaEntity entity = dao().getLibretaById(id);
        if (entity == null) {
            return;
        }
        entity.titulo = titulo;
        entity.fechaModificacion = ahora();
        dao().updateLibreta(entity);
    }

    @Override
    public void getAllLibretas(List<Libreta> list) {
        list.clear();
        for (LibretaEntity entity : dao().getAllLibretas()) {
            Libreta libreta = Mapper.toLibreta(entity);
            List<Nota> notas = new ArrayList<>();
            for (NotaEntity notaEntity : dao().getNotasDeLibreta(entity.id)) {
                notas.add(toNota(notaEntity, libreta));
            }
            libreta.setNotas(notas);
            list.add(libreta);
        }
    }

    @Override
    public void getAllNotasFrom(int idLibreta, List<Nota> list) {
        list.clear();
        Libreta libreta = getLibreta(idLibreta);
        for (NotaEntity entity : dao().getNotasDeLibreta(idLibreta)) {
            list.add(toNota(entity, libreta));
        }
    }
}
