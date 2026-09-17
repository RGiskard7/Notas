package com.example.notas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
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
import com.example.notas.util.EtiquetaSelection;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditNotaActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_edit_nota);

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

        titulo = (EditText) findViewById(R.id.editTextTituloNwNota);
        texto = (EditText) findViewById(R.id.editTextContenidoNwNota);
        spinnerLibretas = (Spinner) findViewById(R.id.spinnerOpcionLibretas);
        buttonEtiquetas = (ImageButton) findViewById(R.id.buttonEtiquetas);
        numEtiquetas = (TextView) findViewById(R.id.textView3);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

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
                        if (item.getItemId() == R.id.action_vinietas) {
                            if (texto.getSelectionStart() == 0) {
                                if (TextUtils.isEmpty(texto.getText())) {
                                    texto.getText().insert(texto.getText().length(), "\t\t\u2022 ");
                                } else {
                                    texto.getText().insert(texto.getText().length(), "\n\t\t\u2022 ");
                                }
                            } else {
                                if (TextUtils.isEmpty(texto.getText())) {
                                    texto.getText().insert(texto.getSelectionStart(), "\t\t\u2022 ");
                                } else {
                                    if (texto.getText().toString().charAt(texto.getSelectionStart() - 1) >= 32 &&
                                            texto.getText().toString().charAt(texto.getSelectionStart() - 1) <= 255 ||
                                            texto.getText().toString().charAt(texto.getSelectionStart() - 2) == 0x2022) {
                                        texto.getText().insert(texto.getSelectionStart(), "\n\t\t\u2022 ");
                                    } else {
                                        texto.getText().insert(texto.getSelectionStart(), "\t\t\u2022 ");
                                    }
                                }
                            }
                        }
                        return true;
                    }
                });
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
