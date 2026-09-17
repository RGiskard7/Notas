package com.example.notas.UI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notas.R;
import com.example.notas.data.Etiqueta;

import java.util.List;

public class EtiquetaAdapter extends RecyclerView.Adapter<EtiquetaAdapter.EtiquetaViewHolder> {

    public interface OnEtiquetaClickListener {
        void onEtiquetaClick(int position);

        void onEtiquetaLongClick(int position);
    }

    private final List<Etiqueta> etiquetas;
    private final OnEtiquetaClickListener listener;

    public EtiquetaAdapter(List<Etiqueta> etiquetas, OnEtiquetaClickListener listener) {
        this.etiquetas = etiquetas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EtiquetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.libreta_item, parent, false);
        return new EtiquetaViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull final EtiquetaViewHolder holder, int position) {
        Etiqueta etiqueta = etiquetas.get(position);
        holder.titulo.setText(etiqueta.getTitulo());
        int numNotas = etiqueta.getNumNotas();
        holder.notas.setText(holder.itemView.getResources().getQuantityString(R.plurals.notas, numNotas, numNotas));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEtiquetaClick(holder.getBindingAdapterPosition());
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onEtiquetaLongClick(holder.getBindingAdapterPosition());
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return etiquetas.size();
    }

    static class EtiquetaViewHolder extends RecyclerView.ViewHolder {
        final TextView titulo;
        final TextView notas;

        EtiquetaViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.textViewTitulo2);
            notas = itemView.findViewById(R.id.textViewNotas);
        }
    }
}
