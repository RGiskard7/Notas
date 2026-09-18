package com.inkpot.app;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.inkpot.app.UI.DialogoColor;
import com.inkpot.app.UI.EditNotaViewModel;
import com.inkpot.app.data.Adjunto;
import com.inkpot.app.data.Etiqueta;
import com.inkpot.app.data.Libreta;
import com.inkpot.app.data.Nota;
import com.inkpot.app.data.NotasRepository;
import com.inkpot.app.databinding.ActivityEditNotaBinding;
import com.inkpot.app.util.Adjuntos;
import com.inkpot.app.util.EtiquetaSelection;
import com.inkpot.app.util.FormatoNota;
import com.inkpot.app.util.PaletaNotas;
import com.inkpot.app.util.Vinietas;
import com.google.android.material.chip.Chip;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pantalla para crear o editar una nota.
 *
 * <p>Permite escribir el título y el texto, elegir la libreta y marcar las
 * etiquetas. También ofrece insertar viñetas desde la barra inferior.</p>
 */
public class EditNotaActivity extends AppCompatActivity {
    private ActivityEditNotaBinding binding;
    private Nota nota;
    private Libreta libreta;
    private Libreta oldLibreta;
    private EditText titulo;
    private EditText texto;
    private Chip chipLibreta;
    private Chip chipEtiquetas;
    private boolean editando = false;
    private List<Libreta> allLibretas;
    private Set<Etiqueta> currentEtiquetasNota;
    private Set<Etiqueta> originalEtiquetasNota;
    private List<Etiqueta> allEtiquetas;
    private EditNotaViewModel viewModel;
    private LinearLayout contenedorAdjuntos;
    private int colorNota = 0;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNotaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                actualizarLibreta(libretas);
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
                actualizarEtiquetasChip();
            }
        });
        viewModel.getAdjuntos().observe(this, new Observer<List<Adjunto>>() {
            @Override
            public void onChanged(List<Adjunto> adjuntos) {
                mostrarAdjuntos(adjuntos);
            }
        });

        loadData();
    }

    public void loadData() {
        viewModel.cargarLibretas();
        viewModel.cargarEtiquetas();
        if (editando) {
            viewModel.cargarEtiquetasDeNota(nota.getId());
            viewModel.cargarAdjuntos(nota.getId());
        }
    }

    public void createComponents() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titulo = binding.editTextTituloNwNota;
        texto = binding.editTextContenidoNwNota;
        chipLibreta = binding.chipLibreta;
        chipEtiquetas = binding.chipEtiquetas;
        contenedorAdjuntos = binding.contenedorAdjuntos;

        binding.scrollAdjuntos.setVisibility(editando ? View.VISIBLE : View.GONE);
        binding.buttonAnadirAdjunto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adjuntarLauncher.launch(new String[]{"image/*"});
            }
        });

        fillComponents();
    }

    public void fillComponents() {
        if (editando) {
            getSupportActionBar().setTitle(R.string.editar_nota);
            titulo.setText(nota.getTitulo());
            texto.setText(nota.getTexto());
            colorNota = nota.getColor();
            aplicarColor();
        } else {
            getSupportActionBar().setTitle(R.string.nueva_nota);
        }
        actualizarEtiquetasChip();
    }

    /** Aplica el color elegido como fondo del área de edición. */
    private void aplicarColor() {
        if (colorNota != 0) {
            binding.contenidoEditor.setBackgroundColor(PaletaNotas.color(this, colorNota));
        } else {
            binding.contenidoEditor.setBackground(null);
        }
    }

    /** Actualiza el chip que muestra el número de etiquetas de la nota. */
    private void actualizarEtiquetasChip() {
        chipEtiquetas.setText(getString(R.string.etiquetas) + " (" + currentEtiquetasNota.size() + ")");
        chipEtiquetas.setContentDescription(getString(R.string.etiquetas) + ": " + currentEtiquetasNota.size());
    }

    private void actualizarLibreta(List<Libreta> libretas) {
        allLibretas.clear();
        allLibretas.addAll(libretas);

        int indice = -1;
        if (editando && oldLibreta != null) {
            indice = indiceDe(oldLibreta.getId());
        } else if (!editando && getIntent().getExtras() != null && getIntent().getExtras().containsKey("libretaPadre")) {
            Libreta padre = (Libreta) getIntent().getExtras().get("libretaPadre");
            indice = indiceDe(padre.getId());
        }
        if (indice < 0 && !allLibretas.isEmpty()) {
            indice = 0;
        }
        if (indice >= 0) {
            seleccionarLibreta(indice);
        }
    }

    /** Fija la libreta seleccionada y lo refleja en el chip. */
    void seleccionarLibreta(int indice) {
        libreta = allLibretas.get(indice);
        chipLibreta.setText(libreta.getTitulo());
        chipLibreta.setContentDescription(getString(R.string.libreta) + ": " + libreta.getTitulo());
    }

    private int indiceDe(int idLibreta) {
        for (int i = 0; i < allLibretas.size(); i++) {
            if (allLibretas.get(i).getId() == idLibreta) {
                return i;
            }
        }
        return -1;
    }

    /** Dibuja las miniaturas de los adjuntos de la nota, con su botón de quitar. */
    private void mostrarAdjuntos(List<Adjunto> adjuntos) {
        contenedorAdjuntos.removeAllViews();
        int lado = (int) (96 * getResources().getDisplayMetrics().density);
        int separacion = (int) (8 * getResources().getDisplayMetrics().density);

        for (final Adjunto adjunto : adjuntos) {
            View vista = getLayoutInflater().inflate(R.layout.adjunto_item, contenedorAdjuntos, false);
            ImageView imagen = vista.findViewById(R.id.imageViewAdjunto);
            ImageButton quitar = vista.findViewById(R.id.buttonQuitarAdjunto);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(lado, lado);
            imagen.setLayoutParams(params);
            imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);

            ViewGroup.LayoutParams raiz = vista.getLayoutParams();
            raiz.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            raiz.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            if (raiz instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) raiz).setMarginEnd(separacion);
            }
            vista.setLayoutParams(raiz);

            File fichero = Adjuntos.fichero(this, adjunto.getRuta());
            if (fichero.exists()) {
                imagen.setImageBitmap(BitmapFactory.decodeFile(fichero.getAbsolutePath()));
            }
            quitar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmarEliminarAdjunto(adjunto);
                }
            });
            contenedorAdjuntos.addView(vista);
        }
    }

    /** Copia la imagen elegida y la asocia a la nota. */
    private void adjuntarImagen(Uri uri) {
        viewModel.agregarAdjunto(nota.getId(), uri, Adjuntos.nombreFichero(this, uri), Adjuntos.mime(this, uri),
                new NotasRepository.Callback<Boolean>() {
                    @Override
                    public void onResult(Boolean anadido) {
                        int mensaje = Boolean.TRUE.equals(anadido) ? R.string.adjunto_anadido : R.string.error_adjunto;
                        Toast.makeText(EditNotaActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmarEliminarAdjunto(final Adjunto adjunto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.messageAlertDialogAdjunto).setTitle(R.string.titleAlertDialog);
        builder.setPositiveButton(R.string.positiveBtnAlertDialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewModel.eliminarAdjunto(adjunto, new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(EditNotaActivity.this, R.string.adjunto_eliminado, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.negativeBtnAlertDIalog, null);
        builder.create().show();
    }

    public void eventRecorder() {
        chipLibreta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                elegirLibreta();
            }
        });

        chipEtiquetas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                elegirEtiquetas();
            }
        });

        binding.buttonVinietas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertarVinieta();
            }
        });
        binding.buttonCasilla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertarAlInicioDeLinea(FormatoNota.MARCA_TAREA);
            }
        });
        binding.buttonListaNumerada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertarAlInicioDeLinea("1. ");
            }
        });
        binding.buttonNegrita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                envolverSeleccion("**");
            }
        });
        binding.buttonCursiva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                envolverSeleccion("*");
            }
        });
        binding.buttonTachado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                envolverSeleccion("~~");
            }
        });
        binding.buttonCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                envolverSeleccion("`");
            }
        });
        binding.buttonEnlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertarEnlace();
            }
        });
        binding.buttonEncabezado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertarAlInicioDeLinea("## ");
            }
        });
        binding.buttonCita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertarAlInicioDeLinea("> ");
            }
        });
        binding.buttonSeparador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertarSeparador();
            }
        });
    }

    /** Muestra la lista de libretas para elegir una. */
    private void elegirLibreta() {
        if (allLibretas.isEmpty()) {
            return;
        }

        final String[] nombres = new String[allLibretas.size()];
        int seleccionada = 0;
        for (int i = 0; i < allLibretas.size(); i++) {
            nombres[i] = allLibretas.get(i).getTitulo();
            if (libreta != null && allLibretas.get(i).getId() == libreta.getId()) {
                seleccionada = i;
            }
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.libreta);
        dialog.setSingleChoiceItems(nombres, seleccionada, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                seleccionarLibreta(which);
                d.dismiss();
            }
        });
        dialog.show();
    }

    /** Muestra el diálogo para marcar y desmarcar etiquetas. */
    private void elegirEtiquetas() {
        if (allEtiquetas.isEmpty()) {
            Toast.makeText(EditNotaActivity.this, R.string.no_etiquetas_disponibles, Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] etiquetasName = new String[allEtiquetas.size()];
        final boolean[] checkedEtiquetas = new boolean[allEtiquetas.size()];
        for (int i = 0; i < allEtiquetas.size(); i++) {
            etiquetasName[i] = allEtiquetas.get(i).getTitulo();
            checkedEtiquetas[i] = currentEtiquetasNota.contains(allEtiquetas.get(i));
        }

        AlertDialog.Builder builderDialog = new AlertDialog.Builder(this);
        builderDialog.setTitle(R.string.elige_etiquetas);
        builderDialog.setMultiChoiceItems(etiquetasName, checkedEtiquetas, null);
        builderDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EtiquetaSelection.Diff diff = EtiquetaSelection.calcular(currentEtiquetasNota, allEtiquetas, checkedEtiquetas);
                currentEtiquetasNota.removeAll(diff.quitadas);
                currentEtiquetasNota.addAll(diff.anadidas);
                actualizarEtiquetasChip();
            }
        });
        builderDialog.setNegativeButton(R.string.cancelar, null);
        builderDialog.create().show();
    }

    private void insertarVinieta() {
        Editable contenido = texto.getText();
        int cursor = texto.getSelectionStart();
        if (cursor < 0) {
            cursor = contenido.length();
        }

        if (cursor == 0) {
            // Sin texto antes del cursor: se añade al final.
            if (TextUtils.isEmpty(contenido)) {
                contenido.insert(contenido.length(), "\t\t\u2022 ");
            } else {
                contenido.insert(contenido.length(), "\n\t\t\u2022 ");
            }
        } else if (TextUtils.isEmpty(contenido)) {
            contenido.insert(cursor, "\t\t\u2022 ");
        } else if (Vinietas.necesitaSaltoDeLinea(contenido, cursor)) {
            contenido.insert(cursor, "\n\t\t\u2022 ");
        } else {
            contenido.insert(cursor, "\t\t\u2022 ");
        }
    }

    /** Inserta una marca al principio de la línea del cursor (casilla, lista, cita...). */
    private void insertarAlInicioDeLinea(String marca) {
        Editable contenido = texto.getText();
        int cursor = texto.getSelectionStart();
        if (cursor < 0) {
            cursor = contenido.length();
        }

        String original = contenido.toString();
        String nuevo = FormatoNota.insertarAlInicioDeLinea(original, cursor, marca);
        texto.setText(nuevo);
        texto.setSelection(cursor + (nuevo.length() - original.length()));
    }

    /** Convierte la selección en un enlace de Markdown. */
    private void insertarEnlace() {
        int inicio = texto.getSelectionStart();
        int fin = texto.getSelectionEnd();
        if (inicio < 0) {
            inicio = texto.getText().length();
        }
        if (fin < inicio) {
            fin = inicio;
        }

        Editable contenido = texto.getText();
        String seleccion = contenido.subSequence(inicio, fin).toString();
        String enlace = "[" + seleccion + "](url)";
        contenido.replace(inicio, fin, enlace);
        texto.setSelection(inicio + enlace.length());
    }

    /** Inserta una línea separadora. */
    private void insertarSeparador() {
        Editable contenido = texto.getText();
        int cursor = texto.getSelectionStart();
        if (cursor < 0) {
            cursor = contenido.length();
        }
        contenido.insert(cursor, "\n---\n");
        texto.setSelection(cursor + 5);
    }

    /** Rodea el texto seleccionado con la marca indicada (negrita o cursiva). */
    private void envolverSeleccion(String marca) {
        int inicio = texto.getSelectionStart();
        int fin = texto.getSelectionEnd();
        if (inicio < 0) {
            inicio = texto.getText().length();
        }
        if (fin < inicio) {
            fin = inicio;
        }

        Editable contenido = texto.getText();
        contenido.insert(fin, marca);
        contenido.insert(inicio, marca);
        if (fin == inicio) {
            texto.setSelection(inicio + marca.length());
        } else {
            texto.setSelection(fin + 2 * marca.length());
        }
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

        if (id == R.id.action_color) {
            DialogoColor.mostrar(this, colorNota, new DialogoColor.OnColorElegido() {
                @Override
                public void onColor(int indice) {
                    colorNota = indice;
                    aplicarColor();
                }
            });
            return true;
        }

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
                            anadidas, quitadas, colorNota, new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(EditNotaActivity.this, R.string.nota_editada, Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            });
                } else {
                    viewModel.crearNota(tituloTexto, textoContenido, libreta.getId(),
                            new ArrayList<>(currentEtiquetasNota), colorNota, new Runnable() {
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
