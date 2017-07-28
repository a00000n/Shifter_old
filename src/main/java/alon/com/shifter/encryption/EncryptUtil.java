package alon.com.shifter.encryption;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Alon on 9/23/2016.
 */

public class EncryptUtil {
    public static Byte[] encryptExternal(String[] details)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException {
        if (details == null || details.length != 3)
            throw new IllegalArgumentException("Details passed to encrypt must be of length 3.");
        String totalKey = (details[0] + details[1] + details[2]).replace(" ", "!");
        totalKey = shuffle(totalKey);
        byte[] key = totalKey.getBytes("UTF-8");
        return encrypt(details, false, key);
    }

    public static Byte[] encryptInternal(Object[] info) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        if (!(info[0] instanceof String && info[1] == null))
            throw new IllegalArgumentException("Incorrect parameters given to encryptInternal");
        SecureRandom random = new SecureRandom(new SecureRandom().generateSeed(15));
        String secureRandomText = new BigInteger(260, random).toString(32);
        String key = shuffle(secureRandomText);
        byte[] keyBytes = key.getBytes();
        return encrypt(info, true, keyBytes);
    }

    private static Byte[] encrypt(Object[] info, boolean inOrEx, byte[] keyBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        keyBytes = sha.digest(keyBytes);
        keyBytes = Arrays.copyOf(keyBytes, 16);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        AlgorithmParameters params = cipher.getParameters();
        byte[] IV = params.getParameterSpec(IvParameterSpec.class).getIV();
        byte[] cipherText = cipher.doFinal(info[inOrEx ? 0 : 2].toString().getBytes("UTF-8"));
        info[0] = new String(cipherText);
        if (inOrEx)
            info[1] = secretKeySpec;
        Byte[] iv = new Byte[IV.length];
        for (int i = 0; i < IV.length; i++) {
            Byte b = IV[i];
            iv[i] = b;
        }
        return iv;
    }

    private static String shuffle(String key) {
        int sumChars = key.length();
        char[] tempKey = new char[sumChars];
        for (int i = 0; i < sumChars / 2; i++) {
            int swap = i + sumChars / 2;
            if (i % 4 == 0) {
                swap = sumChars / 2 + i;
                tempKey[sumChars / 2 - i] = key.toCharArray()[swap];
                tempKey[swap] = key.toCharArray()[sumChars / 2 - i];
            } else if (i % 3 == 0) {
                swap = sumChars - i;
                tempKey[i] = key.toCharArray()[swap];
                tempKey[swap] = key.toCharArray()[i];
            } else if (i % 2 == 0) {
                tempKey[i] = key.toCharArray()[swap];
                tempKey[swap] = key.toCharArray()[i];
            }
        }
        return new String(tempKey);
    }

    public static String decrypt(String encrypted, byte[] iv, String[] details)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        if (details == null || details.length != 3)
            throw new IllegalArgumentException("Details passed to encrypt must be of length 3.");
        int sumChars = details[0].length() + details[1].length() + details[2].length();
        String totalKey = (details[0] + details[1] + details[2]).replace(" ", "!");
        char[] tempKey = totalKey.toCharArray();
        for (int i = 0; i < sumChars / 2; i++) {
            int swap = i + sumChars / 2;
            if (i % 2 == 0) {
                tempKey[i] = totalKey.toCharArray()[swap];
                tempKey[swap] = totalKey.toCharArray()[i];
            } else if (i % 3 == 0) {
                swap = sumChars - i;
                tempKey[i] = totalKey.toCharArray()[swap];
                tempKey[swap] = totalKey.toCharArray()[i];
            } else if (i % 4 == 0) {
                swap = sumChars / 2 + i;
                tempKey[sumChars / 2 - i] = totalKey.toCharArray()[swap];
                tempKey[swap] = totalKey.toCharArray()[sumChars / 2 - i];
            }
        }
        totalKey = new String(tempKey);
        byte[] key = totalKey.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        return new String(cipher.doFinal(encrypted.getBytes("UTF-8")), "UTF-8");
    }
}
