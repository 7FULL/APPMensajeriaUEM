package com.appmsg.appmensajeriauem.utils;


import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class PassUtils {

    private static final int ITERATIONS = 310000;
    private static final int KEY_LENGTH = 256;

    public static String hashPassword(char[] password) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();

        return Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean checkPassword(char[] password, String stored) throws Exception {
        String[] parts = stored.split("\\$");
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] hashStored = Base64.getDecoder().decode(parts[1]);

        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hashAttempt = skf.generateSecret(spec).getEncoded();

        if (hashStored.length != hashAttempt.length) return false;

        for (int i = 0; i < hashStored.length; i++) {
            if (hashStored[i] != hashAttempt[i]) return false;
        }
        return true;
    }
}
