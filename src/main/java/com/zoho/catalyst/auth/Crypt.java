package com.zoho.catalyst.auth;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class Crypt {
    public static SecretKey getPasswordFromKey(String key, String salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec spec = new PBEKeySpec(key.toCharArray(), salt.getBytes(), 1000, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return secret;
    }

    public static String decrypt(String input) throws Exception { // todo: throw proper EXCEPTION
        byte[] encodedByteArr = Hex.decodeHex(input);
        byte[] initializationVector = Arrays.copyOfRange(encodedByteArr, 0, 16);
        byte[] encodedData = Arrays.copyOfRange(encodedByteArr, 17, encodedByteArr.length);

        SecretKey password = getPasswordFromKey("ZC_TRAM", new String(initializationVector));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, password, new IvParameterSpec(initializationVector));
        byte[] decodedData = cipher.doFinal(encodedData);
        return new String(decodedData);
    }
}
