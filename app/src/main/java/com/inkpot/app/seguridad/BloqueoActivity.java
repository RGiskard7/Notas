package com.inkpot.app.seguridad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.inkpot.app.MainActivity;
import com.inkpot.app.R;

/**
 * Pantalla de bloqueo: pide el PIN antes de dejar entrar a la aplicación.
 */
public class BloqueoActivity extends AppCompatActivity {

    private EditText pin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloqueo);

        pin = findViewById(R.id.editTextPin);
        Button desbloquear = findViewById(R.id.buttonDesbloquear);
        desbloquear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comprobarPin();
            }
        });

        // Si ya no hay PIN (por ejemplo, se ha quitado), se entra directamente.
        if (!GestorPin.hayPin(this)) {
            GestorPin.marcarDesbloqueado();
            abrirAplicacion();
        }
    }

    private void comprobarPin() {
        if (GestorPin.comprobar(this, pin.getText().toString())) {
            GestorPin.marcarDesbloqueado();
            abrirAplicacion();
        } else {
            pin.setText("");
            Toast.makeText(this, R.string.pin_incorrecto, Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirAplicacion() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
