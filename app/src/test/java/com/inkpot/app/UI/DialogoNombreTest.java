package com.inkpot.app.UI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Looper;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.inkpot.app.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;

/**
 * Pruebas del diálogo de nombre compartido por libretas y etiquetas.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class DialogoNombreTest {

    private String capturado;

    private AlertDialog mostrar(String valorInicial) {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
        DialogoNombre.mostrar(activity, "Título", valorInicial, new DialogoNombre.OnNombreAceptado() {
            @Override
            public void onNombre(String nombre) {
                capturado = nombre;
            }
        });
        return (AlertDialog) ShadowDialog.getLatestDialog();
    }

    @Test
    public void muestraElValorInicial() {
        AlertDialog dialogo = mostrar("Trabajo");

        EditText entrada = dialogo.findViewById(R.id.entradaNombre);

        assertEquals("Trabajo", entrada.getText().toString());
    }

    @Test
    public void nombreValido_avisaConElTextoRecortado() {
        AlertDialog dialogo = mostrar("");
        EditText entrada = dialogo.findViewById(R.id.entradaNombre);
        entrada.setText("  Estudios  ");

        dialogo.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();

        assertEquals("Estudios", capturado);
    }

    @Test
    public void nombreVacio_noAvisa() {
        AlertDialog dialogo = mostrar("");
        EditText entrada = dialogo.findViewById(R.id.entradaNombre);
        entrada.setText("   ");

        dialogo.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();

        assertNull(capturado);
    }
}
