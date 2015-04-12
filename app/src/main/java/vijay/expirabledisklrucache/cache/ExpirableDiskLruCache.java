package vijay.expirabledisklrucache.cache;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import vijay.expirabledisklrucache.cache.security.EncrypterDecrypter;
import vijay.expirabledisklrucache.cache.security.EncrypterDecrypterConcealImpl;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by vijay
 */
public class ExpirableDiskLruCache {

    private static final String LOG_TAG                    = "EXPIRABLE_DISK_CACHE";

    private static final String EVICTION_TIME              = "EVICTION_TIME";

    private static final Long   DEFAULT_EVICTION_TIME_SPAN = Long.MAX_VALUE;
    public static final String  UTF_8                      = "utf-8";

    private static boolean      sLogEnabled                = false;

    private SimpleDiskCache     mSimpleDiskCache;

    private EncrypterDecrypter  mEncrypterDecrypter;

    private Gson                mGson;

    private boolean             sEncryptionEnabled         = false;

    private ExpirableDiskLruCache() {
    }

    public void init(Context context, Long size) throws Exception {
        init(context, size, false);
    }

    public void init(Context context, Long size, boolean logEnabled) throws Exception {
        init(context, size, logEnabled, false);
    }

    public void init(Context context, Long size, boolean logEnabled, boolean encryptionEnabled) throws Exception {
        EncrypterDecrypter encrypterDecrypter = null;
        if (encryptionEnabled) {
            encrypterDecrypter = new EncrypterDecrypterConcealImpl(context);
        }
        init(context, size, logEnabled, encryptionEnabled, encrypterDecrypter);
    }

    public void init(Context context, Long size, boolean logEnabled, boolean encryptionEnabled,
            EncrypterDecrypter encrypterDecrypter) throws Exception {
        mGson = new Gson();
        mSimpleDiskCache = SimpleDiskCache.open(context.getFilesDir(), 1, size);
        sLogEnabled = logEnabled;
        sEncryptionEnabled = encryptionEnabled;
        if (sEncryptionEnabled) {
            mEncrypterDecrypter = encrypterDecrypter;
        }
    }

    /**
     * Will never be expired based on evictionTimeSpan. Can only be expired if
     * size of cache is full and this is the oldest entry in cache
     *
     * @param key
     * @param value
     * @throws IOException
     */
    public void put(String key, Object value) throws Exception {
        put(key, value, DEFAULT_EVICTION_TIME_SPAN);
    }

    public void put(String key, Object value, PutCallback callback) {
        new PutTask(key, value, DEFAULT_EVICTION_TIME_SPAN, callback).execute();
    }

    /**
     * Will be expired based on evictionTimeSpan. And can also be expired if
     * size of cache is full and this is the oldest entry in cache
     *
     * @param key
     * @param value
     * @param evictionTimeSpan
     * @throws IOException
     */
    public void put(String key, Object value, Long evictionTimeSpan) throws Exception {
        Map<String, Serializable> map = new HashMap<>(1);
        map.put(EVICTION_TIME, getEvictionTime(evictionTimeSpan));
        byte[] valueBytes = mGson.toJson(value).getBytes(UTF_8);
        if (sEncryptionEnabled) {
            valueBytes = mEncrypterDecrypter.encrypt(valueBytes, key);
        }
        mSimpleDiskCache.put(key, valueBytes.toString(), map);
        if (sLogEnabled) {
            Log.d(LOG_TAG, "[PUT] : " + key);
        }
    }

    public void put(String key, Object value, Long evictionTimeSpan, PutCallback callback) {
        new PutTask(key, value, evictionTimeSpan, callback).execute();
    }

