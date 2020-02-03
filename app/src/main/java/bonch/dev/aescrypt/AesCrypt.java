package bonch.dev.aescrypt;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

abstract class AesCrypt {

    private final byte[] key;

    protected AesCrypt(byte[] key) {
        this.key = key;
    }

    abstract Cipher getCipher();

    public void encrypt(byte[] iv, ByteBuffer byteBuffer) {
        crypt(iv, byteBuffer, Cipher.ENCRYPT_MODE);
    }

    public void decrypt(byte[] iv, ByteBuffer byteBuffer) {
        crypt(iv, byteBuffer, Cipher.DECRYPT_MODE);
    }

    protected void crypt(byte[] iv, ByteBuffer byteBuffer, int cipherMod) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(cipherMod, secretKeySpec, ivParameterSpec);
            cipher.doFinal(byteBuffer, byteBuffer.slice());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | ShortBufferException e) {
            throw new RuntimeException();
        }
    }
}
