package bonch.dev.aescrypt;

import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESCrypt {

    private final byte[] key;
    private final String aesType;


    public AESCrypt(byte[] key, String aesType) {

        this.key = key;
        this.aesType = aesType;
    }

    public byte[] encrypt(byte[] iv, byte[] data, int offset, int length) {
        return crypt(iv, data, offset, length, Cipher.ENCRYPT_MODE);
    }

    public byte[] decrypt(byte[] iv, byte[] encryptedData, int offset, int length) {
        return crypt(iv, encryptedData, offset, length, Cipher.DECRYPT_MODE);
    }

    private byte[] crypt(byte[] iv, byte[] data, int offset, int length, int cipherMod) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        try {
            Cipher cipher = Cipher.getInstance(aesType);
            cipher.init(cipherMod, secretKeySpec, ivParameterSpec);
            return cipher.doFinal(data, offset, length);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException();
        }
    }


}
