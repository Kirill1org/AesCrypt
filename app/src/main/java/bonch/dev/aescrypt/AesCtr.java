package bonch.dev.aescrypt;

import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class AesCtr extends AesCrypt {

    public AesCtr(byte[] key) {
        super(key);
    }

    @Override
    public Cipher getCipher() {
        try {
            return Cipher.getInstance("AES/CTR/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException();
        }
    }

}
