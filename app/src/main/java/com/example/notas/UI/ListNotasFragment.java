package com.example.notas.UI;

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

import androidx.fragment.app.Fragment;

import com.example.notas.MainActivity;
import com.example.notas.data.ILibretaDAO;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;
import com.example.notas.R;
import com.example.notas.SegundaActivity;
import com.example.notas.TerceraActivity;
import com.example.notas.data.FactoryDAO;
import com.example.notas.data.INotaDAO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListNotasFragment extends Fragment implements SearchView.OnQueryTextListener {
    private ListView lv;
    private AdaptadorListNotas adaptador;
    private List<Nota> listaNotas;
    private FactoryDAO SQLiteFactory;
    private INotaDAO notaDAO;
    private ILibretaDAO libretaDAO;
    private Libreta libreta;
    private SearchView searchView;

    public ListNotasFragment(Libreta libreta) {
        this.libreta = libreta;
    }

    public ListNotasFragment() {
        libreta = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_notas, container, false);

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.show();

        // Conexion con el proveedor de datos a través del DAO
        SQLiteFactory = FactoryDAO.getFactory(FactoryDAO.SQLITE_FACTORY);
        notaDAO = SQLiteFactory.getNotaDao(getActivity());
        libretaDAO = SQLiteFactory.getLibretaDao(getActivity());

        // Se carga la base de datos en memoria
        if (listaNotas == null) {
            listaNotas = new ArrayList<>();
            notaDAO.getAllNotas(listaNotas);
        } else {
            listaNotas = new ArrayList<>();
            libretaDAO.getAllNotasFrom(libreta.getId(), listaNotas);
        }

        Collections.sort(listaNotas, new Comparator<Nota>() { // Se ordenan las notas por fecha descendente
            @Override
            public int compare(Nota o1, Nota o2) {
                return o2.getFechaCreacion().compareTo(o1.getFechaCreacion());
            }
        });

        adaptador = new AdaptadorListNotas(getActivity(), listaNotas);
        lv = (ListView) view.findViewById(R.id.listViewNotas);
        lv.setAdapter(adaptador);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() { // VER NOTA
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), TerceraActivity.class);
                intent.putExtra("nota", listaNotas.get(position));
                startActivity(intent);
            }
        });

        registerForContextMenu(lv);

        searchView = (SearchView) view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                resetListaNotas();
                return false;
            }
        });

        return view;
    }

    public Libreta getLibreta() {
        return libreta;
    }

    private void resetListaNotas() {
        if (libreta == null) {
            notaDAO.getAllNotas(listaNotas);
        } else {
            libretaDAO.getAllNotasFrom(libreta.getId(), listaNotas);
        }
        adaptador.notifyDataSetChanged();
        Collections.sort(listaNotas, new Comparator<Nota>() { // Se ordenan las notas por fecha descendente
            @Override
            public int compare(Nota o1, Nota o2) {
                return o2.getFechaCreacion().compareTo(o1.getFechaCreacion());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        resetListaNotas();
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
                Nota notaEliminar = listaNotas.get(info.position);
                notaDAO.deleteNota(notaEliminar.getId()); // Eliminar nota por id
                resetListaNotas();
                Toast.makeText(getActivity(), "Nota eliminada", Toast.LENGTH_SHORT).show();

                return true;

            case R.id.itemEditar:
                Intent intent = new Intent(getActivity(), SegundaActivity.class);
                intent.putExtra("nota", listaNotas.get(info.position));
                intent.putExtra("tipo", "editable");
                startActivity(intent);

                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        notaDAO.getAllNotas(listaNotas);
        List<Nota> listaNotasCopy = new ArrayList<>(listaNotas);
        listaNotas.clear();

        if (!TextUtils.isEmpty(query)) {
            for (Nota nota : listaNotasCopy) {
                if (nota.getTitulo().contains(query)) {
                    listaNotas.add(nota);
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
