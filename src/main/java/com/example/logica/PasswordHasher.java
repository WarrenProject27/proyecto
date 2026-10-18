package com.example.logica;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {

    private static final int ITERACIONES = 120_000;
    private static final int LONGITUD_SALT = 16;
    private static final int LONGITUD_HASH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String contrasena) {
        byte[] salt = new byte[LONGITUD_SALT];
        RANDOM.nextBytes(salt);
        byte[] hash = derivarHash(contrasena, salt, ITERACIONES);
        return "pbkdf2-sha256$" + ITERACIONES + "$"
            + Base64.getEncoder().encodeToString(salt) + "$"
            + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verificar(String contrasena, String hashGuardado) {
        String[] partes = hashGuardado.split("\\$", -1);
        if (partes.length != 4 || !"pbkdf2-sha256".equals(partes[0])) {
            throw new IllegalArgumentException(
                "El formato del hash de contraseña no es válido."
            );
        }

        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] hashEsperado = Base64.getDecoder().decode(partes[3]);
            byte[] hashIngresado = derivarHash(contrasena, salt, iteraciones);
            return MessageDigest.isEqual(hashEsperado, hashIngresado);
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException(
                "El hash de contraseña almacenado está dañado.", error
            );
        }
    }

    private static byte[] derivarHash(
        String contrasena, byte[] salt, int iteraciones
    ) {
        PBEKeySpec especificacion = new PBEKeySpec(
            contrasena.toCharArray(), salt, iteraciones, LONGITUD_HASH
        );
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(especificacion)
                .getEncoded();
        } catch (GeneralSecurityException error) {
            throw new IllegalStateException(
                "No se pudo procesar la contraseña de forma segura.", error
            );
        } finally {
            especificacion.clearPassword();
        }
    }
}
