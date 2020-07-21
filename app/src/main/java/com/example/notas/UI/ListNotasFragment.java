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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListNotasFragment extends Fragment {
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

        // Conexion con el proveedor de datos a través del DAO
        SQLiteFactory = FactoryDAO.getFactory(FactoryDAO.SQLITE_FACTORY);
        notaDAO = SQLiteFactory.getNotaDao(getActivity());
        libretaDAO = SQLiteFactory.getLibretaDao(getActivity());

        listaNotas = new ArrayList<>();

        loadData();
        createComponents(view);
        eventRecorder();

        return view;
    }

    public void loadData() {
        if (libreta == null) { // Se carga la base de datos en memoria
            notaDAO.getAllNotas(listaNotas);
        } else {
            libretaDAO.getAllNotasFrom(libreta.getId(), listaNotas);
        }

        Collections.sort(listaNotas, new Comparator<Nota>() { // Se ordenan las notas por fecha descendente
            @Override
            public int compare(Nota o1, Nota o2) {
                return o2.getFechaCreacion().compareTo(o1.getFechaCreacion());
            }
        });
    }

    public void createComponents(View view) {
        setHasOptionsMenu(true); // Habilita la modificacion del menu superior aniadido en la actividad
                                 // Permite añadir acciones a las opciones del menu superior de la actividad

        if (libreta == null) {
            ((MainActivity) getActivity()).getSupportActionBar().setTitle("Todas las notas");
        } else {
            ((MainActivity) getActivity()).getSupportActionBar().setTitle(libreta.getTitulo() + " - notas");
        }

        adaptador = new AdaptadorListNotas(getActivity(), listaNotas);
        lv = (ListView) view.findViewById(R.id.listViewNotas);
        lv.setAdapter(adaptador);

        registerForContextMenu(lv);
    }

    public void eventRecorder() {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() { // VER NOTA
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), TerceraActivity.class);
                intent.putExtra("nota", listaNotas.get(position));
                startActivity(intent);
            }
        });
    }

    public Libreta getLibreta() {
        return libreta;
    }

    private void resetListaNotas() {
        loadData();
        adaptador.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        resetListaNotas();
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
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                resetListaNotas();
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filtrar_fecha_asc) {
            Collections.sort(listaNotas, new Comparator<Nota>() {
                @Override
                public int compare(Nota o1, Nota o2) {
                    return o1.getFechaCreacion().compareTo(o2.getFechaCreacion());
                }
            });
            adaptador.notifyDataSetChanged();
        }

        if (id == R.id.action_filtrar_fecha_des) {
            Collections.sort(listaNotas, new Comparator<Nota>() {
                @Override
                public int compare(Nota o1, Nota o2) {
                    return o2.getFechaCreacion().compareTo(o1.getFechaCreacion());
                }
            });
            adaptador.notifyDataSetChanged();
        }

        if (id == R.id.action_filtrar_titulo_asc) {
            Collections.sort(listaNotas, new Comparator<Nota>() {
                @Override
                public int compare(Nota o1, Nota o2) {
                    return o1.getTitulo().compareToIgnoreCase(o2.getTitulo());
                }
            });
            adaptador.notifyDataSetChanged();
        }

        if (id == R.id.action_filtrar_titulo_des) {
            Collections.sort(listaNotas, new Comparator<Nota>() {
                @Override
                public int compare(Nota o1, Nota o2) {
                    return o2.getTitulo().compareToIgnoreCase(o1.getTitulo());
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
                builder.setMessage(R.string.messageAlertDialog).setTitle(R.string.titleAlertDialog);
                builder.setPositiveButton(R.string.positiveBtnAlertDialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Nota notaEliminar = listaNotas.get(info.position);
                        notaDAO.deleteNota(notaEliminar.getId()); // Eliminar nota por id
                        resetListaNotas();
                        // Toast.makeText(getActivity(), "Nota eliminada", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(R.string.negativeBtnAlertDIalog, null);
                builder.create().show();

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
}
