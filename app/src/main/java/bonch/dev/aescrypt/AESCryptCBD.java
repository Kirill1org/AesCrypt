package bonch.dev.aescrypt;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESCryptCBD {

    public static AESCryptCBD instance = null;

    private byte[] KEY;
    private final byte[] IV;

    private AESCryptCBD(byte[] key, byte[] iv) {

        KEY = key;
        IV = iv;
    }

    public static AESCryptCBD getInstance(byte[] key, byte[] iv) {

        if (instance == null) {
            instance = new AESCryptCBD(key, iv);
        }
        return instance;
    }

    public byte[] encrypt(byte[] message) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        Log.e("AES_CBD_ENCRYPT", "INPUT LENGTH: "+message.length);

        SecretKeySpec secretKeySpec = new SecretKeySpec(KEY, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

        byte[] cryptBuff = cipher.doFinal(message);
        Log.e("AES_CTR_ENCRYPT", "OUTPUT: "+ Arrays.toString(cryptBuff));
        Log.e("AES_CBD_ENCRYPT", "OUTPUT_LENGTH: "+cryptBuff.length);
        Log.e("AES_CBD_ENCRYPT", "INIT VECOTR: "+Arrays.toString(ivParameterSpec.getIV()));
        return cryptBuff;


    }

    public byte[] decrypt(byte[] encryptedMessage) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, UnsupportedEncodingException {


        Log.e("AES_CBD_DECRYPT", "INPUT LENGTH: "+encryptedMessage.length);

        SecretKeySpec skeySpec = new SecretKeySpec(KEY, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV);

        Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        ecipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);

        byte[] originalBytes = ecipher.doFinal(encryptedMessage);
        Log.e("AES_CBD_DECRYPT", "OUTPUT_LENGTH: "+originalBytes.length);
        Log.e("AES_CBD_DECRYPT", "INIT VECOTR: "+Arrays.toString(ivSpec.getIV()));

        return originalBytes;

    }


}
