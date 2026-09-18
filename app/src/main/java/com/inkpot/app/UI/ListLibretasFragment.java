package com.inkpot.app.UI;

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

import com.inkpot.app.MainActivity;
import com.inkpot.app.R;
import com.inkpot.app.data.Libreta;
import com.inkpot.app.data.NotasRepository;
import com.inkpot.app.databinding.FragmentListLibretasBinding;
import com.inkpot.app.util.FiltroTitulo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Fragmento que lista las libretas.
 *
 * <p>Al pulsar una libreta se muestran sus notas; al mantenerla pulsada se
 * ofrecen las opciones de editar o eliminar.</p>
 */
public class ListLibretasFragment extends Fragment {
    private RecyclerView recyclerView;
    private LibretaAdapter adaptador;
    private FragmentListLibretasBinding binding;
    private List<Libreta> listaLibretas;
    private List<Libreta> listaLibretasCompleta;
    private SearchView searchView;
    private ListLibretasViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListLibretasBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        listaLibretas = new ArrayList<>();
        listaLibretasCompleta = new ArrayList<>();

        createComponents(view);

        viewModel = new ViewModelProvider(this).get(ListLibretasViewModel.class);
        viewModel.getLibretas().observe(getViewLifecycleOwner(), new Observer<List<Libreta>>() {
            @Override
            public void onChanged(List<Libreta> libretas) {
                listaLibretasCompleta.clear();
                listaLibretasCompleta.addAll(libretas);
                aplicarFiltro(viewModel.getConsulta());
            }
        });
        viewModel.cargar();

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void aplicarFiltro(String query) {
        viewModel.setConsulta(query);
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
        if (binding != null) {
            binding.estadoVacio.setVisibility(listaLibretas.isEmpty() ? View.VISIBLE : View.GONE);
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
        recyclerView = binding.listViewLibretas;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
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
        menu.findItem(R.id.action_filtrar_modificacion_asc).setVisible(false);
        menu.findItem(R.id.action_filtrar_modificacion_des).setVisible(false);
        menu.findItem(R.id.action_vista).setVisible(false);

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
                    return Integer.compare(o1.getNumNotas(), o2.getNumNotas());
                }
            });
        }

        if (id == R.id.action_recuento_notas_des) {
            ordenarYRefrescar(new Comparator<Libreta>() {
                @Override
                public int compare(Libreta o1, Libreta o2) {
                    return Integer.compare(o2.getNumNotas(), o1.getNumNotas());
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    private void ordenarYRefrescar(Comparator<Libreta> comparador) {
        Collections.sort(listaLibretasCompleta, comparador);
        aplicarFiltro(viewModel.getConsulta());
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
        final Libreta libretaEditar = listaLibretas.get(position);
        if (libretaEditar.getId() == 1) {
            Toast.makeText(getActivity(), R.string.no_editar_default, Toast.LENGTH_SHORT).show();
            return;
        }
        DialogoNombre.mostrar(getActivity(), getString(R.string.editar_libreta), libretaEditar.getTitulo(),
                new DialogoNombre.OnNombreAceptado() {
                    @Override
                    public void onNombre(String nombre) {
                        if (nombre.equals(libretaEditar.getTitulo())) {
                            return;
                        }
                        viewModel.editarSiNoExiste(libretaEditar.getId(), nombre, new NotasRepository.Callback<Boolean>() {
                            @Override
                            public void onResult(Boolean editada) {
                                if (!editada) {
                                    Toast.makeText(getActivity(), R.string.libreta_duplicada, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Toast.makeText(getActivity(), R.string.libreta_guardada, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

    private void confirmarEliminar(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.messageAlertDialog2).setTitle(R.string.titleAlertDialog);
        builder.setPositiveButton(R.string.positiveBtnAlertDialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Libreta libretaEliminar = listaLibretas.get(position);
                if (libretaEliminar.getId() != 1) {
                    boolean teniaNotas = libretaEliminar.getNumNotas() > 0;
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

    /** Vuelve a cargar el listado de libretas. */
    public void resetListaLibretas() {
        if (viewModel != null) {
            viewModel.cargar();
        }
    }

    /** Vuelve a cargar el listado de libretas. */
    public void recargar() {
        if (viewModel != null) {
            viewModel.cargar();
        }
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
