public interface IUserNode {
    /** Instructs the node to store the KeyValuePair somewhere in the network*/
    void setValue(KeyValuePair pair);

    /** Returns the k closest known nodes to the nodeId */
    KeyValuePair getValue(HashKey id);
}
