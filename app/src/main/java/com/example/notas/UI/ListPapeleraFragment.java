package com.example.notas.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notas.MainActivity;
import com.example.notas.R;
import com.example.notas.ViewNotaActivity;
import com.example.notas.data.Nota;
import com.example.notas.databinding.FragmentListNotasBinding;
import com.example.notas.recordatorios.ProgramadorRecordatorios;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento que muestra la papelera: las notas eliminadas, desde la más
 * reciente. Al mantener pulsada una nota se puede restaurar o borrar del todo.
 */
public class ListPapeleraFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotaAdapter adaptador;
    private List<Nota> listaNotas;
    private FragmentListNotasBinding binding;
    private ListPapeleraViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentListNotasBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        listaNotas = new ArrayList<>();
        createComponents();

        viewModel = new ViewModelProvider(this).get(ListPapeleraViewModel.class);
        viewModel.getNotas().observe(getViewLifecycleOwner(), new Observer<List<Nota>>() {
            @Override
            public void onChanged(List<Nota> notas) {
                listaNotas.clear();
                listaNotas.addAll(notas);
                mostrarNotas();
            }
        });
        viewModel.cargar();

        return view;
    }

    private void mostrarNotas() {
        if (adaptador != null) {
            adaptador.submit(listaNotas);
        }
        if (binding != null) {
            binding.textViewVacio.setVisibility(listaNotas.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void createComponents() {
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.papelera);

        adaptador = new NotaAdapter(listaNotas, new NotaAdapter.OnNotaClickListener() {
            @Override
            public void onNotaClick(int position) {
                Intent intent = new Intent(getActivity(), ViewNotaActivity.class);
                intent.putExtra("nota", listaNotas.get(position));
                startActivity(intent);
            }

            @Override
            public void onNotaLongClick(int position) {
                mostrarOpciones(position);
            }
        });
        recyclerView = binding.listViewNotas;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adaptador);
    }

    private void mostrarOpciones(final int position) {
        final String[] opciones = {getString(R.string.restaurar), getString(R.string.eliminar_definitivamente)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(listaNotas.get(position).getTitulo());
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    restaurar(position);
                } else {
                    confirmarBorrado(position);
                }
            }
        });
        builder.create().show();
    }

    private void restaurar(int position) {
        Nota nota = listaNotas.get(position);
        viewModel.restaurar(nota.getId());
        if (nota.getRecordatorio() > System.currentTimeMillis()) {
            ProgramadorRecordatorios.programar(getActivity(), nota.getId(), nota.getRecordatorio(), nota.getTitulo());
        }
        Toast.makeText(getActivity(), R.string.nota_restaurada, Toast.LENGTH_SHORT).show();
    }

    private void confirmarBorrado(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.messageAlertDialog).setTitle(R.string.titleAlertDialog);
        builder.setPositiveButton(R.string.positiveBtnAlertDialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ProgramadorRecordatorios.cancelar(getActivity(), listaNotas.get(position).getId());
                viewModel.borrarDefinitivamente(listaNotas.get(position).getId());
                Toast.makeText(getActivity(), R.string.nota_eliminada, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.negativeBtnAlertDIalog, null);
        builder.create().show();
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_filtrar_fecha_asc).setVisible(false);
        menu.findItem(R.id.action_filtrar_fecha_des).setVisible(false);
        menu.findItem(R.id.action_filtrar_modificacion_asc).setVisible(false);
        menu.findItem(R.id.action_filtrar_modificacion_des).setVisible(false);
        menu.findItem(R.id.action_filtrar_titulo_asc).setVisible(false);
        menu.findItem(R.id.action_filtrar_titulo_des).setVisible(false);
        menu.findItem(R.id.action_recuento_notas_asc).setVisible(false);
        menu.findItem(R.id.action_recuento_notas_des).setVisible(false);
        menu.findItem(R.id.action_vista).setVisible(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.cargar();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
