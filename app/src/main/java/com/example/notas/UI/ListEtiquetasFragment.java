package com.example.notas.UI;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notas.MainActivity;
import com.example.notas.R;
import com.example.notas.data.Etiqueta;
import com.example.notas.data.NotasRepository;
import com.example.notas.databinding.FragmentListEtiquetasBinding;
import com.example.notas.util.FiltroTitulo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Fragmento que lista las etiquetas.
 *
 * <p>Al pulsar una etiqueta se muestran las notas que la llevan; al mantenerla
 * pulsada se ofrecen las opciones de editar o eliminar.</p>
 */
public class ListEtiquetasFragment extends Fragment {
    private RecyclerView recyclerView;
    private EtiquetaAdapter adaptador;
    private FragmentListEtiquetasBinding binding;
    private List<Etiqueta> listaEtiquetas;
    private List<Etiqueta> listaEtiquetasCompleta;
    private SearchView searchView;
    private ListEtiquetasViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListEtiquetasBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        listaEtiquetas = new ArrayList<>();
        listaEtiquetasCompleta = new ArrayList<>();

        createComponents(view);

        viewModel = new ViewModelProvider(this).get(ListEtiquetasViewModel.class);
        viewModel.getEtiquetas().observe(getViewLifecycleOwner(), new Observer<List<Etiqueta>>() {
            @Override
            public void onChanged(List<Etiqueta> etiquetas) {
                listaEtiquetasCompleta.clear();
                listaEtiquetasCompleta.addAll(etiquetas);
                aplicarFiltro(viewModel.getConsulta());
            }
        });
        viewModel.cargar();

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void aplicarFiltro(String query) {
        viewModel.setConsulta(query);
        listaEtiquetas.clear();
        listaEtiquetas.addAll(FiltroTitulo.filtrar(listaEtiquetasCompleta, query, new FiltroTitulo.TituloProvider<Etiqueta>() {
            @Override
            public String titulo(Etiqueta item) {
                return item.getTitulo();
            }
        }));
        if (adaptador != null) {
            adaptador.notifyDataSetChanged();
        }
        if (binding != null) {
            binding.textViewVacio.setVisibility(listaEtiquetas.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void createComponents(View view) {
        setHasOptionsMenu(true);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.etiquetas);

        adaptador = new EtiquetaAdapter(listaEtiquetas, new EtiquetaAdapter.OnEtiquetaClickListener() {
            @Override
            public void onEtiquetaClick(int position) {
                abrirEtiqueta(listaEtiquetas.get(position));
            }

            @Override
            public void onEtiquetaLongClick(int position) {
                mostrarOpciones(position);
            }
        });
        recyclerView = binding.listViewEtiquetas;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adaptador);
    }

    private void abrirEtiqueta(Etiqueta etiqueta) {
        ListNotasFragment fragment = ListNotasFragment.newInstance(etiqueta); // Listar las notas de la etiqueta
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START); // Cerrar la pestaña al presionar
    }

    public void resetListaEtiquetas() {
        if (viewModel != null) {
            viewModel.cargar();
        }
    }

    /** Vuelve a cargar el listado de etiquetas. */
    public void recargar() {
        resetListaEtiquetas();
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
                viewModel.setConsulta("");
                aplicarFiltro("");
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filtrar_titulo_asc) {
            ordenarYRefrescar(new Comparator<Etiqueta>() {
                @Override
                public int compare(Etiqueta o1, Etiqueta o2) {
                    return o1.getTitulo().compareToIgnoreCase(o2.getTitulo());
                }
            });
        }

        if (id == R.id.action_filtrar_titulo_des) {
            ordenarYRefrescar(new Comparator<Etiqueta>() {
                @Override
                public int compare(Etiqueta o1, Etiqueta o2) {
                    return o2.getTitulo().compareToIgnoreCase(o1.getTitulo());
                }
            });
        }

        if (id == R.id.action_recuento_notas_asc) {
            ordenarYRefrescar(new Comparator<Etiqueta>() {
                @Override
                public int compare(Etiqueta o1, Etiqueta o2) {
                    return Integer.compare(o1.getNumNotas(), o2.getNumNotas());
                }
            });
        }

        if (id == R.id.action_recuento_notas_des) {
            ordenarYRefrescar(new Comparator<Etiqueta>() {
                @Override
                public int compare(Etiqueta o1, Etiqueta o2) {
                    return Integer.compare(o2.getNumNotas(), o1.getNumNotas());
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    private void ordenarYRefrescar(Comparator<Etiqueta> comparador) {
        Collections.sort(listaEtiquetasCompleta, comparador);
        aplicarFiltro(viewModel.getConsulta());
    }

    // OPCIONES AL MANTENER PULSADO
    private void mostrarOpciones(final int position) {
        final String[] opciones = {getString(R.string.editar), getString(R.string.eliminar)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(listaEtiquetas.get(position).getTitulo());
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    editarEtiqueta(position);
                } else {
                    confirmarEliminar(position);
                }
            }
        });
        builder.create().show();
    }

    private void editarEtiqueta(int position) {
        final Etiqueta etiquetaEditar = listaEtiquetas.get(position);
        DialogoNombre.mostrar(getActivity(), getString(R.string.editar_etiqueta), etiquetaEditar.getTitulo(),
                new DialogoNombre.OnNombreAceptado() {
                    @Override
                    public void onNombre(String nombre) {
                        if (nombre.equals(etiquetaEditar.getTitulo())) {
                            return;
                        }
                        viewModel.editar(etiquetaEditar.getId(), nombre,
                                new NotasRepository.Callback<Boolean>() {
                                    @Override
                                    public void onResult(Boolean editada) {
                                        if (editada) {
                                            Toast.makeText(getActivity(), R.string.etiqueta_editada, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), R.string.etiqueta_duplicada, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
    }

    private void confirmarEliminar(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.messageAlertDialog3).setTitle(R.string.titleAlertDialog);
        builder.setPositiveButton(R.string.positiveBtnAlertDialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewModel.eliminar(listaEtiquetas.get(position).getId());
                Toast.makeText(getActivity(), R.string.etiqueta_eliminada, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
