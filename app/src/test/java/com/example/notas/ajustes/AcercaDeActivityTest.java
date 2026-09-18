package com.example.notas.ajustes;

import static org.junit.Assert.assertTrue;

import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.notas.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class AcercaDeActivityTest {
    @Test
    public void muestraLaVersionDeLaAplicacion() {
        AcercaDeActivity activity = Robolectric.buildActivity(AcercaDeActivity.class).setup().get();

        TextView version = activity.findViewById(R.id.textViewVersion);
        assertTrue(version.getText().toString().contains("Versión"));
    }
}
