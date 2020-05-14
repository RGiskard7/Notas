package com.example.notas.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.notas.data.Libreta;
import com.example.notas.R;

import java.util.List;

public class AdaptadorListLibretas extends BaseAdapter {
    private Context context;
    private List<Libreta> listaLibretas;
    private Boolean isSpinner;

    public AdaptadorListLibretas(Context context, List<Libreta> listaLibretas) {
        this.context = context;
        this.listaLibretas = listaLibretas;
        isSpinner = false;
    }

    public void setIsSpinner(Boolean opcion) {
        isSpinner = opcion;
    }

    public Boolean isSpinner() {
        return isSpinner;
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

        if (!isSpinner) {
            if (item == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                item = layoutInflater.inflate(R.layout.libreta_item, null);
            }

            TextView titulo = (TextView)item.findViewById(R.id.textViewTitulo2);
            titulo.setText(listaLibretas.get(position).getTitulo());

            TextView notas = (TextView)item.findViewById(R.id.textViewNotas);
            notas.setText(listaLibretas.get(position).getNotas().size() + " Notas");
        } else {
            if (item == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                item = layoutInflater.inflate(R.layout.libreta_item_spinner, null);

                TextView titulo = (TextView)item.findViewById(R.id.tituloLibretaSpinner);
                titulo.setText(listaLibretas.get(position).getTitulo());
            }

        }

        return item;
    }
}
