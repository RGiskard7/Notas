package com.example.notas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notas.UI.AdaptadorListLibretas;
import com.example.notas.UI.EditNotaViewModel;
import com.example.notas.data.Etiqueta;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;
import com.example.notas.databinding.ActivityEditNotaBinding;
import com.example.notas.util.EtiquetaSelection;
import com.example.notas.util.FormatoNota;
import com.example.notas.util.Vinietas;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pantalla para crear o editar una nota.
 *
 * <p>Permite escribir el título y el texto, elegir la libreta y marcar las
 * etiquetas. También ofrece insertar viñetas desde la barra inferior.</p>
 */
public class EditNotaActivity extends AppCompatActivity {
    private ActivityEditNotaBinding binding;
    private Nota nota;
    private Libreta libreta;
    private Libreta oldLibreta;
    private EditText titulo;
    private EditText texto;
    private Spinner spinnerLibretas;
    private ImageButton buttonEtiquetas;
    private TextView numEtiquetas;
    private BottomNavigationView bottomNavigationView;
    private boolean editando = false;
    private List<Libreta> allLibretas;
    private Set<Etiqueta> currentEtiquetasNota;
    private Set<Etiqueta> originalEtiquetasNota;
    private List<Etiqueta> allEtiquetas;
    private EditNotaViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNotaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        allLibretas = new ArrayList<>();
        allEtiquetas = new ArrayList<>();
        currentEtiquetasNota = new HashSet<>();
        originalEtiquetasNota = new HashSet<>();

        if (getIntent().getExtras() != null && getIntent().getExtras().get("tipo").toString().equals("editable")) {
            editando = true;
            nota = (Nota) getIntent().getSerializableExtra("nota");
            oldLibreta = nota.getLibreta();
        }

        createComponents();
        eventRecorder();

        viewModel = new ViewModelProvider(this).get(EditNotaViewModel.class);
        viewModel.getLibretas().observe(this, new Observer<List<Libreta>>() {
            @Override
            public void onChanged(List<Libreta> libretas) {
                actualizarSpinner(libretas);
            }
        });
        viewModel.getEtiquetas().observe(this, new Observer<List<Etiqueta>>() {
            @Override
            public void onChanged(List<Etiqueta> etiquetas) {
                allEtiquetas.clear();
                allEtiquetas.addAll(etiquetas);
            }
        });
        viewModel.getEtiquetasDeNota().observe(this, new Observer<List<Etiqueta>>() {
            @Override
            public void onChanged(List<Etiqueta> etiquetas) {
                currentEtiquetasNota.clear();
                currentEtiquetasNota.addAll(etiquetas);
                originalEtiquetasNota.clear();
                originalEtiquetasNota.addAll(etiquetas);
                numEtiquetas.setText(Integer.toString(currentEtiquetasNota.size()));
            }
        });

