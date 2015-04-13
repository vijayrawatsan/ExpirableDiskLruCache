package vijay.expirabledisklrucache.cache.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import vijay.expirabledisklrucache.BuildConfig;
import vijay.expirabledisklrucache.cache.ExpirableDiskLruCache;
import vijay.expirabledisklrucache.cache.security.EncrypterDecrypter;

import com.google.gson.Gson;

/**
 * Created by vijay on 4/11/15.
 */
@RunWith(CustomRobolectricRunner.class)
@Config(constants = BuildConfig.class)
public class BasicTest {

    private static final String   UTF_8 = "utf-8";

    private Gson                  mGson;

    private ExpirableDiskLruCache mCache;

    private EncrypterDecrypter    mEncrypterDecrypterMock;

    @Before
    public void setUp() {
        try {
            mGson = new Gson();
            mEncrypterDecrypterMock = Mockito.mock(EncrypterDecrypter.class);
            ExpirableDiskLruCache.getInstance().init(RuntimeEnvironment.application, 10000l, false, true,
                    mEncrypterDecrypterMock);
            mCache = ExpirableDiskLruCache.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldPutAndGet() throws Exception {
        setUpMocks("key", "value");

        mCache.put("key", "value");

        assertThat(mCache.get("key", String.class)).isEqualTo("value");

        verifyMethodCalls("key", "value");
    }

    @Test
    public void shouldEvict() throws Exception {
        setUpMocks("key", "value");

        mCache.put("key", "value", 1000l);

        Thread.sleep(500);
        assertThat(mCache.get("key", String.class)).isEqualTo("value");

        verifyMethodCalls("key", "value");

        Thread.sleep(600);
        assertThat(mCache.get("key", String.class)).isEqualTo(null);
    }

    @Test
    public void shouldRemove() throws Exception {
        setUpMocks("key", "value");

        mCache.put("key", "value");

        assertThat(mCache.get("key", String.class)).isEqualTo("value");

        verifyMethodCalls("key", "value");

        mCache.remove("key");

        assertThat(mCache.get("key", String.class)).isEqualTo(null);
    }

    @Test
    public void shouldRemoveAll() throws Exception {
        setUpMocks("key1", "value1");
        setUpMocks("key2", "value2");

        mCache.put("key1", "value1");
        mCache.put("key2", "value2");

        assertThat(mCache.get("key1", String.class)).isEqualTo("value1");
        assertThat(mCache.get("key2", String.class)).isEqualTo("value2");

        verifyMethodCalls("key1", "value1");
        verifyMethodCalls("key2", "value2");

        mCache.removeAll();

        assertThat(mCache.get("key1", String.class)).isEqualTo(null);
        assertThat(mCache.get("key2", String.class)).isEqualTo(null);
    }

    @After
    public void tearDown() {
        try {
            mCache.removeAll();
            mCache = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void verifyMethodCalls(String key, String value) throws Exception {
        byte[] values = mGson.toJson(value).getBytes(UTF_8);
        Mockito.verify(mEncrypterDecrypterMock).encrypt(values, key);
        Mockito.verify(mEncrypterDecrypterMock).decrypt(encryptOrDecrypt(values), key);
    }

    private void setUpMocks(String key, String value) throws Exception {
        byte[] values = mGson.toJson(value).getBytes(UTF_8);
        Mockito.when(mEncrypterDecrypterMock.encrypt(values, key)).thenReturn(encryptOrDecrypt(values));
        Mockito.when(mEncrypterDecrypterMock.decrypt(encryptOrDecrypt(values), key)).thenReturn(
                encryptOrDecrypt(encryptOrDecrypt(values)));
    }

    /**
     * Basic reverse
     *
     * @param input
     * @return
     */
    private byte[] encryptOrDecrypt(byte[] input) {
        byte[] one = Arrays.copyOf(input, input.length);
        int length = one.length;
        for (int i = 0; i < (length / 2); i++) {
            byte temp = one[i];
            one[i] = one[length - (i + 1)];
            one[length - (i + 1)] = temp;
        }
        return one;
    }

}
