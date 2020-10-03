package com.example.notas;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.notas.UI.ListEtiquetasFragment;
import com.example.notas.UI.ListLibretasFragment;
import com.example.notas.UI.ListNotasFragment;
import com.example.notas.data.FactoryDAO;
import com.example.notas.data.IEtiquetaDAO;
import com.example.notas.data.ILibretaDAO;
import com.example.notas.data.Libreta;
import com.example.notas.data.LibretaDAOSQLite;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.InputType;
import android.util.Log;
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

        try {
            //Verificamos si tenemos los permisos necesarios escribir en tarjeta SD
            int permisoEscritura = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permisoEscritura != PackageManager.PERMISSION_GRANTED) {
                // Toast.makeText(getApplicationContext(), "No tiene permiso para acceder a EXTERNAL_STORAGE", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 225);
            } else {
                Log.i("Mensaje", "Se tiene permiso para acceso a EXTERNAL_STORAGE");
            }

        } catch (Exception e){
            Toast.makeText(getApplicationContext(), "No podrá acceder a EXTERNAL_STORAGE, verifique permisos." + e.getMessage().toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        createComponents(savedInstanceState);
        eventRecorder();
    }

    public void createComponents(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, new ListNotasFragment()).commit();
            getSupportFragmentManager().beginTransaction().addToBackStack(null); // Pila de fragment
        }

        // Configuracion de los menus (drawer y toolbar)
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Todas las notas");

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
                    fragment = new ListNotasFragment();
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
                    getSupportFragmentManager().beginTransaction().addToBackStack(null); // Pila de fragment

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
                    Intent intent = new Intent(MainActivity.this, SegundaActivity.class);
                    intent.putExtra("tipo", "nueva");

                    // Si se esta dentro del listado de notas de una libreta
                    if((libreta = ((ListNotasFragment) currentFragment).getLibreta()) != null) {
                        intent.putExtra("libretaPadre", libreta);
                    }

                    startActivity(intent);
                } else if (currentFragment instanceof  ListLibretasFragment) { // Nueva libreta
                    Intent intent = new Intent(MainActivity.this, CuartaActivity.class);
                    intent.putExtra("tipo", "nueva");
                    startActivity(intent);

                    /*AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    final EditText input = new EditText(MainActivity.this);

                    input.setInputType(InputType.TYPE_CLASS_TEXT);

                    dialog.setTitle("Nueva libreta");
                    dialog.setView(input);

                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FactoryDAO SQLiteFactory = FactoryDAO.getFactory(FactoryDAO.SQLITE_FACTORY);
                            ILibretaDAO libretaDAO = SQLiteFactory.getLibretaDao(getApplicationContext());

                            if (libretaDAO.existTitulo(input.getText().toString())) {
                                Toast.makeText(MainActivity.this, "Ya existe una nota con ese título", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            libretaDAO.createLibreta(input.getText().toString()); // Añadir nueva libreta
                            Toast.makeText(MainActivity.this, "Libreta guardada", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    dialog.create().show();
                    ((ListLibretasFragment) currentFragment).resetListaLibretas();*/

                } else if (currentFragment instanceof  ListEtiquetasFragment) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    final EditText input = new EditText(MainActivity.this);
                    final ListEtiquetasFragment listEtiquetasFragment = (ListEtiquetasFragment) currentFragment;

                    input.setInputType(InputType.TYPE_CLASS_TEXT);

                    dialog.setTitle("Nueva etiqueta");
                    dialog.setView(input);

                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FactoryDAO SQLiteFactory = FactoryDAO.getFactory(FactoryDAO.SQLITE_FACTORY);
                            IEtiquetaDAO etiquetaDAO = SQLiteFactory.getEtiquetaDao(getApplicationContext());

                            if (etiquetaDAO.existTitulo(input.getText().toString())) {
                                Toast.makeText(MainActivity.this, "Ya existe una etiqueta con ese título", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            etiquetaDAO.createEtiqueta(input.getText().toString()); // Añadir nueva etiqueta
                            listEtiquetasFragment.resetListaEtiquetas();
                            Toast.makeText(MainActivity.this, "Etiqueta guardada", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
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

    // Al pulsarse el botón back si el menú está desplegado debería ocultarse. Este comportamiento es
    // necesario implementarlo en la Activity.
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
        Boolean exitApp = true;

        // Cerrar el menu lateral al presinar el boton atras si este menu esta desplegado
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            exitApp = false;
        }

        // Volver a la lista de libretas desde la lista de notas de una libreta dando al boton de atras
        if (currentFragment instanceof ListNotasFragment) {
            if (((ListNotasFragment) currentFragment).getLibreta() != null) { // Si es el listado de notas de una libreta
                fragmentManager.beginTransaction().replace(R.id.fragmentContainer, new ListLibretasFragment()).commit();
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.allLibretas);
                getSupportActionBar().setTitle("Libretas");
                exitApp = false;
            }
        }

        // Volver a todas las notas desdel fragment de libretas al presionar atras
        if (currentFragment instanceof  ListLibretasFragment) {
            fragmentManager.beginTransaction().replace(R.id.fragmentContainer, new ListNotasFragment()).commit();
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.allNotas);
            getSupportActionBar().setTitle("Todas las notas");
            exitApp = false;
        }

        // Salir de la app al presionar atras si no se da ninguna de las condiciones anteriores
        if (exitApp == true) {
            super.onBackPressed();
        }
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