package com.example.notas.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.notas.R;
import com.example.notas.data.Libreta;

import java.util.List;

public class AdaptadorListLibretas extends BaseAdapter {
    private final Context context;
    private final List<Libreta> listaLibretas;

    public AdaptadorListLibretas(Context context, List<Libreta> listaLibretas) {
        this.context = context;
        this.listaLibretas = listaLibretas;
    }

    @Override
    public int getCount() {
        return listaLibretas.size();
    }

    @Override
    public Object getItem(int position) {
        return listaLibretas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return listaLibretas.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        if (item == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            item = layoutInflater.inflate(R.layout.libreta_item_spinner, parent, false);
        }

        TextView titulo = item.findViewById(R.id.tituloLibretaSpinner);
        titulo.setText(listaLibretas.get(position).getTitulo());
        return item;
    }
}