    public <T> T get(String key, Class<T> classOfT) throws Exception {
        SimpleDiskCache.StringEntry entry = mSimpleDiskCache.getString(key);
        if (entry == null) {
            if (sLogEnabled) {
                Log.d(LOG_TAG, "[MISS] : " + key);
            }
            return null;
        }
        Map<String, Serializable> metadata = entry.getMetadata();
        Long evictionTime = (Long) metadata.get(EVICTION_TIME);
        if (System.currentTimeMillis() <= evictionTime) {
            if (sLogEnabled) {
                Log.d(LOG_TAG, "[HIT] : " + key);
            }
            byte[] valueBytes = entry.getString().getBytes(UTF_8);
            if (sEncryptionEnabled) {
                valueBytes = mEncrypterDecrypter.decrypt(valueBytes, key);
            }
            return mGson.fromJson(valueBytes.toString(), classOfT);
        }
        if (sLogEnabled) {
            Log.d(LOG_TAG, "[EXPIRED] : " + key);
        }
        remove(key);
        return null;
    }

    public <T> void get(String key, Class<T> classOfT, GetCallback<T> callback) {
        new GetTask(key, classOfT, callback).execute();
    }

    public void remove(String key) throws Exception {
        mSimpleDiskCache.remove(key);
        if (sLogEnabled) {
            Log.d(LOG_TAG, "[REMOVED] : " + key);
        }
    }

    public void remove(String key, RemoveCallback callback) {
        new RemoveTask(key, callback).execute();
    }

    public boolean contains(String key) throws IOException {
        return mSimpleDiskCache.contains(key);
    }

    public void removeAll() throws Exception {
        mSimpleDiskCache.clear();
        if (sLogEnabled) {
            Log.d(LOG_TAG, "[ALL CLEARED]");
        }
    }

    private Long getEvictionTime(Long evictionTimeSpan) {
        return System.currentTimeMillis() + evictionTimeSpan;
    }

    private static class LazyHolder {
        private static final ExpirableDiskLruCache INSTANCE = new ExpirableDiskLruCache();
    }

    public static ExpirableDiskLruCache getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * AsyncTask to perform put operation in a background thread.
     */
    private static class PutTask extends AsyncTask<Void, Void, Void> {
        private final String      mKey;
        private final Long        mEvictionTimeSpan;
        private Exception         mException;
        private final PutCallback mCallback;
        final Object              mValue;

        private PutTask(String key, Object value, Long evictionTimeSpan, PutCallback callback) {
            mKey = key;
            mEvictionTimeSpan = evictionTimeSpan;
            mCallback = callback;
            mValue = value;
            mException = null;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                getInstance().put(mKey, mValue, mEvictionTimeSpan);
            } catch (Exception e) {
                mException = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mCallback != null) {
                if (mException == null) {
                    mCallback.onSuccess();
                } else {
                    mCallback.onFailure(mException);
                }
            }
        }
    }

    /**
     * AsyncTask to perform get operation in a background thread.
     */
    private static class GetTask<T> extends AsyncTask<Void, Void, T> {

        private final String      mKey;
        private final GetCallback mCallback;
        private final Class<T>    mClassOfT;
        private Exception         mException;

        private GetTask(String key, Class<T> classOfT, GetCallback<T> callback) {
            mKey = key;
            mCallback = callback;
            mClassOfT = classOfT;
            mException = null;
        }

        @Override
        protected T doInBackground(Void... params) {
            try {
                return getInstance().get(mKey, mClassOfT);
            } catch (Exception e) {
                mException = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(T object) {
            if (mCallback != null) {
                if (mException == null) {
                    mCallback.onSuccess(object);
                } else {
                    mCallback.onFailure(mException);
                }
            }
        }
    }

    /**
     * AsyncTask to perform delete operation in a background thread.
     */
    private static class RemoveTask extends AsyncTask<Void, Void, Void> {

        private final String         mKey;
        private final RemoveCallback mCallback;
        private Exception            mException;

        private RemoveTask(String key, RemoveCallback callback) {
            mKey = key;
            mCallback = callback;
            mException = null;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                getInstance().remove(mKey);
            } catch (Exception e) {
                mException = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mCallback != null) {
                if (mException == null) {
                    mCallback.onSuccess();
                } else {
                    mCallback.onFailure(mException);
                }
            }
        }
    }

    public interface RemoveCallback {
        public void onSuccess();

        public void onFailure(Exception e);
    }

    public interface GetCallback<T> {
        public void onSuccess(T object);

        public void onFailure(Exception e);
    }

    public interface PutCallback {
        public void onSuccess();

        public void onFailure(Exception e);
    }
}
