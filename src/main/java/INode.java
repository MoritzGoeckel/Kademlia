public interface INode {
    /** Returns true if the node is still reachable */
    boolean ping(INode sender);

    /** Instructs the node to store the KeyValuePair */
    void store(KeyValuePair pair, INode sender);

    /** Returns the k closest known nodes to the nodeId */
    INode[] findNodes(HashKey targetID, int k, INode sender);

    /** If node has the value it returns it. If not it returns the k closest known nodes to the id */
    RemoteNodesOrKeyValuePair findValue(HashKey targetValueID, int k, INode sender);

    HashKey getNodeId();

    int getPort();

    String getAddress();
}
