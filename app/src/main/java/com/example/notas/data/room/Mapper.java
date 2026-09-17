package com.example.notas.data.room;

import com.example.notas.data.Etiqueta;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;

import java.util.ArrayList;
import java.util.List;

final class Mapper {
    private Mapper() {
    }

    static Libreta toLibreta(LibretaEntity entity) {
        if (entity == null) {
            return null;
        }
        Libreta libreta = new Libreta(entity.id, entity.titulo, entity.fechaCreacion);
        libreta.setFechaModificacion(entity.fechaModificacion);
        return libreta;
    }

    static Etiqueta toEtiqueta(EtiquetaEntity entity) {
        if (entity == null) {
            return null;
        }
        Etiqueta etiqueta = new Etiqueta(entity.id, entity.titulo, entity.fechaCreacion);
        etiqueta.setFechaModificacion(entity.fechaModificacion);
        return etiqueta;
    }

    static List<Etiqueta> toEtiquetas(List<EtiquetaEntity> entities) {
        List<Etiqueta> etiquetas = new ArrayList<>();
        for (EtiquetaEntity entity : entities) {
            etiquetas.add(toEtiqueta(entity));
        }
        return etiquetas;
    }

    static Nota toNota(NotaEntity entity, Libreta libreta, List<Etiqueta> etiquetas) {
        Nota nota = new Nota(entity.id, entity.titulo, entity.texto, libreta, etiquetas, entity.fechaCreacion);
        nota.setFechaModificacion(entity.fechaModificacion);
        return nota;
    }
}
