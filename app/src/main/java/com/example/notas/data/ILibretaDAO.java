package com.example.notas.data;

import java.util.List;

public interface ILibretaDAO {
    public void createLibreta(String titulo);
    public void closeDB();
    public Boolean existTitulo(String titulo);
    public Libreta getLibreta(int id);
    public void deleteLibreta(int id);
    public void addNotaToLibreta(int idLibreta, int idNota);
    public void editLibreta(int id, String titulo);
    public void getAllLibretas(List<Libreta> list);
    public void getAllNotasFrom(int idLibreta, List<Nota> list);
}
