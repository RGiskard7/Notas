package com.example.notas.UI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notas.data.Nota;
import com.example.notas.databinding.NotaItemBinding;
import com.example.notas.util.Fechas;

import java.util.List;

/**
 * Adapter del listado de notas.
 *
 * <p>Muestra el título, un extracto del texto y la fecha de cada nota, y avisa
 * al fragmento cuando se pulsa o se mantiene pulsado un elemento.</p>
 */
public class NotaAdapter extends RecyclerView.Adapter<NotaAdapter.NotaViewHolder> {

    /** Acciones sobre un elemento del listado. */
    public interface OnNotaClickListener {
        /** Se ha pulsado la nota de esa posición. */
        void onNotaClick(int position);

        /** Se ha mantenido pulsada la nota de esa posición. */
        void onNotaLongClick(int position);
    }

    private final List<Nota> notas;
    private final OnNotaClickListener listener;

    public NotaAdapter(List<Nota> notas, OnNotaClickListener listener) {
        this.notas = notas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NotaItemBinding binding = NotaItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NotaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotaViewHolder holder, int position) {
        Nota nota = notas.get(position);
        CharSequence extracto = RenderizadorNota.renderizar(nota.getTexto(), null);
        String fecha = Fechas.formatearNota(nota.getFechaCreacion());
        holder.binding.textViewTitulo.setText(nota.getTitulo());
        holder.binding.textViewTexto.setText(extracto);
        holder.binding.textViewFecha.setText(fecha);

        StringBuilder descripcion = new StringBuilder(nota.getTitulo());
        if (extracto.length() > 0) {
            descripcion.append(". ").append(extracto);
        }
        descripcion.append(". ").append(fecha);
        holder.itemView.setContentDescription(descripcion.toString());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onNotaClick(holder.getBindingAdapterPosition());
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onNotaLongClick(holder.getBindingAdapterPosition());
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notas.size();
    }

    static class NotaViewHolder extends RecyclerView.ViewHolder {
        final NotaItemBinding binding;

        NotaViewHolder(@NonNull NotaItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
