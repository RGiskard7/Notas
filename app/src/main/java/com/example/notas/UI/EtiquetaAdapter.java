package com.example.notas.UI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notas.R;
import com.example.notas.data.Etiqueta;
import com.example.notas.databinding.LibretaItemBinding;

import java.util.List;

/**
 * Adapter del listado de etiquetas, con su recuento de notas.
 */
public class EtiquetaAdapter extends RecyclerView.Adapter<EtiquetaAdapter.EtiquetaViewHolder> {

    /** Acciones sobre un elemento del listado. */
    public interface OnEtiquetaClickListener {
        /** Se ha pulsado la etiqueta de esa posición. */
        void onEtiquetaClick(int position);

        /** Se ha mantenido pulsada la etiqueta de esa posición. */
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
        LibretaItemBinding binding = LibretaItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EtiquetaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final EtiquetaViewHolder holder, int position) {
        Etiqueta etiqueta = etiquetas.get(position);
        holder.binding.imageViewIcono.setImageResource(R.drawable.ic_etiquetas);
        holder.binding.textViewTitulo2.setText(etiqueta.getTitulo());
        holder.binding.textViewNotas.setText(holder.itemView.getResources()
                .getQuantityString(R.plurals.notas, etiqueta.getNumNotas(), etiqueta.getNumNotas()));

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
        final LibretaItemBinding binding;

        EtiquetaViewHolder(@NonNull LibretaItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
