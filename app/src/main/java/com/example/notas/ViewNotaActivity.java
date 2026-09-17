package com.example.notas;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.notas.UI.RenderizadorNota;
import com.example.notas.UI.ViewNotaViewModel;
import com.example.notas.data.Adjunto;
import com.example.notas.data.Etiqueta;
import com.example.notas.data.Libreta;
import com.example.notas.data.Nota;
import com.example.notas.data.NotasRepository;
import com.example.notas.databinding.ActivityViewNotaBinding;
import com.example.notas.recordatorios.ProgramadorRecordatorios;
import com.example.notas.util.Adjuntos;
import com.example.notas.util.Fechas;
import com.example.notas.util.FormatoNota;
import com.example.notas.util.Markdown;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Pantalla que muestra una nota y permite editarla o eliminarla.
 *
 * <p>Presenta las fechas de creación y, si la nota se ha modificado, la de
 * modificación.</p>
 */
public class ViewNotaActivity extends AppCompatActivity {
    private ActivityViewNotaBinding binding;
    private TextView titulo;
    private TextView texto;
    private TextView fecha;
    private TextView fechaModificacion;
    private TextView textViewRecordatorio;
    private TextView txlibreta;
    private TextView numEtiquetas;
    private Nota nota;
    private Libreta libreta;
    private List<Etiqueta> currentEtiquetasNota;
    private ImageButton buttonEtiquetas;
    private LinearLayout contenedorAdjuntos;
    private ViewNotaViewModel viewModel;

