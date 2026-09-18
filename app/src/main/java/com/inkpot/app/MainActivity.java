package com.inkpot.app;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;

import com.inkpot.app.UI.DialogoNombre;
import com.inkpot.app.UI.ListEtiquetasFragment;
import com.inkpot.app.UI.ListLibretasFragment;
import com.inkpot.app.UI.ListNotasFragment;
import com.inkpot.app.UI.ListPapeleraFragment;
import com.inkpot.app.ajustes.AjustesActivity;
import com.inkpot.app.data.Etiqueta;
import com.inkpot.app.data.Libreta;
import com.inkpot.app.data.NotasRepository;
import com.inkpot.app.data.Nota;
import com.inkpot.app.databinding.ActivityMainBinding;
import com.inkpot.app.seguridad.BloqueoActivity;
import com.inkpot.app.seguridad.GestorPin;
import com.inkpot.app.util.Markdown;
import com.inkpot.app.util.Respaldo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla principal de la aplicación.
 *
 * <p>Contiene el menú lateral, la barra de herramientas y el contenedor donde se
 * van sustituyendo los fragmentos de listado (notas, libretas y etiquetas). El
 * botón flotante crea una nota o una libreta según la pantalla activa.</p>
 */
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private FloatingActionButton fab;

    /** Abre el selector de fichero para importar una nota desde Markdown. */
    private final ActivityResultLauncher<String[]> importarLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        importarMarkdown(uri);
                    }
                }
            });

    /** Pide dónde guardar la copia de seguridad de todas las notas. */
    private final ActivityResultLauncher<String> exportarTodoLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/zip"),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        exportarTodo(uri);
                    }
                }
            });

    /** Abre el selector del ZIP de copia de seguridad para importarlo. */
    private final ActivityResultLauncher<String[]> importarTodoLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        importarTodo(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Si hay un PIN configurado y no se ha desbloqueado todavía, se pide.
        if (GestorPin.hayPin(this) && !GestorPin.estaDesbloqueado()) {
            startActivity(new Intent(this, BloqueoActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (manejarAtras()) {
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        });

        createComponents(savedInstanceState);
        eventRecorder();
    }

    public void createComponents(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, ListNotasFragment.newInstance()).commit();
        }

        // Configuracion de los menus (drawer y toolbar)
        toolbar = binding.appBar.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.todas_las_notas);

        drawer = binding.drawerLayout;
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close); // Integrar el menu drawer con el toolbar mediante el icono "hamburguesa"
        toggle.syncState();

        navigationView = binding.navView;

        fab = binding.appBar.fab;  // Boton flotante para crear nueva nota o una nueva libreta
        fab.setContentDescription(getString(R.string.nueva_nota));
    }

    public void eventRecorder() {
        drawer.addDrawerListener(toggle);
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
                int capa = newState == DrawerLayout.STATE_IDLE ? View.LAYER_TYPE_NONE : View.LAYER_TYPE_HARDWARE;
                navigationView.setLayerType(capa, null);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;
                Boolean fragmentSelected = false;

                if (menuItem.getItemId() == R.id.allNotas) {
                    fragment = ListNotasFragment.newInstance();
                    fragmentSelected = true;
                } else if (menuItem.getItemId() == R.id.allLibretas) {
                    fragment = new ListLibretasFragment();
                    fragmentSelected = true;
                } else if (menuItem.getItemId() == R.id.allEtiquetas) {
                    fragment = new ListEtiquetasFragment();
                    fragmentSelected = true;
                } else if (menuItem.getItemId() == R.id.allPapelera) {
                    fragment = new ListPapeleraFragment();
                    fragmentSelected = true;
                } else if (menuItem.getItemId() == R.id.allAjustes) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    startActivity(new Intent(MainActivity.this, AjustesActivity.class));
                    return true;
                }

                if (fragmentSelected) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit(); // Anniadir fragment select a la pila de fragmentos

                    actualizarDescripcionFab(menuItem.getItemId());

                    DrawerLayout drawer = binding.drawerLayout;
                    drawer.closeDrawer(GravityCompat.START); // Cerrar la pestaña al presionar

                    return true;
                }
                return false;
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

                if (currentFragment instanceof ListNotasFragment) { // Nueva nota
                    Libreta libreta = null;
                    Intent intent = new Intent(MainActivity.this, EditNotaActivity.class);
                    intent.putExtra("tipo", "nueva");

                    // Si se esta dentro del listado de notas de una libreta
                    if((libreta = ((ListNotasFragment) currentFragment).getLibreta()) != null) {
                        intent.putExtra("libretaPadre", libreta);
                    }

                    startActivity(intent);
                } else if (currentFragment instanceof  ListLibretasFragment) { // Nueva libreta
                    final ListLibretasFragment listLibretasFragment = (ListLibretasFragment) currentFragment;
                    DialogoNombre.mostrar(MainActivity.this, getString(R.string.nueva_libreta), "",
                            new DialogoNombre.OnNombreAceptado() {
                                @Override
                                public void onNombre(String nombre) {
                                    NotasRepository.get(MainActivity.this).crearLibretaSiNoExiste(nombre,
                                            new NotasRepository.Callback<Boolean>() {
                                                @Override
                                                public void onResult(Boolean creada) {
                                                    if (!creada) {
                                                        Toast.makeText(MainActivity.this, R.string.libreta_duplicada, Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }
                                                    listLibretasFragment.resetListaLibretas();
                                                    Toast.makeText(MainActivity.this, R.string.libreta_guardada, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });

                } else if (currentFragment instanceof  ListEtiquetasFragment) {
                    final ListEtiquetasFragment listEtiquetasFragment = (ListEtiquetasFragment) currentFragment;
                    DialogoNombre.mostrar(MainActivity.this, getString(R.string.nueva_etiqueta), "",
                            new DialogoNombre.OnNombreAceptado() {
                                @Override
                                public void onNombre(String nombre) {
                                    NotasRepository.get(MainActivity.this).crearEtiquetaSiNoExiste(nombre,
                                            new NotasRepository.Callback<Boolean>() {
                                                @Override
                                                public void onResult(Boolean creada) {
                                                    if (!creada) {
                                                        Toast.makeText(MainActivity.this, R.string.etiqueta_duplicada, Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }
                                                    listEtiquetasFragment.resetListaEtiquetas();
                                                    Toast.makeText(MainActivity.this, R.string.etiqueta_guardada, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                }
            }
        });
    }

    // Gestiona el botón atrás: cierra el menú, retrocede entre listados o deja salir de la app.
    // Devuelve true si ha consumido el evento.
    private boolean manejarAtras() {
        DrawerLayout drawer = binding.drawerLayout;
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
        NavigationView navigationView = binding.navView;

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

        if (currentFragment instanceof ListNotasFragment) {
            if (((ListNotasFragment) currentFragment).getLibreta() != null) { // Notas de una libreta
                fragmentManager.beginTransaction().replace(R.id.fragmentContainer, new ListLibretasFragment()).commit();
                navigationView.setCheckedItem(R.id.allLibretas);
                actualizarDescripcionFab(R.id.allLibretas);
                getSupportActionBar().setTitle(R.string.libretas);
                return true;
            } else if (((ListNotasFragment) currentFragment).getEtiqueta() != null) { // Notas de una etiqueta
                fragmentManager.beginTransaction().replace(R.id.fragmentContainer, new ListEtiquetasFragment()).commit();
                navigationView.setCheckedItem(R.id.allEtiquetas);
                actualizarDescripcionFab(R.id.allEtiquetas);
                getSupportActionBar().setTitle(R.string.etiquetas);
                return true;
            }
        }

        if (currentFragment instanceof ListLibretasFragment || currentFragment instanceof ListEtiquetasFragment
                || currentFragment instanceof ListPapeleraFragment) {
            fragmentManager.beginTransaction().replace(R.id.fragmentContainer, ListNotasFragment.newInstance()).commit();
            navigationView.setCheckedItem(R.id.allNotas);
            actualizarDescripcionFab(R.id.allNotas);
            getSupportActionBar().setTitle(R.string.todas_las_notas);
            return true;
        }

        return false;
    }

    // OPCIONES MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        if (searchManager != null && searchItem != null && searchItem.getActionView() instanceof SearchView) {
            ((SearchView) searchItem.getActionView()).setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean hayPin = GestorPin.hayPin(this);
        menu.findItem(R.id.action_quitar_pin).setVisible(hayPin);
        menu.findItem(R.id.action_bloquear).setVisible(hayPin);
        menu.findItem(R.id.action_establecer_pin).setTitle(hayPin ? R.string.cambiar_pin : R.string.establecer_pin);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_importar) {
            importarLauncher.launch(new String[]{"text/*", "text/markdown", "text/plain"});
        } else if (id == R.id.action_exportar_todo) {
            exportarTodoLauncher.launch("nevernote.zip");
        } else if (id == R.id.action_importar_todo) {
            importarTodoLauncher.launch(new String[]{"application/zip", "application/octet-stream"});
        } else if (id == R.id.action_establecer_pin) {
            pedirNuevoPin();
        } else if (id == R.id.action_quitar_pin) {
            pedirPinParaQuitar();
        } else if (id == R.id.action_bloquear) {
            GestorPin.bloquear();
            startActivity(new Intent(this, BloqueoActivity.class));
            finish();
        } else if (id == R.id.action_salir) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /** Pide un PIN nuevo y lo guarda. */
    private void pedirNuevoPin() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final EditText entrada = new EditText(this);
        entrada.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        dialog.setTitle(R.string.pin_nuevo);
        dialog.setView(entrada);
        dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                String pin = entrada.getText().toString();
                if (pin.length() < 4) {
                    Toast.makeText(MainActivity.this, R.string.pin_corto, Toast.LENGTH_SHORT).show();
                    return;
                }
                GestorPin.guardarPin(MainActivity.this, pin);
                invalidateOptionsMenu();
                Toast.makeText(MainActivity.this, R.string.pin_guardado, Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setNegativeButton(R.string.cancelar, null);
        dialog.create().show();
    }

    /** Pide el PIN actual y, si es correcto, lo quita. */
    private void pedirPinParaQuitar() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final EditText entrada = new EditText(this);
        entrada.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        dialog.setTitle(R.string.pin_actual);
        dialog.setView(entrada);
        dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                if (GestorPin.comprobar(MainActivity.this, entrada.getText().toString())) {
                    GestorPin.quitarPin(MainActivity.this);
                    invalidateOptionsMenu();
                    Toast.makeText(MainActivity.this, R.string.pin_quitado, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, R.string.pin_incorrecto, Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.setNegativeButton(R.string.cancelar, null);
        dialog.create().show();
    }

    /** Lee el fichero elegido, lo interpreta como nota y la guarda en 'Default'. */
    private void importarMarkdown(Uri uri) {
        try {
            String contenido = leerTexto(uri);
            Markdown.NotaMarkdown importada = Markdown.importar(contenido, nombreFichero(uri));

            NotasRepository.get(this).crearNota(importada.titulo, importada.texto, 1,
                    new ArrayList<Etiqueta>(), 0, new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, R.string.nota_importada, Toast.LENGTH_SHORT).show();
                            recargarListadoActual();
                        }
                    });
        } catch (IOException e) {
            Toast.makeText(this, R.string.error_importar, Toast.LENGTH_SHORT).show();
        }
    }

    /** Escribe todas las notas en un ZIP de copia de seguridad. */
    private void exportarTodo(final Uri uri) {
        NotasRepository.get(this).notasTodas(new NotasRepository.Callback<List<Nota>>() {
            @Override
            public void onResult(List<Nota> notas) {
                try (OutputStream salida = getContentResolver().openOutputStream(uri)) {
                    if (salida == null) {
                        throw new IOException("No se pudo abrir el fichero");
                    }
                    Respaldo.exportar(notas, salida);
                    Toast.makeText(MainActivity.this, R.string.respaldo_exportado, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, R.string.error_respaldo, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /** Lee un ZIP de copia de seguridad y crea las notas que contiene. */
    private void importarTodo(Uri uri) {
        try (InputStream entrada = getContentResolver().openInputStream(uri)) {
            if (entrada == null) {
                throw new IOException("No se pudo abrir el fichero");
            }
            List<Markdown.NotaMarkdown> notas = Respaldo.importar(entrada);
            NotasRepository.get(this).crearNotas(notas, new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, R.string.respaldo_importado, Toast.LENGTH_SHORT).show();
                    recargarListadoActual();
                }
            });
        } catch (IOException e) {
            Toast.makeText(this, R.string.error_respaldo, Toast.LENGTH_SHORT).show();
        }
    }

    /** Ajusta la descripción del botón flotante a la sección actual. */
    private void actualizarDescripcionFab(int idMenu) {
        int descripcion;
        if (idMenu == R.id.allLibretas) {
            descripcion = R.string.nueva_libreta;
        } else if (idMenu == R.id.allEtiquetas) {
            descripcion = R.string.nueva_etiqueta;
        } else {
            descripcion = R.string.nueva_nota;
        }
        fab.setContentDescription(getString(descripcion));
    }

    private void recargarListadoActual() {        Fragment actual = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (actual instanceof ListNotasFragment) {
            ((ListNotasFragment) actual).recargar();
        } else if (actual instanceof ListLibretasFragment) {
            ((ListLibretasFragment) actual).recargar();
        } else if (actual instanceof ListEtiquetasFragment) {
            ((ListEtiquetasFragment) actual).recargar();
        }
    }

    private String leerTexto(Uri uri) throws IOException {
        try (InputStream entrada = getContentResolver().openInputStream(uri)) {
            if (entrada == null) {
                throw new IOException("No se pudo abrir el fichero");
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] datos = new byte[4096];
            int leidos;
            while ((leidos = entrada.read(datos)) != -1) {
                buffer.write(datos, 0, leidos);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }
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
        return nombre == null ? "nota.md" : nombre;
    }
}