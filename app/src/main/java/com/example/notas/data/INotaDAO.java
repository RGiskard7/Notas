package com.example.notas.data;

import java.util.List;

public interface INotaDAO {
    public int createNota(String titulo, String texto);
    public void closeDB();
    public Nota getNota(int id);
    public void editNota(int id, String titulo, String texto);
    public Libreta getLibreta(int idNota);
    public void deleteLibreta(int idNota, int idLibreta);
    public void deleteNota(int id);
    public void getAllNotas(List<Nota> list);
    public void addEtiquetasToNota(int idNota, List<Etiqueta> etiquetas);
    public void deletedEtiquetasFromNota(int idNota, List<Etiqueta> etiquetas);
    public void getAllEtiquetasFrom(int idNota, List<Etiqueta> list);
}
