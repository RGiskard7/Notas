package com.inkpot.app.data.room;

import com.inkpot.app.data.Etiqueta;
import com.inkpot.app.data.Libreta;
import com.inkpot.app.data.Nota;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convierte las entidades de Room al modelo de dominio que usa la aplicación y
 * agrupa los recuentos de notas en mapas para rellenarlos sin consultas extra.
 */
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
        nota.setRecordatorio(entity.recordatorio);
        nota.setFijada(entity.fijada == 1);
        nota.setColor(entity.color);
        return nota;
    }

    static Nota toNota(NotaConRelaciones relacion) {
        Libreta libreta = (relacion.libretas == null || relacion.libretas.isEmpty())
                ? null
                : toLibreta(relacion.libretas.get(0));
        List<Etiqueta> etiquetas = relacion.etiquetas == null
                ? new ArrayList<Etiqueta>()
                : toEtiquetas(relacion.etiquetas);
        return toNota(relacion.nota, libreta, etiquetas);
    }

    static Map<Integer, Integer> aMapa(List<Conteo> conteos) {
        Map<Integer, Integer> mapa = new HashMap<>();
        for (Conteo conteo : conteos) {
            mapa.put(conteo.id, conteo.total);
        }
        return mapa;
    }

    static Nota toNota(NotaConRelaciones relacion, Map<Integer, Integer> conteoLibretas, Map<Integer, Integer> conteoEtiquetas) {
        Nota nota = toNota(relacion);
        if (nota.getLibreta() != null) {
            Integer total = conteoLibretas.get(nota.getLibreta().getId());
            nota.getLibreta().setNumNotas(total == null ? 0 : total);
        }
        for (Etiqueta etiqueta : nota.getEtiquetas()) {
            Integer total = conteoEtiquetas.get(etiqueta.getId());
            etiqueta.setNumNotas(total == null ? 0 : total);
        }
        return nota;
    }

    static Libreta toLibreta(LibretaConRelaciones relacion) {
        Libreta libreta = new Libreta(
                relacion.libreta.id,
                relacion.libreta.titulo,
                contarActivas(relacion.notas),
                relacion.libreta.fechaCreacion);
        libreta.setFechaModificacion(relacion.libreta.fechaModificacion);
        return libreta;
    }

    static Etiqueta toEtiqueta(EtiquetaConRelaciones relacion) {
        Etiqueta etiqueta = new Etiqueta(
                relacion.etiqueta.id,
                relacion.etiqueta.titulo,
                contarActivas(relacion.notas),
                relacion.etiqueta.fechaCreacion);
        etiqueta.setFechaModificacion(relacion.etiqueta.fechaModificacion);
        return etiqueta;
    }

    /** Cuenta las notas que no están en la papelera. */
    private static int contarActivas(List<NotaEntity> notas) {
        if (notas == null) {
            return 0;
        }
        int activas = 0;
        for (NotaEntity nota : notas) {
            if (nota.eliminadaEn == 0) {
                activas++;
            }
        }
        return activas;
    }
}
