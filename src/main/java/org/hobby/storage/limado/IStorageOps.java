package org.hobby.storage.limado;

public interface IStorageOps<K,V> {
    void write(K key, V value) throws Exception;
    V read(K key) throws Exception;
}
