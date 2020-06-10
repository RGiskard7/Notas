package com.example.notas.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.notas.CuartaActivity;
import com.example.notas.MainActivity;
import com.example.notas.data.Libreta;
import com.example.notas.R;
import com.example.notas.data.FactoryDAO;
import com.example.notas.data.ILibretaDAO;
import com.example.notas.data.Nota;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListLibretasFragment extends Fragment {
    private ListView lv;
    private AdaptadorListLibretas adaptador;
    private List<Libreta> listaLibretas;
    private FactoryDAO SQLiteFactory;
    private ILibretaDAO libretaDAO;
    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_libretas, container, false);

        // Conexion con el proveedor de datos a través del DAO
        SQLiteFactory = FactoryDAO.getFactory(FactoryDAO.SQLITE_FACTORY);
        libretaDAO = SQLiteFactory.getLibretaDao(getActivity());

        listaLibretas = new ArrayList<>();

        loadData();
        createComponents(view);
        eventRecorder();

        return view;
    }

    public void loadData() {
        libretaDAO.getAllLibretas(listaLibretas); // Se carga la base de datos en memoria
        for (Libreta libreta : listaLibretas) {
            libretaDAO.getAllNotasFrom(libreta.getId(), new ArrayList<Nota>());
        }
    }

    public void createComponents(View view) {
        setHasOptionsMenu(true);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Libretas");

        adaptador = new AdaptadorListLibretas(getActivity(), listaLibretas);
        lv = (ListView) view.findViewById(R.id.listViewLibretas);
        lv.setAdapter(adaptador);


        registerForContextMenu(lv);
    }

    public void eventRecorder() {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() { // VER NOTA
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Libreta libreta = (Libreta) parent.getItemAtPosition(position);
                ListNotasFragment fragment = new ListNotasFragment(libreta); // Listar las notas de la libreta
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
                DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout); // Cerrar la pestaña al presionar
                drawer.closeDrawer(GravityCompat.START);
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(libreta.getTitulo() + " - notas");
            }
        });
    }

    private void resetListaLibretas() {
        loadData();
        adaptador.notifyDataSetChanged();
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
                libretaDAO.getAllLibretas(listaLibretas);
                List<Libreta> listaLibretasCopy = new ArrayList<>(listaLibretas);
                listaLibretas.clear();

                if (!TextUtils.isEmpty(query)) {
                    for (Libreta libreta : listaLibretasCopy) {
                        if (libreta.getTitulo().contains(query)) {
                            listaLibretas.add(libreta);
                        }
                    }
                }

                adaptador.notifyDataSetChanged();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                resetListaLibretas();
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filtrar_titulo_asc) {
            Collections.sort(listaLibretas, new Comparator<Libreta>() {
                @Override
                public int compare(Libreta o1, Libreta o2) {
                    return o1.getTitulo().compareToIgnoreCase(o2.getTitulo());
                }
            });
            adaptador.notifyDataSetChanged();
        }

        if (id == R.id.action_filtrar_titulo_des) {
            Collections.sort(listaLibretas, new Comparator<Libreta>() {
                @Override
                public int compare(Libreta o1, Libreta o2) {
                    return o2.getTitulo().compareToIgnoreCase(o1.getTitulo());
                }
            });
            adaptador.notifyDataSetChanged();
        }

        if (id == R.id.action_recuento_notas_asc) {
            Collections.sort(listaLibretas, new Comparator<Libreta>() {
                @Override
                public int compare(Libreta o1, Libreta o2) {
                    Integer v1 = o1.getNotas().size();
                    Integer v2 = o2.getNotas().size();
                    return v1.compareTo(v2);
                }
            });
            adaptador.notifyDataSetChanged();
        }

        if (id == R.id.action_recuento_notas_des) {
            Collections.sort(listaLibretas, new Comparator<Libreta>() {
                @Override
                public int compare(Libreta o1, Libreta o2) {
                    Integer v1 = o1.getNotas().size();
                    Integer v2 = o2.getNotas().size();
                    return v2.compareTo(v1);
                }
            });
            adaptador.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    // OPCIONES MENU CONTEXTUAL
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.ctx_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.itemEliminar:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.messageAlertDialog2).setTitle(R.string.titleAlertDialog);
                builder.setPositiveButton(R.string.positiveBtnAlertDialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Libreta libretaEliminar = listaLibretas.get(info.position);

                        if (libretaEliminar.getId() != 1 && libretaEliminar.getTitulo() != "Default") {
                            List<Nota> listaNotas = new ArrayList<>();
                            libretaDAO.getAllNotasFrom(libretaEliminar.getId(), listaNotas);
                            libretaDAO.deleteLibreta(libretaEliminar.getId());

                            if (!listaNotas.isEmpty()) {
                                for(Nota nota: listaNotas) {
                                    libretaDAO.addNotaToLibreta(1, nota.getId());
                                }
                                Toast.makeText(getActivity(), "Libreta eliminada, todas las notas han sido movidas a 'Default'", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Libreta eliminada", Toast.LENGTH_SHORT).show();
                            }
                            resetListaLibretas();
                        } else {
                            Toast.makeText(getActivity(), "No se puede eliminar la libreta 'Default'", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton(R.string.negativeBtnAlertDIalog, null);
                builder.create().show();

                return true;

            case R.id.itemEditar:
                Libreta libretaEditar = listaLibretas.get(info.position);

                if (libretaEditar.getId() != 1 && libretaEditar.getTitulo() != "Default") {
                    Intent intent = new Intent(getActivity(), CuartaActivity.class);
                    intent.putExtra("libreta", libretaEditar);
                    intent.putExtra("tipo", "editable");
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "No se puede editar la libreta 'Default'", Toast.LENGTH_SHORT).show();
                }

                return true;

            default:
                return super.onContextItemSelected(item);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        resetListaLibretas();
    }
}
