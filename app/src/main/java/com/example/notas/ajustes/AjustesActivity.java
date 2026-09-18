package com.example.notas.ajustes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.notas.R;
import com.example.notas.databinding.ActivityAjustesBinding;

/**
 * Pantalla de ajustes: de momento, el tema (según el sistema, claro u oscuro) y
 * el acceso a "Acerca de".
 */
public class AjustesActivity extends AppCompatActivity {

    private ActivityAjustesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAjustesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.ajustes);

        mostrarTemaActual();

        binding.grupoTema.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                aplicarTema(modoDe(checkedId));
            }
        });

        binding.filaAcercaDe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AjustesActivity.this, AcercaDeActivity.class));
            }
        });
    }

    private void mostrarTemaActual() {
        int tema = Preferencias.getTema(this);
        if (tema == AppCompatDelegate.MODE_NIGHT_NO) {
            binding.temaClaro.setChecked(true);
        } else if (tema == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.temaOscuro.setChecked(true);
        } else {
            binding.temaSistema.setChecked(true);
        }
    }

    private int modoDe(int idBoton) {
        if (idBoton == R.id.temaClaro) {
            return AppCompatDelegate.MODE_NIGHT_NO;
        }
        if (idBoton == R.id.temaOscuro) {
            return AppCompatDelegate.MODE_NIGHT_YES;
        }
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }

    private void aplicarTema(int modo) {
        Preferencias.setTema(this, modo);
        AppCompatDelegate.setDefaultNightMode(modo);
    }
}