    /** Abre el selector de imágenes para adjuntar una a la nota. */
    private final ActivityResultLauncher<String[]> adjuntarLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            new androidx.activity.result.ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        adjuntarImagen(uri);
                    }
                }
            });

    /** Abre el selector de fichero para exportar la nota a Markdown. */
    private final ActivityResultLauncher<String> exportarLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/markdown"),
            new androidx.activity.result.ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        exportarMarkdown(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewNotaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        nota = (Nota) getIntent().getSerializableExtra("nota");
        libreta = nota.getLibreta();

        createComponents();

        viewModel = new ViewModelProvider(this).get(ViewNotaViewModel.class);
        viewModel.getEtiquetasDeNota().observe(this, new Observer<List<Etiqueta>>() {
            @Override
            public void onChanged(List<Etiqueta> etiquetas) {
                currentEtiquetasNota = etiquetas;
                numEtiquetas.setText(Integer.toString(currentEtiquetasNota.size()));
            }
        });
        viewModel.getNota().observe(this, new Observer<Nota>() {
            @Override
            public void onChanged(Nota actualizada) {
                if (actualizada != null) {
                    nota = actualizada;
                    libreta = nota.getLibreta();
                    fillComponents();
                }
            }
        });
        viewModel.getAdjuntos().observe(this, new Observer<List<Adjunto>>() {
            @Override
            public void onChanged(List<Adjunto> adjuntos) {
                mostrarAdjuntos(adjuntos);
            }
        });

        fillComponents();
        viewModel.cargarEtiquetasDeNota(nota.getId());
        viewModel.cargarAdjuntos(nota.getId());
        eventRecorder();
    }

    public void createComponents() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.ver_nota);

        titulo = binding.textViewTituloNota;
        texto = binding.textViewTextoNota;
        texto.setMovementMethod(new ScrollingMovementMethod());
        fecha = binding.textViewFechaNota;
        fechaModificacion = binding.textViewFechaModificacion;
        textViewRecordatorio = binding.textViewRecordatorio;
        txlibreta = binding.textViewLibretaNota;
        numEtiquetas = binding.textView3;
        buttonEtiquetas = binding.buttonEtiquetas;
        currentEtiquetasNota = new ArrayList<>();
        contenedorAdjuntos = binding.contenedorAdjuntos;
    }

    public void fillComponents() {
        titulo.setText(nota.getTitulo());
        titulo.setTextIsSelectable(true);
        mostrarTexto();
        fecha.setText(getString(R.string.fecha_creacion, Fechas.formatearNota(nota.getFechaCreacion())));
        if (nota.getFechaModificacion() != nota.getFechaCreacion()) {
            fechaModificacion.setText(getString(R.string.fecha_modificacion, Fechas.formatearNota(nota.getFechaModificacion())));
            fechaModificacion.setVisibility(View.VISIBLE);
        } else {
            fechaModificacion.setVisibility(View.GONE);
        }
        if (libreta != null) {
            txlibreta.setText(libreta.getTitulo());
        }
        mostrarRecordatorio();
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

                    AlertDialog.Builder builderDialog = new AlertDialog.Builder(ViewNotaActivity.this);
                    builderDialog.setTitle(R.string.etiquetas);
                    builderDialog.setItems(etiquetasName, null);
                    builderDialog.setPositiveButton(R.string.ok, null);
                    AlertDialog dialog = builderDialog.create();
                    dialog.show();
                } else {
                    Toast.makeText(ViewNotaActivity.this, R.string.no_etiquetas_en_nota, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_nota, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_quitar_recordatorio).setVisible(nota.getRecordatorio() > 0);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_Editar) {
            Intent intent = new Intent(this, EditNotaActivity.class);
            intent.putExtra("nota", nota);
            intent.putExtra("tipo", "editable");
            startActivityForResult(intent, 1);
        } else if (id == R.id.action_adjuntar) {
            adjuntarLauncher.launch(new String[]{"image/*"});
        } else if (id == R.id.action_recordatorio) {
            elegirFechaHora();
        } else if (id == R.id.action_quitar_recordatorio) {
            quitarRecordatorio();
        } else if (id == R.id.action_exportar) {
            exportarLauncher.launch(Markdown.nombreFichero(nota.getTitulo()));
        } else if (id == R.id.action_Eliminar) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ViewNotaActivity.this);
            builder.setMessage(R.string.messageAlertDialog).setTitle(R.string.titleAlertDialog);
            builder.setPositiveButton(R.string.positiveBtnAlertDialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ProgramadorRecordatorios.cancelar(ViewNotaActivity.this, nota.getId());
                    viewModel.eliminar(nota.getId(), new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.nota_eliminada, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            });
            builder.setNegativeButton(R.string.negativeBtnAlertDIalog, null);
            builder.create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    // Se actualizan los datos de la nota en caso de que haya sido editada
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.cargarNota(nota.getId());
                viewModel.cargarEtiquetasDeNota(nota.getId());
            }
        }
    }

    /** Muestra el texto con casillas y formato, y permite marcar las tareas. */
    private void mostrarTexto() {
        texto.setMovementMethod(LinkMovementMethod.getInstance());
        texto.setText(RenderizadorNota.renderizar(nota.getTexto(), new RenderizadorNota.OnTareaPulsada() {
            @Override
            public void onTareaPulsada(int numeroLinea) {
                final String nuevo = FormatoNota.alternarTarea(nota.getTexto(), numeroLinea);
                viewModel.actualizarTexto(nota.getId(), nota.getTitulo(), nuevo, new Runnable() {
                    @Override
                    public void run() {
                        nota.setTexto(nuevo);
                        mostrarTexto();
                    }
                });
            }
        }));
    }

    /** Dibuja las imágenes adjuntas de la nota. */
    private void mostrarAdjuntos(List<Adjunto> adjuntos) {
        contenedorAdjuntos.removeAllViews();
        for (final Adjunto adjunto : adjuntos) {
            ImageView vista = new ImageView(this);
            vista.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            vista.setAdjustViewBounds(true);

            File fichero = Adjuntos.fichero(this, adjunto.getRuta());
            if (fichero.exists()) {
                vista.setImageBitmap(BitmapFactory.decodeFile(fichero.getAbsolutePath()));
            }

            vista.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    confirmarEliminarAdjunto(adjunto);
                    return true;
                }
            });
            contenedorAdjuntos.addView(vista);
        }
    }

    /** Copia la imagen elegida y la asocia a la nota. */
    private void adjuntarImagen(Uri uri) {
        viewModel.agregarAdjunto(nota.getId(), uri, nombreFichero(uri), mimeDe(uri),
                new NotasRepository.Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean anadido) {
                        if (anadido) {
                            Toast.makeText(ViewNotaActivity.this, R.string.adjunto_anadido, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ViewNotaActivity.this, R.string.error_adjunto, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void confirmarEliminarAdjunto(final Adjunto adjunto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.messageAlertDialogAdjunto).setTitle(R.string.titleAlertDialog);
        builder.setPositiveButton(R.string.positiveBtnAlertDialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewModel.eliminarAdjunto(adjunto, null);
            }
        });
        builder.setNegativeButton(R.string.negativeBtnAlertDIalog, null);
        builder.create().show();
    }

    private String nombreFichero(Uri uri) {
        String nombre = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columna = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (columna >= 0) {
                    nombre = cursor.getString(columna);
                }
            }
        }
        return nombre == null ? "imagen" : nombre;
    }

    private String mimeDe(Uri uri) {
        String tipo = getContentResolver().getType(uri);
        return tipo == null ? "image/*" : tipo;
    }

    /** Muestra u oculta la fecha del recordatorio. */
    private void mostrarRecordatorio() {
        if (nota.getRecordatorio() > 0) {
            textViewRecordatorio.setText(getString(R.string.recordatorio_label, Fechas.formatearNota(nota.getRecordatorio())));
            textViewRecordatorio.setVisibility(View.VISIBLE);
        } else {
            textViewRecordatorio.setVisibility(View.GONE);
        }
    }

    /** Pide fecha y hora para el recordatorio. */
    private void elegirFechaHora() {
        Calendar calendario = Calendar.getInstance();
        if (nota.getRecordatorio() > 0) {
            calendario.setTimeInMillis(nota.getRecordatorio());
        }

        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int anio, int mes, int dia) {
                elegirHora(anio, mes, dia);
            }
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void elegirHora(final int anio, final int mes, final int dia) {
        Calendar calendario = Calendar.getInstance();
        if (nota.getRecordatorio() > 0) {
            calendario.setTimeInMillis(nota.getRecordatorio());
        }

        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hora, int minuto) {
                Calendar elegido = Calendar.getInstance();
                elegido.set(anio, mes, dia, hora, minuto, 0);
                elegido.set(Calendar.MILLISECOND, 0);
                fijarRecordatorio(elegido.getTimeInMillis());
            }
        }, calendario.get(Calendar.HOUR_OF_DAY), calendario.get(Calendar.MINUTE), true).show();
    }

    private void fijarRecordatorio(final long cuando) {
        pedirPermisoNotificaciones();
        viewModel.ponerRecordatorio(nota.getId(), cuando, new Runnable() {
            @Override
            public void run() {
                nota.setRecordatorio(cuando);
                ProgramadorRecordatorios.programar(ViewNotaActivity.this, nota.getId(), cuando, nota.getTitulo());
                mostrarRecordatorio();
                invalidateOptionsMenu();
                Toast.makeText(ViewNotaActivity.this, R.string.recordatorio_fijado, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void quitarRecordatorio() {
        viewModel.ponerRecordatorio(nota.getId(), 0, new Runnable() {
            @Override
            public void run() {
                nota.setRecordatorio(0);
                ProgramadorRecordatorios.cancelar(ViewNotaActivity.this, nota.getId());
                mostrarRecordatorio();
                invalidateOptionsMenu();
                Toast.makeText(ViewNotaActivity.this, R.string.recordatorio_quitado, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Pide el permiso de notificaciones en Android 13 o superior. */
    private void pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 226);
        }
    }

    /** Escribe la nota en Markdown en el fichero elegido por el usuario. */
    private void exportarMarkdown(Uri uri) {
        try (OutputStream salida = getContentResolver().openOutputStream(uri)) {
            if (salida == null) {
                throw new IOException("No se pudo abrir el fichero");
            }
            salida.write(Markdown.exportar(nota.getTitulo(), nota.getTexto()).getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, R.string.nota_exportada, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, R.string.error_exportar, Toast.LENGTH_SHORT).show();
        }
    }
}
