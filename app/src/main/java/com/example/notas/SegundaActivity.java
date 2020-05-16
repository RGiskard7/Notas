package com.example.notas;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.notas.UI.AdaptadorListLibretas;
import com.example.notas.data.FactoryDAO;
import com.example.notas.data.ILibretaDAO;
import com.example.notas.data.INotaDAO;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;

import java.util.ArrayList;
import java.util.List;

public class SegundaActivity extends AppCompatActivity {
    private Nota nota;
    private Libreta libreta;
    private Libreta oldLibreta;
    private EditText titulo;
    private EditText texto;
    private Spinner spinnerLibretas;
    private boolean editando;
    private FactoryDAO SQLiteFactory;
    private ILibretaDAO libretaDAO;
    private INotaDAO notaDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segunda);

        getSupportActionBar().setTitle("Nueva nota");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titulo = (EditText) findViewById(R.id.editText);
        texto = (EditText) findViewById(R.id.editText2);
        spinnerLibretas = (Spinner) findViewById(R.id.spinnerOpcionLibretas);

        SQLiteFactory = FactoryDAO.getFactory(FactoryDAO.SQLITE_FACTORY);
        libretaDAO = SQLiteFactory.getLibretaDao(getApplicationContext());
        notaDAO = SQLiteFactory.getNotaDao(getApplicationContext());

        List<Libreta> libretasDisponibles = new ArrayList<>();
        libretaDAO.getAllLibretas(libretasDisponibles);
        
        AdaptadorListLibretas adaptador = new AdaptadorListLibretas(getApplicationContext(), libretasDisponibles);
        adaptador.setIsSpinner(true);
        spinnerLibretas.setAdapter(adaptador);
        spinnerLibretas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                libreta = (Libreta) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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


        editando = false;

        if (getIntent().getExtras() != null && getIntent().getExtras().get("tipo").toString().equals("editable")) {
            getSupportActionBar().setTitle("Editar nota");
            nota = (Nota) getIntent().getSerializableExtra("nota");
            titulo.setText(nota.getTitulo());
            texto.setText(nota.getTexto());
            oldLibreta = notaDAO.getLibreta(nota.getId());
            editando = true;
        }
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
                if (titulo.getText().toString().compareTo("") == 0 && texto.getText().toString().compareTo("") != 0) {
                    titulo.setText("Nota sin título");
                }

                if (editando) {
                    editando = false;
                    notaDAO.editNota(nota.getId(), titulo.getText().toString(), texto.getText().toString());
                    notaDAO.deleteLibreta(nota.getId(), oldLibreta.getId());
                    libretaDAO.addNotaToLibreta(libreta.getId(), nota.getId());
                    Toast.makeText(this, "Nota editada", Toast.LENGTH_SHORT).show();
                } else {
                    if (notaDAO.existTitulo(titulo.getText().toString())) {
                        Toast.makeText(this, "Ya existe una nota con ese título", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    int idNewNota = notaDAO.createNota(titulo.getText().toString(), texto.getText().toString()); // Añadir nueva nota
                    libretaDAO.addNotaToLibreta(libreta.getId(), idNewNota);
                    Toast.makeText(this, "Nota creada", Toast.LENGTH_SHORT).show();
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
