package com.example.notas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notas.UI.AdaptadorListLibretas;
import com.example.notas.data.FactoryDAO;
import com.example.notas.data.ILibretaDAO;
import com.example.notas.data.INotaDAO;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class SegundaActivity extends AppCompatActivity {
    private Nota nota;
    private Libreta libreta;
    private Libreta oldLibreta;
    private EditText titulo;
    private EditText texto;
    private Spinner spinnerLibretas;
    BottomNavigationView bottomNavigationView;
    private boolean editando;
    private FactoryDAO SQLiteFactory;
    private ILibretaDAO libretaDAO;
    private INotaDAO notaDAO;
    private List<Libreta> libretasDisponibles;
    //private List<CheckBox> checkBoxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segunda);

        // Conexion con el proveedor de datos a través del DAO
        SQLiteFactory = FactoryDAO.getFactory(FactoryDAO.SQLITE_FACTORY);
        libretaDAO = SQLiteFactory.getLibretaDao(getApplicationContext());
        notaDAO = SQLiteFactory.getNotaDao(getApplicationContext());

        libretasDisponibles = new ArrayList<>();
        libretaDAO.getAllLibretas(libretasDisponibles);

        editando = false;

        createComponents();
        eventRecorder();

        // Para que aparezca en el spinner como opcion seleccionada la libreta desde donde se esta creando la nota
        if (getIntent().getExtras() != null && getIntent().getExtras().get("tipo").toString().equals("nueva")) {
            if (getIntent().getExtras().containsKey("libretaPadre")) {
                libreta = (Libreta) getIntent().getExtras().get("libretaPadre");

                int i = 0;
                for (Libreta lib : libretasDisponibles) {
                    if (lib.getId() == libreta.getId()) {
                        spinnerLibretas.setSelection(i);
                        break;
                    }
                    i++;
                }
            }
        }

        if (getIntent().getExtras() != null && getIntent().getExtras().get("tipo").toString().equals("editable")) {
            getSupportActionBar().setTitle("Editar nota");
            nota = (Nota) getIntent().getSerializableExtra("nota");
            titulo.setText(nota.getTitulo());
            texto.setText(nota.getTexto());
            oldLibreta = nota.getLibreta();

            // Para que aparezca en el spinner como opcion seleccionada la libreta donde se encuentra la nota a editar
            int i = 0;
            for (Libreta lib : libretasDisponibles) {
                if (lib.getId() == oldLibreta.getId()) {
                    spinnerLibretas.setSelection(i);
                    break;
                }
                i++;
            }

            editando = true;
        }
    }

    public void createComponents() {
        getSupportActionBar().setTitle("Nueva nota");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titulo = (EditText) findViewById(R.id.editTextTituloNwNota);
        texto = (EditText) findViewById(R.id.editTextContenidoNwNota);
        spinnerLibretas = (Spinner) findViewById(R.id.spinnerOpcionLibretas);

        AdaptadorListLibretas adaptador = new AdaptadorListLibretas(getApplicationContext(), libretasDisponibles);
        adaptador.setIsSpinner(true);
        spinnerLibretas.setAdapter(adaptador);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        //checkBoxes = new ArrayList<>();
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

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.action_vinietas) {
                            if (texto.getSelectionStart() == 0) {
                                //Si no esta posicionado el cursor dentro del edittext
                                if (TextUtils.isEmpty(texto.getText())) {
                                    texto.getText().insert(texto.getText().length(),"\t\t\u2022 ");
                                } else {
                                    texto.getText().insert(texto.getText().length(),"\n\t\t\u2022 ");
                                }
                            } else {
                                // Si esta posicionado el cursor dentro del edittext
                                if (TextUtils.isEmpty(texto.getText())) {
                                    texto.getText().insert(texto.getSelectionStart(),"\t\t\u2022 ");
                                } else {
                                    if (texto.getText().toString().charAt(texto.getSelectionStart() - 1) >= 32 &&
                                            texto.getText().toString().charAt(texto.getSelectionStart() - 1) <= 255 ||
                                            texto.getText().toString().charAt(texto.getSelectionStart() - 2) == 0x2022) {
                                        // Si esta posicionado el cursor, el texto no esta vacio, y el texto anterior
                                        // a la posicion del cursor es codigo ascii o una viñeta
                                        texto.getText().insert(texto.getSelectionStart(),"\n\t\t\u2022 ");
                                    } else {
                                        texto.getText().insert(texto.getSelectionStart(),"\t\t\u2022 ");
                                    }
                                }
                            }
                        } /*else if (item.getItemId() == R.id.action_checkbox) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SegundaActivity.this);
                            final EditText input = new EditText(SegundaActivity.this);

                            input.setInputType(InputType.TYPE_CLASS_TEXT);

                            builder.setTitle("Nuevo CheckBox");
                            builder.setView(input);

                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    CheckBox checkBox = new CheckBox(SegundaActivity.this);
                                    checkBox.setText(input.getText().toString());
                                    LinearLayout contenedor = findViewById(R.id.linLayoutCheckBox);
                                    contenedor.addView(checkBox);
                                    checkBoxes.add(checkBox);
                                }
                            });
                            builder.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            builder.create().show();
                        }*/
                        return true;
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(Activity.RESULT_CANCELED);
        super.onSupportNavigateUp();
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_segunda, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_guardar) {
            if (titulo.getText().toString().compareTo("") != 0 || texto.getText().toString().compareTo("") != 0) {
                if (titulo.getText().toString().compareTo("") == 0) {
                    titulo.setText("Nota sin título");
                }

                if (editando) {
                    editando = false;
                    notaDAO.editNota(nota.getId(), titulo.getText().toString(), texto.getText().toString());
                    notaDAO.deleteLibreta(nota.getId(), oldLibreta.getId());
                    libretaDAO.addNotaToLibreta(libreta.getId(), nota.getId());
                    Toast.makeText(this, "Nota editada", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                } else {
                    /*if (notaDAO.existTitulo(titulo.getText().toString())) {
                        Toast.makeText(this, "Ya existe una nota con ese título", Toast.LENGTH_SHORT).show();
                        return false;
                    }*/
                    int idNewNota = notaDAO.createNota(titulo.getText().toString(), texto.getText().toString()); // Añadir nueva nota
                    libretaDAO.addNotaToLibreta(libreta.getId(), idNewNota);
                    /*if (!checkBoxes.isEmpty()) {
                        for(CheckBox ch : checkBoxes) {
                            notaDAO.addCheckBoxToNota(idNewNota, ch);
                        }
                    }*/
                    // Toast.makeText(this, "Nota creada", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "No se puede guardar una nota vacía", Toast.LENGTH_SHORT).show();
            }

            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
