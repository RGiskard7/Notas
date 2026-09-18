package com.inkpot.app.UI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.inkpot.app.R;
import com.inkpot.app.data.Nota;
import com.inkpot.app.databinding.NotaItemBinding;
import com.inkpot.app.util.Fechas;
import com.inkpot.app.util.FormatoNota;
import com.inkpot.app.util.PaletaNotas;
import com.inkpot.app.util.Resaltado;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

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
    private String consulta = "";
    private final java.util.Set<Integer> seleccionados = new java.util.HashSet<>();
    private boolean modoSeleccion;

    public NotaAdapter(List<Nota> notas, OnNotaClickListener listener) {
        this.notas = new java.util.ArrayList<>(notas);
        this.listener = listener;
    }

    /**
     * Sustituye el contenido del listado animando solo lo que cambia.
     *
     * <p>Se usa {@link DiffUtil} para que al borrar una nota el hueco se cierre
     * con una transición en lugar de rehacer toda la lista de golpe.</p>
     */
    public void submit(List<Nota> nuevas) {
        final List<Nota> destino = new java.util.ArrayList<>(nuevas);
        final List<Nota> actuales = new java.util.ArrayList<>(notas);
        DiffUtil.DiffResult resultado = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return actuales.size();
            }

            @Override
            public int getNewListSize() {
                return destino.size();
            }

            @Override
            public boolean areItemsTheSame(int posVieja, int posNueva) {
                return actuales.get(posVieja).getId() == destino.get(posNueva).getId();
            }

            @Override
            public boolean areContentsTheSame(int posVieja, int posNueva) {
                return mismoContenido(actuales.get(posVieja), destino.get(posNueva));
            }
        });
        notas.clear();
        notas.addAll(destino);
        resultado.dispatchUpdatesTo(this);
    }

    private boolean mismoContenido(Nota a, Nota b) {
        return a.getId() == b.getId()
                && android.text.TextUtils.equals(a.getTitulo(), b.getTitulo())
                && android.text.TextUtils.equals(a.getTexto(), b.getTexto())
                && a.getFechaCreacion() == b.getFechaCreacion()
                && a.getFechaModificacion() == b.getFechaModificacion()
                && a.getRecordatorio() == b.getRecordatorio()
                && a.isFijada() == b.isFijada()
                && a.getColor() == b.getColor();
    }

    /** Texto de búsqueda que se resalta en cada nota. */
    public void setConsulta(String consulta) {
        this.consulta = consulta == null ? "" : consulta;
    }

    /** Activa o desactiva el modo de selección múltiple. */
    public void setModoSeleccion(boolean modo) {
        this.modoSeleccion = modo;
        if (!modo) {
            seleccionados.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isModoSeleccion() {
        return modoSeleccion;
    }

    /** Marca o desmarca una nota y devuelve cuántas quedan seleccionadas. */
    public int alternarSeleccion(int id) {
        if (!seleccionados.add(id)) {
            seleccionados.remove(id);
        }
        notifyDataSetChanged();
        return seleccionados.size();
    }

    /** Identificadores de las notas seleccionadas. */
    public java.util.Set<Integer> getSeleccionados() {
        return seleccionados;
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
        int colorResaltado = ContextCompat.getColor(holder.itemView.getContext(), R.color.resaltado);
        holder.binding.textViewTitulo.setText(Resaltado.resaltar(nota.getTitulo(), consulta, colorResaltado));
        holder.binding.textViewTexto.setText(Resaltado.resaltar(extracto, consulta, colorResaltado));
        holder.binding.textViewFecha.setText(fecha);
        holder.binding.imageViewFijada.setVisibility(nota.isFijada() ? View.VISIBLE : View.GONE);

        MaterialCardView tarjeta = holder.binding.getRoot();
        if (nota.getColor() != 0) {
            tarjeta.setCardBackgroundColor(PaletaNotas.color(tarjeta.getContext(), nota.getColor()));
        } else {
            tarjeta.setCardBackgroundColor(MaterialColors.getColor(tarjeta, com.google.android.material.R.attr.colorSurface));
        }
        tarjeta.setChecked(modoSeleccion && seleccionados.contains(nota.getId()));

        int[] progreso = FormatoNota.progresoTareas(nota.getTexto());
        String descripcionTareas = null;
        if (progreso[1] > 0) {
            holder.binding.textViewTareas.setText(progreso[0] + "/" + progreso[1]);
            holder.binding.textViewTareas.setVisibility(View.VISIBLE);
            descripcionTareas = holder.itemView.getResources()
                    .getString(R.string.progreso_tareas, progreso[0], progreso[1]);
        } else {
            holder.binding.textViewTareas.setVisibility(View.GONE);
        }

        StringBuilder descripcion = new StringBuilder(nota.getTitulo());
        if (extracto.length() > 0) {
            descripcion.append(". ").append(extracto);
        }
        descripcion.append(". ").append(fecha);
        if (descripcionTareas != null) {
            descripcion.append(". ").append(descripcionTareas);
        }
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