        loadData();
    }

    public void loadData() {
        viewModel.cargarLibretas();
        viewModel.cargarEtiquetas();
        if (editando) {
            viewModel.cargarEtiquetasDeNota(nota.getId());
        }
    }

    public void createComponents() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titulo = binding.editTextTituloNwNota;
        texto = binding.editTextContenidoNwNota;
        spinnerLibretas = binding.spinnerOpcionLibretas;
        buttonEtiquetas = binding.buttonEtiquetas;
        numEtiquetas = binding.textView3;
        bottomNavigationView = binding.bottomNavigation;

        fillComponents();
    }

    public void fillComponents() {
        if (editando) {
            getSupportActionBar().setTitle(R.string.editar_nota);
            titulo.setText(nota.getTitulo());
            texto.setText(nota.getTexto());
        } else {
            getSupportActionBar().setTitle(R.string.nueva_nota);
        }
        numEtiquetas.setText(Integer.toString(currentEtiquetasNota.size()));
    }

    private void actualizarSpinner(List<Libreta> libretas) {
        allLibretas.clear();
        allLibretas.addAll(libretas);

        AdaptadorListLibretas adaptador = new AdaptadorListLibretas(getApplicationContext(), allLibretas);
        spinnerLibretas.setAdapter(adaptador);

        int indice = -1;
        if (editando && oldLibreta != null) {
            indice = indiceDe(oldLibreta.getId());
        } else if (!editando && getIntent().getExtras() != null && getIntent().getExtras().containsKey("libretaPadre")) {
            Libreta padre = (Libreta) getIntent().getExtras().get("libretaPadre");
            indice = indiceDe(padre.getId());
        }
        if (indice >= 0) {
            spinnerLibretas.setSelection(indice);
        }
    }

    private int indiceDe(int idLibreta) {
        for (int i = 0; i < allLibretas.size(); i++) {
            if (allLibretas.get(i).getId() == idLibreta) {
                return i;
            }
        }
        return -1;
    }

    public void eventRecorder() {
        spinnerLibretas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                libreta = (Libreta) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        buttonEtiquetas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!allEtiquetas.isEmpty()) {
                    final String[] etiquetasName = new String[allEtiquetas.size()];
                    final boolean[] checkedEtiquetas = new boolean[allEtiquetas.size()];

                    for (int i = 0; i < allEtiquetas.size(); i++) {
                        etiquetasName[i] = allEtiquetas.get(i).getTitulo();
                        checkedEtiquetas[i] = currentEtiquetasNota.contains(allEtiquetas.get(i));
                    }

                    AlertDialog.Builder builderDialog = new AlertDialog.Builder(EditNotaActivity.this);
                    builderDialog.setTitle(R.string.elige_etiquetas);
                    builderDialog.setMultiChoiceItems(etiquetasName, checkedEtiquetas, null);

                    builderDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EtiquetaSelection.Diff diff = EtiquetaSelection.calcular(currentEtiquetasNota, allEtiquetas, checkedEtiquetas);
                            currentEtiquetasNota.removeAll(diff.quitadas);
                            currentEtiquetasNota.addAll(diff.anadidas);
                            numEtiquetas.setText(Integer.toString(currentEtiquetasNota.size()));
                        }
                    });

                    builderDialog.setNegativeButton(R.string.cancelar, null);

                    AlertDialog dialog = builderDialog.create();
                    dialog.show();
                } else {
                    Toast.makeText(EditNotaActivity.this, R.string.no_etiquetas_disponibles, Toast.LENGTH_SHORT).show();
                }
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.action_vinietas) {
                            insertarVinieta();
                        } else if (id == R.id.action_casilla) {
                            insertarCasilla();
                        } else if (id == R.id.action_negrita) {
                            envolverSeleccion("**");
                        } else if (id == R.id.action_cursiva) {
                            envolverSeleccion("*");
                        }
                        return true;
                    }
                });
    }

    private void insertarVinieta() {
        Editable contenido = texto.getText();
        int cursor = texto.getSelectionStart();
        if (cursor < 0) {
            cursor = contenido.length();
        }

        if (cursor == 0) {
            // Sin texto antes del cursor: se añade al final.
            if (TextUtils.isEmpty(contenido)) {
                contenido.insert(contenido.length(), "\t\t\u2022 ");
            } else {
                contenido.insert(contenido.length(), "\n\t\t\u2022 ");
            }
        } else if (TextUtils.isEmpty(contenido)) {
            contenido.insert(cursor, "\t\t\u2022 ");
        } else if (Vinietas.necesitaSaltoDeLinea(contenido, cursor)) {
            contenido.insert(cursor, "\n\t\t\u2022 ");
        } else {
            contenido.insert(cursor, "\t\t\u2022 ");
        }
    }

    /** Inserta una casilla de tarea en la posición del cursor. */
    private void insertarCasilla() {
        Editable contenido = texto.getText();
        int cursor = texto.getSelectionStart();
        if (cursor < 0) {
            cursor = contenido.length();
        }

        String original = contenido.toString();
        String nuevo = FormatoNota.insertarCasilla(original, cursor);
        texto.setText(nuevo);
        texto.setSelection(cursor + (nuevo.length() - original.length()));
    }

    /** Rodea el texto seleccionado con la marca indicada (negrita o cursiva). */
    private void envolverSeleccion(String marca) {
        int inicio = texto.getSelectionStart();
        int fin = texto.getSelectionEnd();
        if (inicio < 0) {
            inicio = texto.getText().length();
        }
        if (fin < inicio) {
            fin = inicio;
        }

        Editable contenido = texto.getText();
        contenido.insert(fin, marca);
        contenido.insert(inicio, marca);
        if (fin == inicio) {
            texto.setSelection(inicio + marca.length());
        } else {
            texto.setSelection(fin + 2 * marca.length());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(Activity.RESULT_CANCELED);
        onBackPressed();
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_nota, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_guardar) {
            String tituloTexto = titulo.getText().toString();
            String textoContenido = texto.getText().toString();

            if (tituloTexto.compareTo("") != 0 || textoContenido.compareTo("") != 0) {
                if (tituloTexto.compareTo("") == 0) {
                    tituloTexto = getString(R.string.nota_sin_titulo);
                    titulo.setText(tituloTexto);
                }
                if (libreta == null && !allLibretas.isEmpty()) {
                    libreta = allLibretas.get(0);
                }

                if (editando) {
                    List<Etiqueta> quitadas = new ArrayList<>(originalEtiquetasNota);
                    quitadas.removeAll(currentEtiquetasNota);
                    List<Etiqueta> anadidas = new ArrayList<>(currentEtiquetasNota);
                    anadidas.removeAll(originalEtiquetasNota);

                    int idLibretaVieja = oldLibreta != null ? oldLibreta.getId() : libreta.getId();
                    viewModel.editarNota(nota.getId(), tituloTexto, textoContenido, idLibretaVieja, libreta.getId(),
                            anadidas, quitadas, new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(EditNotaActivity.this, R.string.nota_editada, Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            });
                } else {
                    viewModel.crearNota(tituloTexto, textoContenido, libreta.getId(),
                            new ArrayList<>(currentEtiquetasNota), new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            });
                }
            } else {
                Toast.makeText(this, R.string.nota_vacia, Toast.LENGTH_SHORT).show();
                finish();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
