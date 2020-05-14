package com.example.notas.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.notas.CuartaActivity;
import com.example.notas.MainActivity;
import com.example.notas.SegundaActivity;
import com.example.notas.data.Libreta;
import com.example.notas.R;
import com.example.notas.data.FactoryDAO;
import com.example.notas.data.ILibretaDAO;
import com.example.notas.data.Nota;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListLibretasFragment extends Fragment implements SearchView.OnQueryTextListener {
    private ListView lv;
    private AdaptadorListLibretas adaptador;
    private List<Libreta> listaLibretas;
    private ILibretaDAO libretaDAO;
    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_libretas, container, false);

        listaLibretas = new ArrayList<>();
        adaptador = new AdaptadorListLibretas(getActivity(), listaLibretas);
        lv = (ListView) view.findViewById(R.id.listViewLibretas);
        lv.setAdapter(adaptador);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() { // VER NOTA
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Libreta libreta = listaLibretas.get(position);
                Libreta libreta = (Libreta) parent.getItemAtPosition(position);
                List<Nota> notas = new ArrayList<>();
                libretaDAO.getAllNotasFrom(libreta.getId(), notas);
                ListNotasFragment fragment = new ListNotasFragment(libreta); // Listar las notas de la libreta
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
                DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout); // Cerrar la pestaña al presionar
                drawer.closeDrawer(GravityCompat.START);
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(libreta.getTitulo() + " - notas");
                /*if (!notas.isEmpty()) {
                    ListNotasFragment fragment = new ListNotasFragment(libreta); // Listar las notas de la libreta
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
                    DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout); // Cerrar la pestaña al presionar
                    drawer.closeDrawer(GravityCompat.START);
                    ((MainActivity) getActivity()).getSupportActionBar().setTitle(libreta.getTitulo() + " - notas");
                } else {
                    Toast.makeText(getActivity(), "La libreta " + libreta.getTitulo() + " esta vacía", Toast.LENGTH_SHORT).show();
                }*/
            }
        });

        registerForContextMenu(lv);

        // Conexion con el proveedor de datos a través del DAO
        FactoryDAO SQLiteFactory = FactoryDAO.getFactory(FactoryDAO.SQLITE_FACTORY);
        libretaDAO = SQLiteFactory.getLibretaDao(getActivity());

        libretaDAO.getAllLibretas(listaLibretas); // Se carga la base de datos en memoria
        /*Collections.sort(listaLibretas, new Comparator<Libreta>() {
            @Override
            public int compare(Libreta o1, Libreta o2) {
                return o2.getFechaCreacion().compareTo(o1.getFechaCreacion());
            } // Se ordenan las libretas por fecha descendente
        });*/

        searchView = (SearchView) view.findViewById(R.id.search_view2);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                resetListaLibretas();
                return false;
            }
        });

        return view;
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
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.itemEliminar:
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

    private void resetListaLibretas() {
        libretaDAO.getAllLibretas(listaLibretas);
        adaptador.notifyDataSetChanged();
        /*Collections.sort(listaLibretas, new Comparator<Libreta>() { // Se ordenan las notas por fecha descendente
            @Override
            public int compare(Libreta o1, Libreta o2) {
                return o2.getFechaCreacion().compareTo(o1.getFechaCreacion());
            }
        });*/
    }

    @Override
    public void onResume() {
        super.onResume();
        resetListaLibretas();
    }

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
}
