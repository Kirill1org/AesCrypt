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

public class AESCrypt {

    private final byte[] key;
    private final Types aesType;


    public AESCrypt(byte[] key, Types aesType) {

        this.key = key;
        this.aesType = aesType;
    }

    public enum Types {

        CTR("AES/CTR/NoPadding"),
        CBC("AES/CBC/PKCS7Padding");

        private String value;

        Types(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public void encrypt(byte[] iv, ByteBuffer byteBuffer) {
        crypt(iv, byteBuffer, Cipher.ENCRYPT_MODE);
    }

    public void decrypt(byte[] iv, ByteBuffer byteBuffer) {
        crypt(iv, byteBuffer, Cipher.DECRYPT_MODE);
    }

    private void crypt(byte[] iv, ByteBuffer byteBuffer, int cipherMod) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        try {
            Cipher cipher = Cipher.getInstance(aesType.value);
            cipher.init(cipherMod, secretKeySpec, ivParameterSpec);
            cipher.doFinal(byteBuffer, byteBuffer.slice());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | ShortBufferException e) {
            throw new RuntimeException();
        }


    }
}
