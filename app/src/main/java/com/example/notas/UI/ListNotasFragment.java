package com.example.notas.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notas.EditNotaActivity;
import com.example.notas.MainActivity;
import com.example.notas.R;
import com.example.notas.ViewNotaActivity;
import com.example.notas.ajustes.Preferencias;
import com.example.notas.data.Etiqueta;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;
import com.example.notas.data.NotasRepository;
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

    /** Coloca siempre las notas fijadas por delante del criterio indicado. */
    private static Comparator<Nota> conFijadasPrimero(final Comparator<Nota> criterio) {
        return new Comparator<Nota>() {
            @Override
            public int compare(Nota o1, Nota o2) {
                if (o1.isFijada() != o2.isFijada()) {
                    return o1.isFijada() ? -1 : 1;
                }
                return criterio.compare(o1, o2);
            }
        };
    }

    /** Comparador correspondiente al criterio de orden guardado. */
    private static Comparator<Nota> comparadorDe(String orden) {
        if (Preferencias.ORDEN_FECHA_CREACION_ASC.equals(orden)) {
            return new Comparator<Nota>() {
                @Override
                public int compare(Nota o1, Nota o2) {
                    return Long.compare(o1.getFechaCreacion(), o2.getFechaCreacion());
                }
            };
        }
        if (Preferencias.ORDEN_FECHA_MODIFICACION_DESC.equals(orden)) {
            return new Comparator<Nota>() {
                @Override
                public int compare(Nota o1, Nota o2) {
                    return Long.compare(o2.getFechaModificacion(), o1.getFechaModificacion());
                }
            };
        }
        if (Preferencias.ORDEN_FECHA_MODIFICACION_ASC.equals(orden)) {
            return new Comparator<Nota>() {
                @Override
                public int compare(Nota o1, Nota o2) {
                    return Long.compare(o1.getFechaModificacion(), o2.getFechaModificacion());
                }
            };
        }
        if (Preferencias.ORDEN_TITULO_ASC.equals(orden)) {
            return new Comparator<Nota>() {
                @Override
                public int compare(Nota o1, Nota o2) {
                    return o1.getTitulo().compareToIgnoreCase(o2.getTitulo());
                }
            };
        }
        if (Preferencias.ORDEN_TITULO_DESC.equals(orden)) {
            return new Comparator<Nota>() {
                @Override
                public int compare(Nota o1, Nota o2) {
                    return o2.getTitulo().compareToIgnoreCase(o1.getTitulo());
                }
            };
        }
        return POR_FECHA_DESC;
    }

    private RecyclerView recyclerView;
    private NotaAdapter adaptador;
    private FragmentListNotasBinding binding;
    private List<Nota> listaNotas;
    private List<Nota> listaNotasCompleta;
    private Libreta libreta;
    private Etiqueta etiqueta;
    private SearchView searchView;
    private ListNotasViewModel viewModel;
    private String ordenActual;
    private boolean modoSeleccion;
    private boolean hapticoDeslizamiento;

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
        ordenActual = Preferencias.getOrdenNotas(getActivity());

        createComponents(view);

        viewModel = new ViewModelProvider(this).get(ListNotasViewModel.class);
        viewModel.getNotas().observe(getViewLifecycleOwner(), new Observer<List<Nota>>() {
            @Override
            public void onChanged(List<Nota> notas) {
                listaNotasCompleta.clear();
                listaNotasCompleta.addAll(notas);
                Collections.sort(listaNotasCompleta, conFijadasPrimero(comparadorDe(ordenActual)));
                mostrarNotas();
            }
        });

        cargarSegunAmbito();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (modoSeleccion) {
                    desactivarSeleccion();
                    return;
                }
                setEnabled(false);
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        });

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

    private void mostrarNotas() {
        listaNotas.clear();
        listaNotas.addAll(listaNotasCompleta);
        if (adaptador != null) {
            adaptador.submit(listaNotas);
        }
        if (binding != null) {
            binding.textViewVacio.setVisibility(listaNotas.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    public void createComponents(View view) {
        setHasOptionsMenu(true);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(tituloAmbito());

        adaptador = new NotaAdapter(listaNotas, new NotaAdapter.OnNotaClickListener() {
            @Override
            public void onNotaClick(int position) {
                if (modoSeleccion) {
                    alternarSeleccion(listaNotas.get(position).getId());
                } else {
                    abrirNota(listaNotas.get(position));
                }
            }

            @Override
            public void onNotaLongClick(int position) {
                if (modoSeleccion) {
                    alternarSeleccion(listaNotas.get(position).getId());
                } else {
                    mostrarOpciones(position);
                }
            }
        });
        recyclerView = binding.listViewNotas;
        recyclerView.setAdapter(adaptador);
        aplicarVista();
        configurarDeslizar();
    }

    /** Título de la barra según el ámbito (todas, una libreta o una etiqueta). */
    private String tituloAmbito() {
        if (libreta != null) {
            return getString(R.string.libretas_con_titulo, libreta.getTitulo());
        }
        if (etiqueta != null) {
            return getString(R.string.etiquetas_con_titulo, etiqueta.getTitulo());
        }
        return getString(R.string.todas_las_notas);
    }

    private void abrirNota(Nota nota) {
        Intent intent = new Intent(getActivity(), ViewNotaActivity.class);
        intent.putExtra("nota", nota);
        startActivity(intent);
    }

    /** Permite enviar una nota a la papelera deslizándola, con opción de deshacer. */
    private void configurarDeslizar() {
        final float densidad = getResources().getDisplayMetrics().density;

        final GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(ContextCompat.getColor(getActivity(), R.color.eliminar));
        fondo.setCornerRadius(14 * densidad);

        final Drawable icono = ContextCompat.getDrawable(getActivity(), R.drawable.ic_eliminar).mutate();
        icono.setTint(Color.WHITE);
        final int tamIcono = (int) (24 * densidad);
        final int margen = (int) (20 * densidad);

        final Paint pinturaTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
        pinturaTexto.setColor(Color.WHITE);
        pinturaTexto.setTypeface(Typeface.DEFAULT_BOLD);
        pinturaTexto.setTextSize(14 * densidad);
        final String etiqueta = getString(R.string.eliminar);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.5f;
            }

            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                // Sin "flick": hay que deslizar de verdad, no basta un gesto rápido.
                return Float.MAX_VALUE;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                int id = listaNotas.get(position).getId();
                ProgramadorRecordatorios.cancelar(getActivity(), id);
                viewModel.eliminar(id);
                mostrarDeshacer(id);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View item = viewHolder.itemView;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX != 0) {
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) item.getLayoutParams();
                    int izquierda = item.getLeft() + lp.leftMargin;
                    int derecha = item.getRight() - lp.rightMargin;
                    int arriba = item.getTop() + lp.topMargin;
                    int abajo = item.getBottom() - lp.bottomMargin;

                    fondo.setBounds(izquierda, arriba, derecha, abajo);
                    fondo.draw(c);

                    int centroY = (arriba + abajo) / 2;
                    int anchoTexto = (int) pinturaTexto.measureText(etiqueta);
                    int xIcono;
                    float xTexto;
                    if (dX > 0) {
                        xIcono = izquierda + margen;
                        xTexto = xIcono + tamIcono + margen / 2f;
                    } else {
                        xIcono = derecha - margen - tamIcono;
                        xTexto = xIcono - margen / 2f - anchoTexto;
                    }
                    icono.setBounds(xIcono, centroY - tamIcono / 2, xIcono + tamIcono, centroY + tamIcono / 2);
                    icono.draw(c);
                    c.drawText(etiqueta, xTexto, centroY + pinturaTexto.getTextSize() / 3f, pinturaTexto);

                    float umbral = item.getWidth() * 0.5f;
                    if (!hapticoDeslizamiento && Math.abs(dX) >= umbral) {
                        item.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        hapticoDeslizamiento = true;
                    } else if (Math.abs(dX) < umbral) {
                        hapticoDeslizamiento = false;
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);
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

        boolean seleccion = modoSeleccion;
        menu.findItem(R.id.app_bar_search).setVisible(!seleccion);
        menu.findItem(R.id.action_filtrar).setVisible(!seleccion);
        menu.findItem(R.id.action_importar).setVisible(!seleccion);
        menu.findItem(R.id.action_exportar_todo).setVisible(!seleccion);
        menu.findItem(R.id.action_importar_todo).setVisible(!seleccion);
        menu.findItem(R.id.action_borrar_seleccion).setVisible(seleccion);
        menu.findItem(R.id.action_mover_seleccion).setVisible(seleccion);
        menu.findItem(R.id.action_etiquetar_seleccion).setVisible(seleccion);

        MenuItem vista = menu.findItem(R.id.action_vista);
        if (vista != null) {
            boolean cuadricula = Preferencias.getCuadricula(getActivity());
            vista.setIcon(cuadricula ? R.drawable.ic_lista : R.drawable.ic_cuadricula);
            vista.setTitle(cuadricula ? R.string.vista_lista : R.string.vista_cuadricula);
        }

        searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                guardarBusquedaReciente(query);
                buscar(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                buscar(newText);
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                buscar("");
                return false;
            }
        });
    }

    /** Guarda la búsqueda enviada para ofrecerla como sugerencia. */
    private void guardarBusquedaReciente(String query) {
        if (query != null && !query.trim().isEmpty()) {
            new SearchRecentSuggestions(getActivity(), BusquedaRecienteProvider.AUTHORITY,
                    BusquedaRecienteProvider.MODO).saveRecentQuery(query, null);
        }
    }

    /** Actualiza la búsqueda y el resaltado del listado. */
    private void buscar(String consulta) {
        if (adaptador != null) {
            adaptador.setConsulta(consulta);
        }
        viewModel.buscar(consulta);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filtrar_fecha_asc) {
            aplicarOrden(Preferencias.ORDEN_FECHA_CREACION_ASC);
        } else if (id == R.id.action_filtrar_fecha_des) {
            aplicarOrden(Preferencias.ORDEN_FECHA_CREACION_DESC);
        } else if (id == R.id.action_filtrar_modificacion_des) {
            aplicarOrden(Preferencias.ORDEN_FECHA_MODIFICACION_DESC);
        } else if (id == R.id.action_filtrar_modificacion_asc) {
            aplicarOrden(Preferencias.ORDEN_FECHA_MODIFICACION_ASC);
        } else if (id == R.id.action_filtrar_titulo_asc) {
            aplicarOrden(Preferencias.ORDEN_TITULO_ASC);
        } else if (id == R.id.action_filtrar_titulo_des) {
            aplicarOrden(Preferencias.ORDEN_TITULO_DESC);
        } else if (id == R.id.action_vista) {
            alternarVista();
        } else if (id == R.id.action_borrar_seleccion) {
            borrarSeleccion();
        } else if (id == R.id.action_mover_seleccion) {
            moverSeleccion();
        } else if (id == R.id.action_etiquetar_seleccion) {
            etiquetarSeleccion();
        }

        return super.onOptionsItemSelected(item);
    }

    /** Aplica y guarda el criterio de orden elegido. */
    private void aplicarOrden(String orden) {
        ordenActual = orden;
        Preferencias.setOrdenNotas(getActivity(), orden);
        Collections.sort(listaNotasCompleta, conFijadasPrimero(comparadorDe(orden)));
        mostrarNotas();
    }

    /** Alterna entre lista y cuadrícula. */
    private void alternarVista() {
        Preferencias.setCuadricula(getActivity(), !Preferencias.getCuadricula(getActivity()));
        aplicarVista();
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    /** Aplica el modo de vista (lista o cuadrícula) guardado. */
    private void aplicarVista() {
        if (Preferencias.getCuadricula(getActivity())) {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
    }

    /** Entra en el modo de selección con una nota ya marcada. */
    private void activarSeleccion(int id) {
        modoSeleccion = true;
        adaptador.setModoSeleccion(true);
        adaptador.alternarSeleccion(id);
        actualizarTituloSeleccion();
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    /** Marca o desmarca una nota; si no queda ninguna, sale del modo. */
    private void alternarSeleccion(int id) {
        int total = adaptador.alternarSeleccion(id);
        if (total == 0) {
            desactivarSeleccion();
        } else {
            actualizarTituloSeleccion();
        }
    }

    /** Sale del modo de selección y restaura el título. */
    private void desactivarSeleccion() {
        modoSeleccion = false;
        if (adaptador != null) {
            adaptador.setModoSeleccion(false);
        }
        if (getActivity() != null) {
            ((MainActivity) getActivity()).getSupportActionBar().setTitle(tituloAmbito());
            getActivity().invalidateOptionsMenu();
        }
    }

    private void actualizarTituloSeleccion() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).getSupportActionBar()
                    .setTitle(getString(R.string.seleccionadas, adaptador.getSeleccionados().size()));
        }
    }

    /** Manda a la papelera todas las notas seleccionadas. */
    private void borrarSeleccion() {
        for (Integer id : new ArrayList<>(adaptador.getSeleccionados())) {
            ProgramadorRecordatorios.cancelar(getActivity(), id);
            viewModel.eliminar(id);
        }
        desactivarSeleccion();
    }

    /** Pregunta a qué libreta mover las notas seleccionadas. */
    private void moverSeleccion() {
        viewModel.libretas(new NotasRepository.Callback<List<Libreta>>() {
            @Override
            public void onResult(final List<Libreta> libretas) {
                if (libretas == null || libretas.isEmpty() || getActivity() == null) {
                    return;
                }
                final String[] nombres = new String[libretas.size()];
                for (int i = 0; i < libretas.size(); i++) {
                    nombres[i] = libretas.get(i).getTitulo();
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.mover_a)
                        .setItems(nombres, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                moverSeleccionALibreta(libretas.get(which).getId());
                            }
                        })
                        .show();
            }
        });
    }

    private void moverSeleccionALibreta(int idLibretaNueva) {
        for (Integer id : new ArrayList<>(adaptador.getSeleccionados())) {
            Nota nota = notaPorId(id);
            int idLibretaVieja = (nota != null && nota.getLibreta() != null) ? nota.getLibreta().getId() : idLibretaNueva;
            viewModel.mover(id, idLibretaVieja, idLibretaNueva);
        }
        desactivarSeleccion();
    }

    /** Pregunta qué etiquetas añadir a las notas seleccionadas. */
    private void etiquetarSeleccion() {
        viewModel.etiquetas(new NotasRepository.Callback<List<Etiqueta>>() {
            @Override
            public void onResult(final List<Etiqueta> etiquetas) {
                if (etiquetas == null || etiquetas.isEmpty() || getActivity() == null) {
                    return;
                }
                final String[] nombres = new String[etiquetas.size()];
                final boolean[] marcadas = new boolean[etiquetas.size()];
                for (int i = 0; i < etiquetas.size(); i++) {
                    nombres[i] = etiquetas.get(i).getTitulo();
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.etiquetar)
                        .setMultiChoiceItems(nombres, marcadas, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                marcadas[which] = isChecked;
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                List<Etiqueta> elegidas = new ArrayList<>();
                                for (int i = 0; i < etiquetas.size(); i++) {
                                    if (marcadas[i]) {
                                        elegidas.add(etiquetas.get(i));
                                    }
                                }
                                etiquetarSeleccionCon(elegidas);
                            }
                        })
                        .setNegativeButton(R.string.cancelar, null)
                        .show();
            }
        });
    }

    private void etiquetarSeleccionCon(List<Etiqueta> etiquetas) {
        for (Integer id : new ArrayList<>(adaptador.getSeleccionados())) {
            viewModel.etiquetar(id, etiquetas);
        }
        desactivarSeleccion();
    }

    private Nota notaPorId(int id) {
        for (Nota nota : listaNotas) {
            if (nota.getId() == id) {
                return nota;
            }
        }
        return null;
    }

    // OPCIONES AL MANTENER PULSADO
    private void mostrarOpciones(final int position) {
        final Nota nota = listaNotas.get(position);
        final String[] opciones = {
                getString(nota.isFijada() ? R.string.desfijar : R.string.fijar),
                getString(R.string.editar),
                getString(R.string.seleccionar),
                getString(R.string.eliminar)
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(nota.getTitulo());
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    viewModel.fijar(nota.getId(), !nota.isFijada());
                } else if (which == 1) {
                    editarNota(position);
                } else if (which == 2) {
                    activarSeleccion(nota.getId());
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
