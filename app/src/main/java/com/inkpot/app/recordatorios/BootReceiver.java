package com.inkpot.app.recordatorios;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.inkpot.app.data.Nota;
import com.inkpot.app.data.NotasRepository;

import java.util.List;

/**
 * Vuelve a programar los recordatorios pendientes al reiniciar el dispositivo.
 *
 * <p>Las alarmas se pierden al apagar el teléfono, así que al arrancar se
 * consultan las notas con recordatorio y se programan de nuevo las que aún no
 * han vencido.</p>
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        final PendingResult resultado = goAsync();
        NotasRepository.get(context).notasConRecordatorio(new NotasRepository.Callback<List<Nota>>() {
            @Override
            public void onResult(List<Nota> notas) {
                long ahora = System.currentTimeMillis();
                for (Nota nota : notas) {
                    if (nota.getRecordatorio() > ahora) {
                        ProgramadorRecordatorios.programar(context, nota.getId(), nota.getRecordatorio(), nota.getTitulo());
                    }
                }
                resultado.finish();
            }
        });
    }
}
