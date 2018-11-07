import java.net.URL;

public class RemoteNode implements INode {
    private final HashKey nodeId;
    private final int port;
    private final URL address;

    public RemoteNode(HashKey nodeId, int port, URL address){
        this.nodeId = nodeId;
        this.port = port;
        this.address = address;
    }

    public HashKey getNodeId() {
        return nodeId;
    }

    public int getPort() {
        return port;
    }

    public URL getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RemoteNode
                && getNodeId().equals(((RemoteNode)obj).getNodeId());
    }

    @Override
    public boolean ping(RemoteNode sender) {
        //TODO: Implement remote calls
        throw new RuntimeException("Remote calls not implemented yet");
    }

    @Override
    public void store(KeyValuePair pair, RemoteNode sender) {
        //TODO: Implement remote calls
        throw new RuntimeException("Remote calls not implemented yet");
    }

    @Override
    public RemoteNode[] findNodes(HashKey targetID, int k, RemoteNode sender) {
        //TODO: Implement remote calls
        throw new RuntimeException("Remote calls not implemented yet");
    }

    @Override
    public RemoteNodesOrKeyValuePair findValue(HashKey targetValueID, int k, RemoteNode sender) {
        //TODO: Implement remote calls
        throw new RuntimeException("Remote calls not implemented yet");
    }
}
