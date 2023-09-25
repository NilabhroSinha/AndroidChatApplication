package com.example.chatapplication.model;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    private static final String iv = "5633496197921896";
    private static final String key = "$B?E(H+MbQeThWmZ";

    public static String encrypt(String input) throws Exception {

        byte[] raw = key.getBytes(StandardCharsets.UTF_8);

        SecretKeySpec seckey = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/Iso10126Padding");
        IvParameterSpec ivps = new IvParameterSpec(iv.getBytes( StandardCharsets.UTF_8 ));

        cipher.init(Cipher.ENCRYPT_MODE, seckey, ivps);

        byte[] encrypted = cipher.doFinal(input.getBytes( StandardCharsets.UTF_8 ));

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String sSrc) throws Exception {
        try {
            byte[] raw = key.getBytes(StandardCharsets.UTF_8);

            SecretKeySpec seckey = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/Iso10126Padding");
            IvParameterSpec ivps = new IvParameterSpec(iv.getBytes( StandardCharsets.UTF_8 ));

            cipher.init(Cipher.DECRYPT_MODE, seckey, ivps);

            byte[] encrypted1 = Base64.getDecoder().decode(sSrc);

            try {
                byte[] original = cipher.doFinal(encrypted1);
                return new String(original, StandardCharsets.UTF_8);
            }catch (Exception e) {
                return null;
            }
        }catch (Exception ex) {
            return null;
        }
    }

}
