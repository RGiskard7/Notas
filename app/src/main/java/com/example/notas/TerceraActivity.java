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
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notas.data.Etiqueta;
import com.example.notas.data.FactoryDAO;
import com.example.notas.data.INotaDAO;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TerceraActivity extends AppCompatActivity {
    private TextView titulo;
    private TextView texto;
    private TextView fecha;
    private TextView txlibreta;
    private TextView numEtiquetas;
    // private LinearLayout linLayoutCheckBoxes;
    private Nota nota;
    private Libreta libreta;
    private List<Etiqueta> currentEtiquetasNota;
    private ImageButton buttonEtiquetas;
    //List<CheckBox> checkBoxes;
    private FactoryDAO SQLiteFactory;
    private INotaDAO notaDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tercera);

        // Conexion con el proveedor de datos a través del DAO
        SQLiteFactory = FactoryDAO.getFactory(FactoryDAO.SQLITE_FACTORY);
        notaDAO = SQLiteFactory.getNotaDao(getApplicationContext());

        nota = (Nota) getIntent().getSerializableExtra("nota");
        libreta = nota.getLibreta();

        /*checkBoxes = new ArrayList<>();
        notaDAO.getAllCheckBoxsFrom(TerceraActivity.this, nota.getId(), checkBoxes);*/

        createComponents();
        fillComponents();
        eventRecorder();
    }

    public void createComponents() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Nota");

        titulo = (TextView) findViewById(R.id.textViewTituloNota);
        texto = (TextView) findViewById(R.id.textViewTextoNota);
        texto.setMovementMethod(new ScrollingMovementMethod());
        fecha = (TextView) findViewById(R.id.textViewFechaNota);
        txlibreta = (TextView) findViewById(R.id.textViewLibretaNota);
        numEtiquetas = (TextView) findViewById(R.id.textView3);
        buttonEtiquetas = (ImageButton) findViewById(R.id.buttonEtiquetas);

        //linLayoutCheckBoxes = findViewById(R.id.linLayoutCheckBox2);

        txlibreta.setText(libreta.getTitulo());
    }

    public void fillComponents() {
        titulo.setText(nota.getTitulo());
        titulo.setTextIsSelectable(true);
        texto.setText(nota.getTexto());
        texto.setTextIsSelectable(true);
        fecha.setText(nota.getFechaCreacion());
        txlibreta.setText(libreta.getTitulo());

        currentEtiquetasNota = new ArrayList<>();
        notaDAO.getAllEtiquetasFrom(nota.getId(), currentEtiquetasNota);

        numEtiquetas.setText(Integer.toString(currentEtiquetasNota.size()));

        /*for (CheckBox ch : checkBoxes) {
            linLayoutCheckBoxes.addView(ch);
        }*/
    }

    public void eventRecorder() {
        buttonEtiquetas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!currentEtiquetasNota.isEmpty()) {
                    final String[] etiquetasName = new String[currentEtiquetasNota.size()];

                    for (int i = 0; i < currentEtiquetasNota.size(); i++) {
                        etiquetasName[i] = currentEtiquetasNota.get(i).getTitulo();
                    }

                    AlertDialog.Builder builderDialog = new AlertDialog.Builder(TerceraActivity.this);
                    builderDialog.setTitle("Etiquetas");
                    builderDialog.setItems(etiquetasName, null);
                    builderDialog.setPositiveButton("OK", null);
                    // builderDialog.setNegativeButton("CANCELAR", null);
                    AlertDialog dialog = builderDialog.create();
                    dialog.show();
                } else {
                    Toast.makeText(TerceraActivity.this, "No hay ninguna etiqueta asociada a esta nota", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
        } /*else if (id == R.id.action_export_txt) {
            try {
                File folder = new File(Environment.getExternalStorageDirectory(), "Gilinote");
                String nameFile = "Nota";
                File[] files;
                File file;

                if (!folder.exists()) {
                    folder.mkdirs();
                }

                files = folder.listFiles();
                if (files != null && files.length > 0) {
                    nameFile += files.length;
                }

                file = new File(folder , nameFile + ".txt");
                OutputStreamWriter stream = new OutputStreamWriter(new FileOutputStream(file));

                stream.write(fecha.getText() + "\n\n" + titulo.getText() + "\n\n" + txlibreta.getText() + "\n\n" + texto.getText());
                stream.flush();
                stream.close();

                Toast.makeText(getApplicationContext(), "Nota txt exportada en carpeta 'Gilinote'", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Error", "e: " + e);
                Toast.makeText(getApplicationContext(), "Error al exportar la nota", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.action_export_pdf) {
            File folder = new File(Environment.getExternalStorageDirectory(), "Gilinote");
            Document document = new Document(PageSize.A4);
            String nameFile = "Nota";
            File[] files;
            File pdfFile;

            if (!folder.exists()) {
                folder.mkdirs();
            }

            files = folder.listFiles();
            if (files != null && files.length > 0) {
                nameFile += files.length;
            }

            pdfFile = new File(folder, nameFile + ".pdf");

            try {
                PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                document.open();

                document.addTitle(titulo.getText().toString());
                document.addCreationDate();
                document.addAuthor("Gilinote");

                Paragraph paragraph = new Paragraph(fecha.getText().toString());
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph = new Paragraph(titulo.getText().toString(), new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLD));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph = new Paragraph(txlibreta.getText().toString(), new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph = new Paragraph(texto.getText().toString(), new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD));
                paragraph.setSpacingAfter(5);
                paragraph.setSpacingBefore(5);
                document.add(paragraph);

                document.close();
                Toast.makeText(getApplicationContext(), "Nota pdf exportada en carpeta 'Gilinote'", Toast.LENGTH_SHORT).show();
            } catch (DocumentException | FileNotFoundException e) {
                e.printStackTrace();
                Log.e("Error", e.toString());
                Toast.makeText(getApplicationContext(), "Error al exportar la nota", Toast.LENGTH_SHORT).show();
            }
        }*/
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

    @Override
    protected void onDestroy() {
        notaDAO.closeDB();

        super.onDestroy();
    }
}
