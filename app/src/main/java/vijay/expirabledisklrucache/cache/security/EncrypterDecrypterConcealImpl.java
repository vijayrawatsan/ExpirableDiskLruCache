package vijay.expirabledisklrucache.cache.security;

import java.io.IOException;

import android.content.Context;

import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

/**
 * Created by vijay on 4/11/15.
 */
public class EncrypterDecrypterConcealImpl implements EncrypterDecrypter {

    // Creates a new Crypto object with default implementations of
    // a key chain as well as native library.
    private Crypto mCrypto;

    public EncrypterDecrypterConcealImpl(Context context) throws KeyChainException, CryptoInitializationException,
            IOException {
        mCrypto = new Crypto(new SharedPrefsBackedKeyChain(context), new SystemNativeCryptoLibrary());
        if (!mCrypto.isAvailable()) {
            throw new CryptoInitializationException(new Throwable("Failed to load crypto libs."));
        }
    }

    @Override
    public byte[] encrypt(byte[] data, String id) throws KeyChainException, CryptoInitializationException, IOException {
        return mCrypto.encrypt(data, new Entity(id));
    }

    @Override
    public byte[] decrypt(byte[] data, String id) throws KeyChainException, CryptoInitializationException, IOException {
        return mCrypto.decrypt(data, new Entity(id));
    }
}
