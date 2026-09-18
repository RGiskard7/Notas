package com.inkpot.app.recordatorios;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.inkpot.app.MainActivity;
import com.inkpot.app.R;

/**
 * Recibe el aviso programado y muestra la notificación del recordatorio.
 */
public class RecordatorioReceiver extends BroadcastReceiver {

    public static final String EXTRA_NOTA_ID = "nota_id";
    public static final String EXTRA_TITULO = "titulo";

    private static final String CANAL = "recordatorios";

    @Override
    public void onReceive(Context context, Intent intent) {
        int idNota = intent.getIntExtra(EXTRA_NOTA_ID, -1);
        String titulo = intent.getStringExtra(EXTRA_TITULO);
        if (idNota == -1) {
            return;
        }

        crearCanal(context);

        Intent abrir = new Intent(context, MainActivity.class);
        abrir.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contenido = PendingIntent.getActivity(context, idNota, abrir,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificacion = new NotificationCompat.Builder(context, CANAL)
                .setSmallIcon(R.mipmap.ic_icon_notas)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(titulo == null ? context.getString(R.string.recordatorio_aviso) : titulo)
                .setContentIntent(contenido)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat gestor = NotificationManagerCompat.from(context);
        if (gestor.areNotificationsEnabled()) {
            gestor.notify(idNota, notificacion.build());
        }
    }

    private void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager gestor = context.getSystemService(NotificationManager.class);
            if (gestor != null && gestor.getNotificationChannel(CANAL) == null) {
                NotificationChannel canal = new NotificationChannel(
                        CANAL,
                        context.getString(R.string.canal_recordatorios),
                        NotificationManager.IMPORTANCE_DEFAULT);
                gestor.createNotificationChannel(canal);
            }
        }
    }
}
