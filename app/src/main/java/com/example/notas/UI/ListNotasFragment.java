package com.example.notas.UI;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notas.EditNotaActivity;
import com.example.notas.MainActivity;
import com.example.notas.R;
import com.example.notas.ViewNotaActivity;
import com.example.notas.data.Etiqueta;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;
import com.example.notas.databinding.FragmentListNotasBinding;
import com.example.notas.recordatorios.ProgramadorRecordatorios;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Fragmento que lista notas.
 *
 * <p>Puede mostrar todas las notas, las de una libreta o las de una etiqueta,
 * según los argumentos con los que se cree. Incluye búsqueda por título y
 * ordenación por fecha, título o recuento.</p>
 */
public class ListNotasFragment extends Fragment {
    private static final String ARG_LIBRETA = "arg_libreta";
    private static final String ARG_ETIQUETA = "arg_etiqueta";

    private static final Comparator<Nota> POR_FECHA_DESC = new Comparator<Nota>() {
        @Override
        public int compare(Nota o1, Nota o2) {
            return Long.compare(o2.getFechaCreacion(), o1.getFechaCreacion());
        }
    };

    private RecyclerView recyclerView;
    private NotaAdapter adaptador;
    private FragmentListNotasBinding binding;
    private List<Nota> listaNotas;
    private List<Nota> listaNotasCompleta;
    private Libreta libreta;
    private Etiqueta etiqueta;
    private SearchView searchView;
    private ListNotasViewModel viewModel;

    public ListNotasFragment() {
        libreta = null;
        etiqueta = null;
    }

    public static ListNotasFragment newInstance() {
        return new ListNotasFragment();
    }

    public static ListNotasFragment newInstance(Libreta libreta) {
        ListNotasFragment fragment = new ListNotasFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LIBRETA, libreta);
        fragment.setArguments(args);
        return fragment;
    }

    public static ListNotasFragment newInstance(Etiqueta etiqueta) {
        ListNotasFragment fragment = new ListNotasFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ETIQUETA, etiqueta);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            libreta = (Libreta) getArguments().getSerializable(ARG_LIBRETA);
            etiqueta = (Etiqueta) getArguments().getSerializable(ARG_ETIQUETA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListNotasBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        listaNotas = new ArrayList<>();
        listaNotasCompleta = new ArrayList<>();

        createComponents(view);

        viewModel = new ViewModelProvider(this).get(ListNotasViewModel.class);
        viewModel.getNotas().observe(getViewLifecycleOwner(), new Observer<List<Nota>>() {
            @Override
            public void onChanged(List<Nota> notas) {
                listaNotasCompleta.clear();
                listaNotasCompleta.addAll(notas);
                Collections.sort(listaNotasCompleta, POR_FECHA_DESC);
                mostrarNotas();
            }
        });

        cargarSegunAmbito();

        return view;
    }

    public Libreta getLibreta() {
        return libreta;
    }

    public Etiqueta getEtiqueta() {
        return etiqueta;
    }

    private void cargarSegunAmbito() {
        if (libreta != null) {
            viewModel.cargarDeLibreta(libreta.getId());
        } else if (etiqueta != null) {
            viewModel.cargarDeEtiqueta(etiqueta.getId());
        } else {
            viewModel.cargarTodas();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void mostrarNotas() {
        listaNotas.clear();
        listaNotas.addAll(listaNotasCompleta);
        if (adaptador != null) {
            adaptador.notifyDataSetChanged();
        }
        if (binding != null) {
            binding.textViewVacio.setVisibility(listaNotas.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    public void createComponents(View view) {
        setHasOptionsMenu(true);

        String titulo;
        if (libreta != null) {
            titulo = getString(R.string.libretas_con_titulo, libreta.getTitulo());
        } else if (etiqueta != null) {
            titulo = getString(R.string.etiquetas_con_titulo, etiqueta.getTitulo());
        } else {
            titulo = getString(R.string.todas_las_notas);
        }
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(titulo);

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
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adaptador);
    }

    private void resetListaNotas() {
        viewModel.recargar();
    }

    /** Vuelve a cargar el listado con el ámbito y la búsqueda actuales. */
    public void recargar() {
        if (viewModel != null) {
            viewModel.recargar();
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.action_recuento_notas_asc).setVisible(false);
        menu.findItem(R.id.action_recuento_notas_des).setVisible(false);

        searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.buscar(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.buscar(newText);
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                viewModel.buscar("");
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filtrar_fecha_asc) {
            ordenarYRefrescar(new Comparator<Nota>() {
                @Override
                public int compare(Nota o1, Nota o2) {
                    return Long.compare(o1.getFechaCreacion(), o2.getFechaCreacion());
                }
            });
        }

        if (id == R.id.action_filtrar_fecha_des) {
            ordenarYRefrescar(new Comparator<Nota>() {
                @Override
                public int compare(Nota o1, Nota o2) {
                    return Long.compare(o2.getFechaCreacion(), o1.getFechaCreacion());
                }
            });
        }

        if (id == R.id.action_filtrar_titulo_asc) {
            ordenarYRefrescar(new Comparator<Nota>() {
                @Override
                public int compare(Nota o1, Nota o2) {
                    return o1.getTitulo().compareToIgnoreCase(o2.getTitulo());
                }
            });
        }

        if (id == R.id.action_filtrar_titulo_des) {
            ordenarYRefrescar(new Comparator<Nota>() {
                @Override
                public int compare(Nota o1, Nota o2) {
                    return o2.getTitulo().compareToIgnoreCase(o1.getTitulo());
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    private void ordenarYRefrescar(Comparator<Nota> comparador) {
        Collections.sort(listaNotasCompleta, comparador);
        mostrarNotas();
    }

    // OPCIONES AL MANTENER PULSADO
    private void mostrarOpciones(final int position) {
        final String[] opciones = {getString(R.string.editar), getString(R.string.eliminar)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(listaNotas.get(position).getTitulo());
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    editarNota(position);
                } else {
                    confirmarEliminar(position);
                }
            }
        });
        builder.create().show();
    }

    private void editarNota(int position) {
        Intent intent = new Intent(getActivity(), EditNotaActivity.class);
        intent.putExtra("nota", listaNotas.get(position));
        intent.putExtra("tipo", "editable");
        startActivity(intent);
    }

    private void confirmarEliminar(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.messageAlertDialog).setTitle(R.string.titleAlertDialog);
        builder.setPositiveButton(R.string.positiveBtnAlertDialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final int id = listaNotas.get(position).getId();
                ProgramadorRecordatorios.cancelar(getActivity(), id);
                viewModel.eliminar(id);
                mostrarDeshacer(id);
            }
        });
        builder.setNegativeButton(R.string.negativeBtnAlertDIalog, null);
        builder.create().show();
    }

    /** Avisa de que la nota se ha movido a la papelera y ofrece deshacerlo. */
    private void mostrarDeshacer(final int id) {
        Snackbar.make(binding.getRoot(), R.string.nota_en_papelera, Snackbar.LENGTH_LONG)
                .setAction(R.string.deshacer, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewModel.restaurar(id);
                    }
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.recargar();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
