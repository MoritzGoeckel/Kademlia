package com.moritzgoeckel.kademlia;

import java.io.Serializable;

/** Pair of a HashKey and a value */
class KeyValuePair  implements Serializable {
    private final HashKey key;
    private final String value;

    KeyValuePair(HashKey key, String value){
        this.key = key;
        this.value = value;
    }

    HashKey getKey() {
        return key;
    }

    String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return key + " -> " + value;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
