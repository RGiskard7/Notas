package com.example.notas.data;

import java.util.List;

public interface INotaDAO {
    public int createNota(String titulo, String texto);
    public void editNota(int id, String titulo, String texto);
    public Libreta getLibreta(int idNota);
    public void deleteLibreta(int idNota, int idLibreta);
    public void deleteNota(int id);
    public void getAllNotas(List<Nota> list);
}
