package com.inkpot.app.recordatorios;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Programa y cancela los avisos de los recordatorios.
 *
 * <p>Usa {@link AlarmManager} con una alarma que despierta el dispositivo. Se
 * usa el identificador de la nota como código de petición para que cada nota
 * tenga su propia alarma.</p>
 */
public final class ProgramadorRecordatorios {

    private ProgramadorRecordatorios() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * Programa el aviso de una nota.
     *
     * @param context contexto de la aplicación.
     * @param idNota  identificador de la nota.
     * @param cuando  fecha del aviso en milisegundos.
     * @param titulo  título de la nota, para la notificación.
     */
    public static void programar(Context context, int idNota, long cuando, String titulo) {
        AlarmManager alarmas = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmas == null) {
            return;
        }
        PendingIntent pendingIntent = intent(context, idNota, titulo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmas.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cuando, pendingIntent);
        } else {
            alarmas.set(AlarmManager.RTC_WAKEUP, cuando, pendingIntent);
        }
    }

    /**
     * Cancela el aviso de una nota.
     *
     * @param context contexto de la aplicación.
     * @param idNota  identificador de la nota.
     */
    public static void cancelar(Context context, int idNota) {
        AlarmManager alarmas = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = intent(context, idNota, null);
        if (alarmas != null) {
            alarmas.cancel(pendingIntent);
        }
        pendingIntent.cancel();
    }

    private static PendingIntent intent(Context context, int idNota, String titulo) {
        Intent intent = new Intent(context, RecordatorioReceiver.class);
        intent.putExtra(RecordatorioReceiver.EXTRA_NOTA_ID, idNota);
        intent.putExtra(RecordatorioReceiver.EXTRA_TITULO, titulo);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(context, idNota, intent, flags);
    }
}
