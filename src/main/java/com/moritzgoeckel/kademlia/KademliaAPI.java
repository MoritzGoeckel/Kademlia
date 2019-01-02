package com.moritzgoeckel.kademlia;

/** User interface for the Kademlia node */
public interface KademliaAPI {

    /** Stores a key value pair in the network */
    void setValue(String key, String value, int k);

    /** Returns the value for a key */
    String getValue(String key, int k);

    /** Shuts the node down */
    void shutdown();

    boolean isShutdown();
}
