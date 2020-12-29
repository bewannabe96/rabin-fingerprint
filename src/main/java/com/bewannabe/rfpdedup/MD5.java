package com.bewannabe.rfpdedup;

import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException; 

public class MD5 {
    private MD5() {};

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String hash(byte[] input) {
        try { 
            MessageDigest md = MessageDigest.getInstance("MD5"); 
            return bytesToHex(md.digest(input));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } 
    }

    public static String hash(String input) {
        return hash(input.getBytes());
    }
}