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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notas.EditLibretaActivity;
import com.example.notas.MainActivity;
import com.example.notas.R;
import com.example.notas.data.Libreta;
import com.example.notas.util.FiltroTitulo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListLibretasFragment extends Fragment {
    private RecyclerView recyclerView;
    private LibretaAdapter adaptador;
    private List<Libreta> listaLibretas;
    private List<Libreta> listaLibretasCompleta;
    private String consultaActual = "";
    private SearchView searchView;
    private ListLibretasViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_libretas, container, false);

        listaLibretas = new ArrayList<>();
        listaLibretasCompleta = new ArrayList<>();

        createComponents(view);

        viewModel = new ViewModelProvider(this).get(ListLibretasViewModel.class);
        viewModel.getLibretas().observe(getViewLifecycleOwner(), new Observer<List<Libreta>>() {
            @Override
            public void onChanged(List<Libreta> libretas) {
                listaLibretasCompleta.clear();
                listaLibretasCompleta.addAll(libretas);
                aplicarFiltro(consultaActual);
            }
        });
        viewModel.cargar();

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void aplicarFiltro(String query) {
        consultaActual = query;
        listaLibretas.clear();
        listaLibretas.addAll(FiltroTitulo.filtrar(listaLibretasCompleta, query, new FiltroTitulo.TituloProvider<Libreta>() {
            @Override
            public String titulo(Libreta item) {
                return item.getTitulo();
            }
        }));
        if (adaptador != null) {
            adaptador.notifyDataSetChanged();
        }
    }

    public void createComponents(View view) {
        setHasOptionsMenu(true);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.libretas);

        adaptador = new LibretaAdapter(listaLibretas, new LibretaAdapter.OnLibretaClickListener() {
            @Override
            public void onLibretaClick(int position) {
                abrirLibreta(listaLibretas.get(position));
            }

            @Override
            public void onLibretaLongClick(int position) {
                mostrarOpciones(position);
            }
        });
        recyclerView = view.findViewById(R.id.listViewLibretas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adaptador);
    }

    private void abrirLibreta(Libreta libreta) {
        ListNotasFragment fragment = ListNotasFragment.newInstance(libreta); // Listar las notas de la libreta
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START); // Cerrar la pestaña al presionar
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.action_filtrar_fecha_asc).setVisible(false);
        menu.findItem(R.id.action_filtrar_fecha_des).setVisible(false);

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

        if (id == R.id.action_filtrar_titulo_asc) {
            ordenarYRefrescar(new Comparator<Libreta>() {
                @Override
                public int compare(Libreta o1, Libreta o2) {
                    return o1.getTitulo().compareToIgnoreCase(o2.getTitulo());
                }
            });
        }

        if (id == R.id.action_filtrar_titulo_des) {
            ordenarYRefrescar(new Comparator<Libreta>() {
                @Override
                public int compare(Libreta o1, Libreta o2) {
                    return o2.getTitulo().compareToIgnoreCase(o1.getTitulo());
                }
            });
        }

        if (id == R.id.action_recuento_notas_asc) {
            ordenarYRefrescar(new Comparator<Libreta>() {
                @Override
                public int compare(Libreta o1, Libreta o2) {
                    Integer v1 = o1.getNotas().size();
                    Integer v2 = o2.getNotas().size();
                    return v1.compareTo(v2);
                }
            });
        }

        if (id == R.id.action_recuento_notas_des) {
            ordenarYRefrescar(new Comparator<Libreta>() {
                @Override
                public int compare(Libreta o1, Libreta o2) {
                    Integer v1 = o1.getNotas().size();
                    Integer v2 = o2.getNotas().size();
                    return v2.compareTo(v1);
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    private void ordenarYRefrescar(Comparator<Libreta> comparador) {
        Collections.sort(listaLibretasCompleta, comparador);
        aplicarFiltro(consultaActual);
    }

    // OPCIONES AL MANTENER PULSADO
    private void mostrarOpciones(final int position) {
        final String[] opciones = {getString(R.string.editar), getString(R.string.eliminar)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(listaLibretas.get(position).getTitulo());
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    editarLibreta(position);
                } else {
                    confirmarEliminar(position);
                }
            }
        });
        builder.create().show();
    }

    private void editarLibreta(int position) {
        Libreta libretaEditar = listaLibretas.get(position);
        if (libretaEditar.getId() != 1) {
            Intent intent = new Intent(getActivity(), EditLibretaActivity.class);
            intent.putExtra("libreta", libretaEditar);
            intent.putExtra("tipo", "editable");
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), R.string.no_editar_default, Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmarEliminar(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.messageAlertDialog2).setTitle(R.string.titleAlertDialog);
        builder.setPositiveButton(R.string.positiveBtnAlertDialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Libreta libretaEliminar = listaLibretas.get(position);
                if (libretaEliminar.getId() != 1) {
                    boolean teniaNotas = !libretaEliminar.getNotas().isEmpty();
                    viewModel.eliminar(libretaEliminar.getId());
                    if (teniaNotas) {
                        Toast.makeText(getActivity(), R.string.libreta_eliminada_movidas, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), R.string.libreta_eliminada, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.no_eliminar_default, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.negativeBtnAlertDIalog, null);
        builder.create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.cargar();
        }
    }
}
