package com.example.notas.UI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notas.R;
import com.example.notas.data.Libreta;

import java.util.List;

public class LibretaAdapter extends RecyclerView.Adapter<LibretaAdapter.LibretaViewHolder> {

    public interface OnLibretaClickListener {
        void onLibretaClick(int position);

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
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.libreta_item, parent, false);
        return new LibretaViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull final LibretaViewHolder holder, int position) {
        Libreta libreta = libretas.get(position);
        holder.titulo.setText(libreta.getTitulo());
        int numNotas = libreta.getNotas().size();
        holder.notas.setText(holder.itemView.getResources().getQuantityString(R.plurals.notas, numNotas, numNotas));

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
        final TextView titulo;
        final TextView notas;

        LibretaViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.textViewTitulo2);
            notas = itemView.findViewById(R.id.textViewNotas);
        }
    }
}
