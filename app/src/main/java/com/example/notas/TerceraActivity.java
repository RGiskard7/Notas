package com.example.notas;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class TerceraActivity extends AppCompatActivity {
    private TextView titulo;
    private TextView texto;
    private TextView fecha;
    private TextView Txlibreta;
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
        // libreta = notaDAO.getLibreta(nota.getId());
        libreta = nota.getLibreta();

        // loadData();
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
        Txlibreta = (TextView) findViewById(R.id.textViewLibretaNota);

        titulo.setText(nota.getTitulo());
        texto.setText(nota.getTexto());
        fecha.setText(nota.getFechaCreacion());

        Txlibreta.setText(libreta.getTitulo());
    }

    /*public void loadData() {
        int nota_id = (Nota) getIntent().getSerializableExtra("nota");
        nota = notaDAO.getNota(nota_id);
        // libreta = notaDAO.getLibreta(nota.getId());
        libreta = nota.getLibreta();
    }*/

    public void fillComponents() {
        titulo.setText(nota.getTitulo());
        texto.setText(nota.getTexto());
        fecha.setText(nota.getFechaCreacion());
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

        } else if (id == R.id.action_export_pdf) {
            /*String nomArchivo = "Nota";
            Document documento;
            File archivo = new File("/" + nomArchivo + ".pdf");
            int i = 0;
            while(archivo.exists()) {
                i++;
                nomArchivo += i;
            }
            try {
                FileOutputStream streamPdf = new FileOutputStream(archivo.getAbsolutePath());
                PdfWriter writer = PdfWriter.getInstance(documento, ficheroPdf);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }*/
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
