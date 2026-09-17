package com.example.notas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.notas.UI.EditLibretaViewModel;
import com.example.notas.data.Libreta;
import com.example.notas.data.NotasRepository;
import com.example.notas.databinding.ActivityEditLibretaBinding;

/**
 * Pantalla para crear o editar una libreta.
 */
public class EditLibretaActivity extends AppCompatActivity {
    private ActivityEditLibretaBinding binding;
    private Libreta libreta;
    private EditText titulo;
    private boolean editando;
    private EditLibretaViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditLibretaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editando = false;

        createComponents();

        viewModel = new ViewModelProvider(this).get(EditLibretaViewModel.class);

        if (getIntent().getExtras() != null && getIntent().getExtras().get("tipo").toString().equals("editable")) {
            getSupportActionBar().setTitle(R.string.editar_libreta);
            libreta = (Libreta) getIntent().getSerializableExtra("libreta");
            titulo.setText(libreta.getTitulo());
            editando = true;
        }
    }

    public void createComponents() {
        getSupportActionBar().setTitle(R.string.nueva_libreta);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titulo = binding.editTextTituloLibreta;
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onSupportNavigateUp();
        setResult(Activity.RESULT_CANCELED);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_libreta, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_guardar2) {
            final String tituloTexto = titulo.getText().toString();

            if (tituloTexto.compareTo("") == 0) {
                Toast.makeText(this, R.string.libreta_sin_nombre, Toast.LENGTH_SHORT).show();
                finish();
                return true;
            }

            if (editando) {
                viewModel.editar(libreta.getId(), tituloTexto, new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(EditLibretaActivity.this, R.string.libreta_editada, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } else {
                viewModel.crearSiNoExiste(tituloTexto, new NotasRepository.Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean creada) {
                        if (!creada) {
                            Toast.makeText(EditLibretaActivity.this, R.string.libreta_duplicada, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(EditLibretaActivity.this, R.string.libreta_guardada, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
