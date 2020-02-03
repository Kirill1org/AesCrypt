package bonch.dev.aescrypt;

import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class AesCBC extends AesCrypt {

    public AesCBC(byte[] key) {
        super(key);
    }

    @Override
    Cipher getCipher() {
        try {
            return Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException();
        }
    }
}
