ExpirableDiskLruCache
=========

ExpirableDiskLruCache is a wrapper for [DiskLruCache](https://github.com/JakeWharton/DiskLruCache) that allows expiring of key/value pairs by specifying evictionTimeSpan. It has very simple API.
If required it allows you to encrypt cached data by simply enabling encryptionEnabled to true. By default, it uses [Conceal by facebook](https://github.com/facebook/conceal) for encryption.
You can provide your custom encryption/decryption as well.

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

or enable encryption

```java
try {
    ExpirableDiskLruCache.getInstance().init(this, 4096, false, true); //in bytes
} catch (Exception e) {
        //failure
}
```
Third parameter is for enable logging. And fourth parameter is for enabling encryption.
If you may want to use your custom encryption/decryption, use:
```java
try {
    ExpirableDiskLruCache.getInstance().init(this, 4096, false, true, customEncrypterDecrypter); //in bytes
} catch (Exception e) {
        //failure
}
```
To create custom encrypter/decrypter simply implement [EncrypterDecrypter.java](https://github.com/vijayrawatsan/ExpirableDiskLruCache/blob/master/app/src/main/java/vijay/expirabledisklrucache/cache/security/EncrypterDecrypter.java)

The best place to do this would be in your application's `onCreate()` method.
Since this library depends directly on [DiskLruCache](https://github.com/JakeWharton/DiskLruCache), you can refer that project for more info on the maximum size you can allocate etc.

## Put stuff

You can put objects into ExpirableDiskLruCache synchronously:

```java
try {
    ExpirableDiskLruCache.getInstance().put("myKey",myObject, myEvictionTimeSpan);
} catch (Exception e) {
    //failure;
}
```
Or asynchronously:
```java
    ExpirableDiskLruCache.getInstance().put("myKey",myObject, myEvictionTimeSpan, putCallback);
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
Or asynchronously:
```java
    ExpirableDiskLruCache.getInstance().get("myKey",MyClass.class, getCallback);
```

## Check for existence

If you wish to know whether an object exists for the given key, you can use:

```java
try {
    boolean objectExists = ExpirableDiskLruCache.getInstance().contains("myKey");
} catch (Exception e) {}
```

## Remove Stuff
You can remove stuff out of ExpirableDiskLruCache synchronously:
```java
try {
    ExpirableDiskLruCache.getInstance().remove("myKey");
} catch (Exception e) {
        //failure
}
```
or asynchronously:
```java
    ExpirableDiskLruCache.getInstance().remove("myKey", deleteCallback);
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

gradle:

Step 1. Add the JitPack repository to your build file

```groovy
repositories {
    maven {
        url "https://jitpack.io"
    }
}
```

Step 2. Add the dependency in the form

```groovy
dependencies {
    compile 'com.github.vijayrawatsan:ExpirableDiskLruCache:0.2'
}
```

maven:

Step 1. Add the JitPack repository to your build file

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

Step 2. Add the dependency in the form

```xml
<dependency>
    <groupId>com.github.vijayrawatsan</groupId>
    <artifactId>ExpirableDiskLruCache</artifactId>
    <version>0.2</version>
</dependency>
```

# FAQs

## What kind of objects can I add to ExpirableDiskLruCache?
Anything that GSON can serialize.

## What happens if my cache size is exceeded?
Older objects will be removed in a LRU (Least Recently Used) order.

# Contributing
Contributions welcome via Github pull requests. Please write test cases as well.

# Credits
ExpirableDiskLruCache is just a tiny little convenience wrapper around the following fantastic projects:

- [DiskLruCache](https://github.com/JakeWharton/DiskLruCache)
- [SimpeDiskCache](https://github.com/fhucho/simple-disk-cache)
- [GSON](https://code.google.com/p/google-gson/)
- [Conceal](https://github.com/facebook/conceal)

Thanks!

# License
This project is licensed under the MIT License. Please refer the [License.txt](https://github.com/vijayrawatsan/ExpirableDiskLruCache/blob/master/License.txt) file.

