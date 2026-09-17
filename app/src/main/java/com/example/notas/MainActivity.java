package com.example.notas;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.notas.UI.ListEtiquetasFragment;
import com.example.notas.UI.ListLibretasFragment;
import com.example.notas.UI.ListNotasFragment;
import com.example.notas.data.Libreta;
import com.example.notas.data.NotasRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.OnBackPressedCallback;
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

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.todas_las_notas);

        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close); // Integrar el menu drawer con el toolbar mediante el icono "hamburguesa"
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);

        fab = findViewById(R.id.fab);  // Boton flotante para crear nueva nota o una nueva libreta
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
                }

                if (fragmentSelected) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit(); // Anniadir fragment select a la pila de fragmentos

                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

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

        if (currentFragment instanceof ListLibretasFragment || currentFragment instanceof ListEtiquetasFragment) {
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

        if (id == R.id.action_salir) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}