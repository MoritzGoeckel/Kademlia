public interface INode {
    /** Returns true if the node is still reachable */
    boolean ping(RemoteNode sender);

    /** Instructs the node to store the KeyValuePair */
    void store(KeyValuePair pair, RemoteNode sender);

    /** Returns the k closest known nodes to the nodeId */
    RemoteNode[] findNode(HashKey nodeID, int k, RemoteNode sender);

    /** If node has the value it returns it. If not it returns the k closest known nodes to the id */
    RemoteNodesOrKeyValuePair findValue(HashKey valueID, int k, RemoteNode sender);
}
