package com.example.notas;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;

import com.example.notas.UI.ListEtiquetasFragment;
import com.example.notas.UI.ListLibretasFragment;
import com.example.notas.UI.ListNotasFragment;
import com.example.notas.UI.ListPapeleraFragment;
import com.example.notas.data.Etiqueta;
import com.example.notas.data.Libreta;
import com.example.notas.data.NotasRepository;
import com.example.notas.databinding.ActivityMainBinding;
import com.example.notas.util.Markdown;
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
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    public void eventRecorder() {
        drawer.addDrawerListener(toggle);

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
                }

                if (fragmentSelected) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit(); // Anniadir fragment select a la pila de fragmentos

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
                    Intent intent = new Intent(MainActivity.this, EditLibretaActivity.class);
                    intent.putExtra("tipo", "nueva");
                    startActivity(intent);

                } else if (currentFragment instanceof  ListEtiquetasFragment) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    final EditText input = new EditText(MainActivity.this);
                    final ListEtiquetasFragment listEtiquetasFragment = (ListEtiquetasFragment) currentFragment;

                    input.setInputType(InputType.TYPE_CLASS_TEXT);

                    dialog.setTitle(R.string.nueva_etiqueta);
                    dialog.setView(input);

                    dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String nombre = input.getText().toString();
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
                    dialog.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    dialog.create().show();
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
                getSupportActionBar().setTitle(R.string.libretas);
                return true;
            } else if (((ListNotasFragment) currentFragment).getEtiqueta() != null) { // Notas de una etiqueta
                fragmentManager.beginTransaction().replace(R.id.fragmentContainer, new ListEtiquetasFragment()).commit();
                navigationView.setCheckedItem(R.id.allEtiquetas);
                getSupportActionBar().setTitle(R.string.etiquetas);
                return true;
            }
        }

        if (currentFragment instanceof ListLibretasFragment || currentFragment instanceof ListEtiquetasFragment
                || currentFragment instanceof ListPapeleraFragment) {
            fragmentManager.beginTransaction().replace(R.id.fragmentContainer, ListNotasFragment.newInstance()).commit();
            navigationView.setCheckedItem(R.id.allNotas);
            getSupportActionBar().setTitle(R.string.todas_las_notas);
            return true;
        }

        return false;
    }

    // OPCIONES MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_importar) {
            importarLauncher.launch(new String[]{"text/*", "text/markdown", "text/plain"});
        } else if (id == R.id.action_salir) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /** Lee el fichero elegido, lo interpreta como nota y la guarda en 'Default'. */
    private void importarMarkdown(Uri uri) {
        try {
            String contenido = leerTexto(uri);
            Markdown.NotaMarkdown importada = Markdown.importar(contenido, nombreFichero(uri));

            NotasRepository.get(this).crearNota(importada.titulo, importada.texto, 1,
                    new ArrayList<Etiqueta>(), new Runnable() {
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

    private void recargarListadoActual() {
        Fragment actual = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
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