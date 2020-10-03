package com.example.notas.data;

import java.util.List;

public interface IEtiquetaDAO {
    public void createEtiqueta(String titulo);
    public Etiqueta getEtiqueta(int id);
    public void getAllEtiquetas(List<Etiqueta> list);
    public void deleteEtiqueta(int id);
    public void editEtiqueta(int id, String titulo);
    public void getAllNotasFrom(int idEtiqueta, List<Nota> list);
    public Boolean existTitulo(String titulo);
}
