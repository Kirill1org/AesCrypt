package bonch.dev.aescrypt;

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

    public byte[] encrypt(byte[] message, byte[] iv) {

        byte[] cryptBuff;
        try {
            cryptBuff = initAesCipher(iv, Cipher.ENCRYPT_MODE).doFinal(message);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            throw new RuntimeException("InitAesParametrs");
        }


        return cryptBuff;


    }

    private Cipher initAesCipher(byte[] iv, int cipherMod) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        Cipher cipher;
        try {
            cipher = Cipher.getInstance(aesType);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            throw new RuntimeException("InitAesParametrs");
        }
        try {
            cipher.init(cipherMod, secretKeySpec, ivParameterSpec);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException e) {
            e.printStackTrace();
            throw new RuntimeException("InitAesParametrs");
        }
        return cipher;

    }

    public byte[] decrypt(byte[] encryptedMessage, byte[] iv) {

        byte[] originalBytes;
        try {
            originalBytes = initAesCipher(iv, Cipher.DECRYPT_MODE).doFinal(encryptedMessage);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            throw new RuntimeException("InitAesParametrs");
        }


        return originalBytes;

    }


}
