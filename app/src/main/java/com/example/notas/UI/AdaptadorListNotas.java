package com.example.notas.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.notas.data.Nota;
import com.example.notas.R;

import java.util.List;

public class AdaptadorListNotas extends BaseAdapter {
    private Context context;
    private List<Nota> listaNotas;

    public AdaptadorListNotas(Context context, List<Nota> listaNotas) {
        this.context = context;
        this.listaNotas = listaNotas;
    }

    @Override
    public int getCount() {
        return listaNotas.size();
    }

    @Override
    public Object getItem(int position) {
        return listaNotas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return listaNotas.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;

        if (item == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            item = layoutInflater.inflate(R.layout.nota_item, null);
        }

        TextView titulo = (TextView)item.findViewById(R.id.textViewTitulo);
        titulo.setText(listaNotas.get(position).getTitulo());

        TextView texto = (TextView)item.findViewById(R.id.textViewTexto);
        texto.setText(listaNotas.get(position).getTexto());
        if (listaNotas.get(position).getTexto().length() > 32) {
            texto.getEllipsize();
        }

        TextView fecha = (TextView)item.findViewById(R.id.textViewFecha);
        fecha.setText(listaNotas.get(position).getFechaCreacion());

        return item;
    }
}
