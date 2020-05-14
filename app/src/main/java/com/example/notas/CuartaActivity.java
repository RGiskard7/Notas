package com.example.notas;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.notas.data.FactoryDAO;
import com.example.notas.data.ILibretaDAO;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;

public class CuartaActivity extends AppCompatActivity {
    private Libreta libreta;
    private EditText titulo;
    private boolean editando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuarta);

        getSupportActionBar().setTitle("Nueva libreta");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titulo = (EditText) findViewById(R.id.tituloLibreta);

        editando = false;

        if (getIntent().getExtras() != null && getIntent().getExtras().get("tipo").toString().equals("editable")) {
            getSupportActionBar().setTitle("Editar libreta");
            libreta = (Libreta) getIntent().getSerializableExtra("libreta");
            titulo.setText(libreta.getTitulo());
            editando = true;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onSupportNavigateUp();
        setResult(Activity.RESULT_CANCELED);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cuarta, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_guardar2) {
            FactoryDAO SQLiteFactory = FactoryDAO.getFactory(FactoryDAO.SQLITE_FACTORY);
            ILibretaDAO libretaDAO = SQLiteFactory.getLibretaDao(getApplicationContext());

            if (editando) {
                editando = false;
                libretaDAO.editLibreta(libreta.getId(), titulo.getText().toString()); // Actualizar libreta
                Toast.makeText(this, "Libreta guardada", Toast.LENGTH_SHORT).show();
            } else {
                libretaDAO.createLibreta(titulo.getText().toString()); // Añadir nueva libreta
                Toast.makeText(this, "Libreta guardada", Toast.LENGTH_SHORT).show();
            }

            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
