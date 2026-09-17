package com.example.notas.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.notas.data.Libreta;
import com.example.notas.databinding.LibretaItemSpinnerBinding;

import java.util.List;

/**
 * Adapter del desplegable de libretas que aparece al crear o editar una nota.
 */
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
        ViewHolder holder;
        if (convertView == null) {
            LibretaItemSpinnerBinding binding = LibretaItemSpinnerBinding.inflate(
                    LayoutInflater.from(context), parent, false);
            holder = new ViewHolder(binding);
            binding.getRoot().setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.binding.tituloLibretaSpinner.setText(listaLibretas.get(position).getTitulo());
        return holder.binding.getRoot();
    }

    /** Guarda la referencia a la vista para no reinflarla en cada fila. */
    private static class ViewHolder {
        final LibretaItemSpinnerBinding binding;

        ViewHolder(LibretaItemSpinnerBinding binding) {
            this.binding = binding;
        }
    }
}
