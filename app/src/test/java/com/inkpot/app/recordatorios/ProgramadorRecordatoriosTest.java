package com.inkpot.app.recordatorios;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlarmManager;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ProgramadorRecordatoriosTest {
    @Test
    public void programaYcancelaLaAlarma() {
        Context context = ApplicationProvider.getApplicationContext();
        AlarmManager alarmas = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        ShadowAlarmManager shadow = shadowOf(alarmas);

        ProgramadorRecordatorios.programar(context, 7, System.currentTimeMillis() + 60000, "Titulo");
        assertEquals(1, shadow.getScheduledAlarms().size());

        ProgramadorRecordatorios.cancelar(context, 7);
        assertEquals(0, shadow.getScheduledAlarms().size());
    }
}
