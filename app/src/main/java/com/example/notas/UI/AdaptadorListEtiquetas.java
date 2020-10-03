package com.example.notas.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.notas.R;
import com.example.notas.data.Etiqueta;
import com.example.notas.data.Libreta;

import java.util.List;

public class AdaptadorListEtiquetas extends BaseAdapter {
    private Context context;
    private List<Etiqueta> listaEtiquetas;

    public AdaptadorListEtiquetas(Context context, List<Etiqueta> listaEtiquetas) {
        this.context = context;
        this.listaEtiquetas = listaEtiquetas;
    }

    @Override
    public int getCount() {
        return listaEtiquetas.size();
    }

    @Override
    public Object getItem(int position) {
        return listaEtiquetas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return listaEtiquetas.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;

        if (item == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            item = layoutInflater.inflate(R.layout.libreta_item, null);
        }

        TextView titulo = (TextView)item.findViewById(R.id.textViewTitulo2);
        titulo.setText(listaEtiquetas.get(position).getTitulo());

        TextView notas = (TextView)item.findViewById(R.id.textViewNotas);
        notas.setText(listaEtiquetas.get(position).getNotas().size() + " Notas");

        return item;
    }
}
