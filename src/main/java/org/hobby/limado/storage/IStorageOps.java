package org.hobby.limado.storage;

public interface IStorageOps<K,V> {
    int write(K key, V value) throws Exception;
    V read(K key) throws Exception;
}
