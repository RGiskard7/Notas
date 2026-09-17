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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notas.EditNotaActivity;
import com.example.notas.MainActivity;
import com.example.notas.R;
import com.example.notas.ViewNotaActivity;
import com.example.notas.data.Etiqueta;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;
import com.example.notas.util.Fechas;
import com.example.notas.util.FiltroTitulo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListNotasFragment extends Fragment {
    private static final String ARG_LIBRETA = "arg_libreta";
    private static final String ARG_ETIQUETA = "arg_etiqueta";

    private static final Comparator<Nota> POR_FECHA_DESC = new Comparator<Nota>() {
        @Override
        public int compare(Nota o1, Nota o2) {
            return Fechas.parse(o2.getFechaCreacion()).compareTo(Fechas.parse(o1.getFechaCreacion()));
        }
    };

    private RecyclerView recyclerView;
    private NotaAdapter adaptador;
    private List<Nota> listaNotas;
    private List<Nota> listaNotasCompleta;
    private String consultaActual = "";
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
        View view = inflater.inflate(R.layout.fragment_list_notas, container, false);

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
                aplicarFiltro(consultaActual);
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
    private void aplicarFiltro(String query) {
        consultaActual = query;
        listaNotas.clear();
        listaNotas.addAll(FiltroTitulo.filtrar(listaNotasCompleta, query, new FiltroTitulo.TituloProvider<Nota>() {
            @Override
            public String titulo(Nota item) {
                return item.getTitulo();
            }
        }));
        if (adaptador != null) {
            adaptador.notifyDataSetChanged();
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
        recyclerView = view.findViewById(R.id.listViewNotas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adaptador);
    }

    private void resetListaNotas() {
        viewModel.recargar();
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
                aplicarFiltro(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                aplicarFiltro(newText);
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                consultaActual = "";
                aplicarFiltro("");
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
                    return Fechas.parse(o1.getFechaCreacion()).compareTo(Fechas.parse(o2.getFechaCreacion()));
                }
            });
        }

        if (id == R.id.action_filtrar_fecha_des) {
            ordenarYRefrescar(new Comparator<Nota>() {
                @Override
                public int compare(Nota o1, Nota o2) {
                    return Fechas.parse(o2.getFechaCreacion()).compareTo(Fechas.parse(o1.getFechaCreacion()));
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
        aplicarFiltro(consultaActual);
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
                viewModel.eliminar(listaNotas.get(position).getId());
            }
        });
        builder.setNegativeButton(R.string.negativeBtnAlertDIalog, null);
        builder.create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.recargar();
        }
    }
}
