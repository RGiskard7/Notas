package com.example.notas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notas.data.FactoryDAO;
import com.example.notas.data.INotaDAO;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;

public class TerceraActivity extends AppCompatActivity {
    private TextView titulo;
    private TextView texto;
    private TextView fecha;
    private TextView Txlibreta;
    private Nota nota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tercera);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titulo = (TextView) findViewById(R.id.textViewTitulo);
        texto = (TextView) findViewById(R.id.textViewTexto);
        texto.setMovementMethod(new ScrollingMovementMethod());
        fecha = (TextView) findViewById(R.id.textViewFecha);
        Txlibreta = (TextView) findViewById(R.id.textViewLibreta);

        nota = (Nota) getIntent().getSerializableExtra("nota");

        titulo.setText(nota.getTitulo());
        texto.setText(nota.getTexto());
        fecha.setText(nota.getFechaCreacion());

        FactoryDAO SQLiteFactory = FactoryDAO.getFactory(FactoryDAO.SQLITE_FACTORY);
        INotaDAO notaDAO = SQLiteFactory.getNotaDao(getApplicationContext());
        Libreta libreta = notaDAO.getLibreta(nota.getId());

        Txlibreta.setText(libreta.getTitulo());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tercera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_Editar) {
            Intent intent = new Intent(this, SegundaActivity.class);
            intent.putExtra("nota", nota);
            intent.putExtra("tipo", "editable");
            startActivity(intent);

        } else if (id == R.id.action_Eliminar) {
            FactoryDAO SQLiteFactory = FactoryDAO.getFactory(FactoryDAO.SQLITE_FACTORY);
            INotaDAO notaDAO = SQLiteFactory.getNotaDao(getApplicationContext());

            notaDAO.deleteNota(nota.getId()); // Eliminar nota por id

            Toast.makeText(this, "Nota eliminada", Toast.LENGTH_SHORT).show();
        }

        finish();

        return super.onOptionsItemSelected(item);
    }
}
