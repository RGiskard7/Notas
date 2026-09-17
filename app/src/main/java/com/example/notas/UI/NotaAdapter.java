package com.example.notas.UI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notas.R;
import com.example.notas.data.Nota;

import java.util.List;

public class NotaAdapter extends RecyclerView.Adapter<NotaAdapter.NotaViewHolder> {

    public interface OnNotaClickListener {
        void onNotaClick(int position);

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
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.nota_item, parent, false);
        return new NotaViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotaViewHolder holder, int position) {
        Nota nota = notas.get(position);
        holder.titulo.setText(nota.getTitulo());
        holder.texto.setText(nota.getTexto());
        holder.fecha.setText(nota.getFechaCreacion());

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
        final TextView titulo;
        final TextView texto;
        final TextView fecha;

        NotaViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.textViewTitulo);
            texto = itemView.findViewById(R.id.textViewTexto);
            fecha = itemView.findViewById(R.id.textViewFecha);
        }
    }
}
