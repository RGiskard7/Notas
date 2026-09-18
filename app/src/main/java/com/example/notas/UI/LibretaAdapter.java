package com.example.notas.UI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notas.R;
import com.example.notas.data.Libreta;
import com.example.notas.databinding.LibretaItemBinding;

import java.util.List;

/**
 * Adapter del listado de libretas, con su recuento de notas.
 */
public class LibretaAdapter extends RecyclerView.Adapter<LibretaAdapter.LibretaViewHolder> {

    /** Acciones sobre un elemento del listado. */
    public interface OnLibretaClickListener {
        /** Se ha pulsado la libreta de esa posición. */
        void onLibretaClick(int position);

        /** Se ha mantenido pulsada la libreta de esa posición. */
        void onLibretaLongClick(int position);
    }

    private final List<Libreta> libretas;
    private final OnLibretaClickListener listener;

    public LibretaAdapter(List<Libreta> libretas, OnLibretaClickListener listener) {
        this.libretas = libretas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LibretaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LibretaItemBinding binding = LibretaItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LibretaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final LibretaViewHolder holder, int position) {
        Libreta libreta = libretas.get(position);
        String recuento = holder.itemView.getResources()
                .getQuantityString(R.plurals.notas, libreta.getNumNotas(), libreta.getNumNotas());
        holder.binding.imageViewIcono.setImageResource(R.drawable.ic_libretas);
        holder.binding.textViewTitulo2.setText(libreta.getTitulo());
        holder.binding.textViewNotas.setText(recuento);
        holder.itemView.setContentDescription(libreta.getTitulo() + ". " + recuento);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onLibretaClick(holder.getBindingAdapterPosition());
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLibretaLongClick(holder.getBindingAdapterPosition());
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return libretas.size();
    }

    static class LibretaViewHolder extends RecyclerView.ViewHolder {
        final LibretaItemBinding binding;

        LibretaViewHolder(@NonNull LibretaItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
