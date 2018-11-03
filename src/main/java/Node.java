import java.util.BitSet;

public class Node implements INode{
    private HashKey nodeID;
    private Bucket buckets = new Bucket();

    public Node(RemoteNode knownNode){
        this();
        recordNode(knownNode);
        //Todo: Locate some more nodes
        //Todo: Ping nodes regularly
    }

    public Node(){
        nodeID = HashKey.fromRandom();
    }

    private void recordNode(RemoteNode other){
        buckets.addNodeMaybe(other, this.nodeID);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public boolean ping(RemoteNode sender) {
        recordNode(sender);
        return true;
    }

    @Override
    public void store(KeyValuePair pair, RemoteNode sender) {
        recordNode(sender);

        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public RemoteNode[] findNode(HashKey nodeID, int k, RemoteNode sender) {
        recordNode(sender);
        //TODO: Cache nodes

        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public RemoteNodesOrKeyValuePair findValue(HashKey valueID, int k, RemoteNode sender) {
        recordNode(sender);
        //TODO: Cache values

        throw new RuntimeException("Not implemented yet");
    }
}
