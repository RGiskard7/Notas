package com.example.notas.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
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
import com.example.notas.R;
import com.example.notas.data.Etiqueta;
import com.example.notas.data.FactoryDAO;
import com.example.notas.data.IEtiquetaDAO;
import com.example.notas.data.ILibretaDAO;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListEtiquetasFragment extends Fragment {
    private ListView lv;
    private AdaptadorListEtiquetas adaptador;
    private List<Etiqueta> listaEtiquetas;
    private FactoryDAO SQLiteFactory;
    private IEtiquetaDAO etiquetaDAO;
    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_etiquetas, container, false);

        // Conexion con el proveedor de datos a través del DAO
        SQLiteFactory = FactoryDAO.getFactory(FactoryDAO.SQLITE_FACTORY);
        etiquetaDAO = SQLiteFactory.getEtiquetaDao(getActivity());

        listaEtiquetas = new ArrayList<>();

        loadData();
        createComponents(view);
        eventRecorder();

        return view;
    }

    private void loadData() {
        etiquetaDAO.getAllEtiquetas(listaEtiquetas); // Se carga la base de datos en memoria
        for (Etiqueta etiqueta : listaEtiquetas) {
            etiquetaDAO.getAllNotasFrom(etiqueta.getId(), new ArrayList<Nota>());
        }
    }

    private void createComponents(View view) {
        setHasOptionsMenu(true);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Etiquetas");

        adaptador = new AdaptadorListEtiquetas(getActivity(), listaEtiquetas);
        lv = (ListView) view.findViewById(R.id.listViewEtiquetas);
        lv.setAdapter(adaptador);

        registerForContextMenu(lv);
    }

    private void eventRecorder() {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() { // VER NOTA
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Etiqueta etiqueta = (Etiqueta) parent.getItemAtPosition(position);
                ListNotasFragment fragment = new ListNotasFragment(etiqueta); // Listar las notas de la etiqueta

                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
                DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START); // Cerrar la pestaña al presionar
            }
        });
    }

    public void resetListaEtiquetas() {
        loadData();
        adaptador.notifyDataSetChanged();
    }

    public void multipleSelectionList(Boolean selection) {
        if (selection) {
            lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        } else {
            lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
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
                etiquetaDAO.getAllEtiquetas(listaEtiquetas);
                List<Etiqueta> listaEtiquetasCopy = new ArrayList<>(listaEtiquetas);
                listaEtiquetas.clear();

                if (!TextUtils.isEmpty(query)) {
                    for (Etiqueta etiqueta : listaEtiquetasCopy) {
                        if (etiqueta.getTitulo().contains(query)) {
                            listaEtiquetas.add(etiqueta);
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
                resetListaEtiquetas();
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filtrar_titulo_asc) {
            Collections.sort(listaEtiquetas, new Comparator<Etiqueta>() {
                @Override
                public int compare(Etiqueta o1, Etiqueta o2) {
                    return o1.getTitulo().compareToIgnoreCase(o2.getTitulo());
                }
            });
            adaptador.notifyDataSetChanged();
        }

        if (id == R.id.action_filtrar_titulo_des) {
            Collections.sort(listaEtiquetas, new Comparator<Etiqueta>() {
                @Override
                public int compare(Etiqueta o1, Etiqueta o2) {
                    return o2.getTitulo().compareToIgnoreCase(o1.getTitulo());
                }
            });
            adaptador.notifyDataSetChanged();
        }

        if (id == R.id.action_recuento_notas_asc) {
            Collections.sort(listaEtiquetas, new Comparator<Etiqueta>() {
                @Override
                public int compare(Etiqueta o1, Etiqueta o2) {
                    Integer v1 = o1.getNotas().size();
                    Integer v2 = o2.getNotas().size();
                    return v1.compareTo(v2);
                }
            });
            adaptador.notifyDataSetChanged();
        }

        if (id == R.id.action_recuento_notas_des) {
            Collections.sort(listaEtiquetas, new Comparator<Etiqueta>() {
                @Override
                public int compare(Etiqueta o1, Etiqueta o2) {
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
                builder.setMessage(R.string.messageAlertDialog3).setTitle(R.string.titleAlertDialog);
                builder.setPositiveButton(R.string.positiveBtnAlertDialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Etiqueta etiquetaEliminar = listaEtiquetas.get(info.position);
                        List<Nota> listaNotas = new ArrayList<>();
                        etiquetaDAO.deleteEtiqueta(etiquetaEliminar.getId());
                        Toast.makeText(getActivity(), "Etiqueta eliminada", Toast.LENGTH_SHORT).show();
                        resetListaEtiquetas();
                    }
                });

                builder.setNegativeButton(R.string.negativeBtnAlertDIalog, null);
                builder.create().show();

                return true;

            case R.id.itemEditar:
                final Etiqueta etiquetaEditar = listaEtiquetas.get(info.position);

                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(etiquetaEditar.getTitulo());

                dialog.setTitle("Editar etiqueta");
                dialog.setView(input);

                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FactoryDAO SQLiteFactory = FactoryDAO.getFactory(FactoryDAO.SQLITE_FACTORY);
                        IEtiquetaDAO etiquetaDAO = SQLiteFactory.getEtiquetaDao(getActivity());

                        if (etiquetaDAO.existTitulo(input.getText().toString())) {
                            Toast.makeText(getActivity(), "Ya existe una etiqueta con ese título", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        etiquetaDAO.editEtiqueta(etiquetaEditar.getId(), input.getText().toString()); // Editar etiqueta
                        etiquetaDAO.closeDB();
                        resetListaEtiquetas();

                        Toast.makeText(getActivity(), "Etiqueta editada", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                dialog.create().show();

                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resetListaEtiquetas();
    }

    @Override
    public void onDestroy() {
        etiquetaDAO.closeDB();
        super.onDestroy();
    }
}
