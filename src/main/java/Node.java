public class Node implements INode{
    private HashKey nodeID;

    public Node(RemoteNode knownNode){
        this();
        //Todo: Put knownNode into buckets
    }

    public Node(){
        nodeID = HashKey.fromRandom();
    }

    private void recordNode(RemoteNode other){
        //TODO: Put into buckets?
    }

    @Override
    public boolean ping(RemoteNode sender) {
        recordNode(sender);

        throw new RuntimeException("Remote calls not implemented yet");
    }

    @Override
    public void store(KeyValuePair pair, RemoteNode sender) {
        recordNode(sender);

        throw new RuntimeException("Remote calls not implemented yet");
    }

    @Override
    public RemoteNode[] findNode(HashKey nodeID, int k, RemoteNode sender) {
        recordNode(sender);
        //TODO: Cache nodes

        throw new RuntimeException("Remote calls not implemented yet");
    }

    @Override
    public RemoteNodesOrKeyValuePair findValue(HashKey valueID, int k, RemoteNode sender) {
        recordNode(sender);
        //TODO: Cache values

        throw new RuntimeException("Remote calls not implemented yet");
    }
}
