ExpirableDiskLruCache
=========

ExpirableDiskLruCache is a wrapper for [DiskLruCache](https://github.com/JakeWharton/DiskLruCache) that allows expiring of key/value pairs by specifying evictionTimeSpan. It has very simple API.

# Eviction Logic
 - During get it will check if currentTime >  currentTime + evictionSpan. Cache entry is removed.
 - When cache reaches the allocated size, oldest entry is removed from the cache(LRU). Eviction time is not considered at all.


# Usage

## Initialize
ExpirableDiskLruCache uses the internal storage allocated to your app. Before you can do anything, you need to initialize ExpirableDiskLruCache with the cache size.

```java
try {
    ExpirableDiskLruCache.getInstance().init(this, 4096); //in bytes
} catch (Exception e) {
        //failure
}
```

The best place to do this would be in your application's `onCreate()` method.

Since this library depends directly on [DiskLruCache](https://github.com/JakeWharton/DiskLruCache), you can refer that project for more info on the maximum size you can allocate etc.

## Put stuff

You can put objects into ExpirableDiskLruCache synchronously only.

```java
try {
    ExpirableDiskLruCache.getInstance().put("myKey",myObject, myEvictionTimeSpan);
} catch (Exception e) {
    //failure;
}
```

## Get Stuff

You can get stuff out of ExpirableDiskLruCache synchronously.

```java
try {
    ExpirableDiskLruCache.getInstance().get("myKey",MyClass.class);
} catch (Exception e) {
        //failure
}
```

## Check for existence

If you wish to know whether an object exists for the given key, you can use:

```java
try {
    boolean objectExists = ExpirableDiskLruCache.getInstance().contains("myKey");
} catch (Exception e) {}
```

## Remove Stuff

```java
try {
    ExpirableDiskLruCache.getInstance().remove("myKey");
} catch (Exception e) {
        //failure
}
```

## Remove All

```java
try {
    ExpirableDiskLruCache.getInstance().removeAll("myKey");
} catch (Exception e) {
        //failure
}
```

# Including in your project
In progress

# FAQs

## What kind of objects can I add to ExpirableDiskLruCache?
Anything that GSON can serialize.

## What happens if my cache size is exceeded?
Older objects will be removed in a LRU (Least Recently Used) order.

## Can I use this a SharedPreferences replacement?
NO! This is a cache. You should store stuff in here that is good to have around, but you wouldn't mind if they were to be removed. SharedPreferences are meant to store user preferences which is not something you want to lose.

# Sample
In progress

# Contributing
Contributions welcome via Github pull requests.

# Credits
ExpirableDiskLruCache is just a tiny little convenience wrapper around the following fantastic projects:

- [DiskLruCache](https://github.com/JakeWharton/DiskLruCache)
- [SimpeDiskCache](https://github.com/fhucho/simple-disk-cache)
- [GSON](https://code.google.com/p/google-gson/)

Thanks, you guys!

# License
This project is licensed under the MIT License. Please refer the [License.txt](https://github.com/vijayrawatsan/ExpirableDiskLruCache/blob/master/License.txt) file.

