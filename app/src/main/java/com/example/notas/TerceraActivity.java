package com.example.notas;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notas.data.FactoryDAO;
import com.example.notas.data.INotaDAO;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class TerceraActivity extends AppCompatActivity {
    private TextView titulo;
    private TextView texto;
    private TextView fecha;
    private TextView txlibreta;
    private Nota nota;
    private Libreta libreta;
    private FactoryDAO SQLiteFactory;
    private INotaDAO notaDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tercera);

        SQLiteFactory = FactoryDAO.getFactory(FactoryDAO.SQLITE_FACTORY);
        notaDAO = SQLiteFactory.getNotaDao(getApplicationContext());

        nota = (Nota) getIntent().getSerializableExtra("nota");
        libreta = nota.getLibreta();

        createComponents();
        fillComponents();
    }

    public void createComponents() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titulo = (TextView) findViewById(R.id.textViewTituloNota);
        texto = (TextView) findViewById(R.id.textViewTextoNota);
        texto.setMovementMethod(new ScrollingMovementMethod());
        fecha = (TextView) findViewById(R.id.textViewFechaNota);
        txlibreta = (TextView) findViewById(R.id.textViewLibretaNota);

        titulo.setText(nota.getTitulo());
        texto.setText(nota.getTexto());
        fecha.setText(nota.getFechaCreacion());

        txlibreta.setText(libreta.getTitulo());
    }

    public void fillComponents() {
        titulo.setText(nota.getTitulo());
        titulo.setTextIsSelectable(true);
        texto.setText(nota.getTexto());
        texto.setTextIsSelectable(true);
        fecha.setText(nota.getFechaCreacion());
        txlibreta.setText(libreta.getTitulo());
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
            startActivityForResult(intent, 1);

        } else if (id == R.id.action_Eliminar) {
            AlertDialog.Builder builder = new AlertDialog.Builder(TerceraActivity.this);
            builder.setMessage(R.string.messageAlertDialog).setTitle(R.string.titleAlertDialog);
            builder.setPositiveButton(R.string.positiveBtnAlertDialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    notaDAO.deleteNota(nota.getId()); // Eliminar nota por id
                    Toast.makeText(getApplicationContext(), "Nota eliminada", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            builder.setNegativeButton(R.string.negativeBtnAlertDIalog, null);
            builder.create().show();

        } else if (id == R.id.action_export_txt) {
            try {
                File newFolder = new File(Environment.getExternalStorageDirectory(), "Gilinote");
                if (!newFolder.exists()) {
                    newFolder.mkdirs();
                }

                File[] files = newFolder.listFiles();
                String nameFile = "Nota";

                if (files != null) {
                    nameFile += files.length;
                }

                File file = new File(newFolder , nameFile + ".txt");
                OutputStreamWriter stream = new OutputStreamWriter(new FileOutputStream(file));

                stream.write(fecha.getText() + "\n\n" + titulo.getText() + "\n\n" + txlibreta.getText() + "\n\n" + texto.getText());
                stream.flush();
                stream.close();

                Toast.makeText(getApplicationContext(), "Nota exportada en carpeta 'Gilinote'", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("Error", "e: " + e);
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error al exportar la nota", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // Se actualizan los datos de la nota en caso de que haya sido editada
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                nota = notaDAO.getNota(nota.getId());
                libreta = nota.getLibreta();
                fillComponents();
            }
        }
    }
}
