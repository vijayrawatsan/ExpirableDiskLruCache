package vijay.expirabledisklrucache.cache.security;

import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;

import java.io.IOException;

/**
 * Created by vijay on 4/11/15.
 */
public interface EncrypterDecrypter {
    byte[] encrypt(byte[] data, String id) throws Exception;
    byte[] decrypt(byte[] data, String id) throws Exception;
}
