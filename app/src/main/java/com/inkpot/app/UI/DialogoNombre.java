package com.inkpot.app.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.inkpot.app.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Diálogo con un único campo de texto, para crear o renombrar libretas y
 * etiquetas. Es el mismo para ambas, para que la experiencia sea coherente.
 */
public final class DialogoNombre {

    /** Recibe el nombre que ha aceptado el usuario. */
    public interface OnNombreAceptado {
        void onNombre(String nombre);
    }

    private DialogoNombre() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * Muestra el diálogo.
     *
     * @param context      contexto con tema Material.
     * @param titulo       título del diálogo.
     * @param valorInicial texto que aparece de partida (vacío al crear).
     * @param callback     recibe el nombre escrito.
     */
    public static void mostrar(Context context, String titulo, String valorInicial, final OnNombreAceptado callback) {
        View vista = LayoutInflater.from(context).inflate(R.layout.dialogo_nombre, null);
        final TextInputEditText entrada = vista.findViewById(R.id.entradaNombre);
        entrada.setText(valorInicial);
        entrada.setSelection(entrada.getText().length());

        new MaterialAlertDialogBuilder(context)
                .setTitle(titulo)
                .setView(vista)
                .setPositiveButton(R.string.guardar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nombre = entrada.getText() == null ? "" : entrada.getText().toString().trim();
                        if (TextUtils.isEmpty(nombre)) {
                            Toast.makeText(context, R.string.nombre_vacio, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        callback.onNombre(nombre);
                    }
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }
}
