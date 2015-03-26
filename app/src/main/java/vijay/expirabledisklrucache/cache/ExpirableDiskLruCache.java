package vijay.expirabledisklrucache.cache;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by vijay
 */
public class ExpirableDiskLruCache {

    private static final String LOG_TAG                    = "EXPIRABLE_DISK_CACHE";

    private static final String EVICTION_TIME              = "EVICTION_TIME";

    private static final Long   DEFAULT_EVICTION_TIME_SPAN = Long.MAX_VALUE;

    private static boolean      sLogEnabled                = false;

    private SimpleDiskCache     mSimpleDiskCache;

    private Gson                mGson;

    private ExpirableDiskLruCache() {
    }

    public void init(Context context, Long size) throws IOException {
        mGson = new Gson();
        mSimpleDiskCache = SimpleDiskCache.open(context.getFilesDir(), 1, size);
    }

    public void init(Context context, Long size, boolean logEnabled) throws IOException {
        mGson = new Gson();
        mSimpleDiskCache = SimpleDiskCache.open(context.getFilesDir(), 1, size);
        sLogEnabled = logEnabled;
    }

    /**
     * Will never be expired based on evictionTimeSpan. Can only be expired if size of cache is full and this is the oldest entry in cache
     * @param key
     * @param value
     * @throws IOException
     */
    public void put(String key, Object value) throws IOException {
        put(key, value, DEFAULT_EVICTION_TIME_SPAN);
    }

    /**
     * Will be expired based on evictionTimeSpan. And can also be expired if size of cache is full and this is the oldest entry in cache
     * @param key
     * @param value
     * @param evictionTimeSpan
     * @throws IOException
     */
    public void put(String key, Object value, Long evictionTimeSpan) throws IOException {
        Map<String, Serializable> map = new HashMap<>(1);
        map.put(EVICTION_TIME, getEvictionTime(evictionTimeSpan));
        mSimpleDiskCache.put(key, mGson.toJson(value), map);
        if (sLogEnabled)
            Log.d(LOG_TAG, "[PUT] : " + key);
    }

    public <T> T get(String key, Class<T> classOfT) throws IOException {
        SimpleDiskCache.StringEntry entry = mSimpleDiskCache.getString(key);
        if (entry == null) {
            if (sLogEnabled)
                Log.d(LOG_TAG, "[MISS] : " + key);
            return null;
        }
        Map<String, Serializable> metadata = entry.getMetadata();
        Long evictionTime = (Long) metadata.get(EVICTION_TIME);
        if (System.currentTimeMillis() <= evictionTime) {
            if (sLogEnabled)
                Log.d(LOG_TAG, "[HIT] : " + key);
            return mGson.fromJson(entry.getString(), classOfT);
        }
        if (sLogEnabled)
            Log.d(LOG_TAG, "[EXPIRED] : " + key);
        remove(key);
        return null;
    }

    public void remove(String key) throws IOException {
        mSimpleDiskCache.remove(key);
        if (sLogEnabled)
            Log.d(LOG_TAG, "[REMOVED] : " + key);
    }

    public boolean contains(String key) throws IOException {
        return mSimpleDiskCache.contains(key);
    }

    public void removeAll() throws IOException {
        mSimpleDiskCache.clear();
        if (sLogEnabled)
            Log.d(LOG_TAG, "[ALL CLEARED] : ");
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

}
