package com.inkpot.app.seguridad;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Gestiona el PIN de bloqueo de la aplicación.
 *
 * <p>El PIN no se guarda tal cual: se guarda una sal aleatoria y el resultado de
 * derivarlo con PBKDF2. Para comprobarlo se vuelve a derivar y se comparan los
 * resultados en tiempo constante.</p>
 *
 * <p>Ojo: esto es una pantalla de bloqueo, no cifrado. La base de datos sigue
 * guardándose sin cifrar; protege del acceso casual, no de alguien con acceso
 * físico y conocimientos.</p>
 */
public final class GestorPin {

    private static final String PREFS = "seguridad";
    private static final String CLAVE_SAL = "sal";
    private static final String CLAVE_HASH = "hash";
    private static final String ALGORITMO = "PBKDF2WithHmacSHA1";
    private static final int ITERACIONES = 10000;
    private static final int LONGITUD_CLAVE = 256;
    private static final int LONGITUD_SAL = 16;

    /** Se pone a true tras desbloquear; se pierde al morir el proceso. */
    private static boolean desbloqueado = false;

    private GestorPin() {
        // Clase de utilidades: no se instancia.
    }

    /** Indica si hay un PIN configurado. */
    public static boolean hayPin(Context context) {
        SharedPreferences prefs = prefs(context);
        return prefs.contains(CLAVE_SAL) && prefs.contains(CLAVE_HASH);
    }

    /** Guarda un PIN nuevo (o cambia el actual). */
    public static void guardarPin(Context context, String pin) {
        byte[] sal = new byte[LONGITUD_SAL];
        new SecureRandom().nextBytes(sal);

        prefs(context).edit()
                .putString(CLAVE_SAL, Base64.encodeToString(sal, Base64.NO_WRAP))
                .putString(CLAVE_HASH, derivar(pin, sal))
                .apply();
        desbloqueado = true;
    }

    /** Comprueba si el PIN es el correcto. */
    public static boolean comprobar(Context context, String pin) {
        SharedPreferences prefs = prefs(context);
        String salGuardada = prefs.getString(CLAVE_SAL, null);
        String hashGuardado = prefs.getString(CLAVE_HASH, null);
        if (salGuardada == null || hashGuardado == null) {
            return false;
        }

        byte[] sal = Base64.decode(salGuardada, Base64.NO_WRAP);
        String hash = derivar(pin, sal);
        return MessageDigest.isEqual(
                hash.getBytes(StandardCharsets.UTF_8),
                hashGuardado.getBytes(StandardCharsets.UTF_8));
    }

    /** Quita el PIN. */
    public static void quitarPin(Context context) {
        prefs(context).edit().clear().apply();
        desbloqueado = true;
    }

    /** Indica si la aplicación está desbloqueada en esta sesión. */
    public static boolean estaDesbloqueado() {
        return desbloqueado;
    }

    /** Marca la aplicación como desbloqueada. */
    public static void marcarDesbloqueado() {
        desbloqueado = true;
    }

    /** Vuelve a dejar la aplicación bloqueada. */
    public static void bloquear() {
        desbloqueado = false;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String derivar(String pin, byte[] sal) {
        try {
            PBEKeySpec especificacion = new PBEKeySpec(pin.toCharArray(), sal, ITERACIONES, LONGITUD_CLAVE);
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance(ALGORITMO);
            byte[] clave = fabrica.generateSecret(especificacion).getEncoded();
            return Base64.encodeToString(clave, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo derivar la clave del PIN", e);
        }
    }
}
