import java.net.URL;

public class RemoteNodeLocal extends RemoteNode implements INode {
    private final Node node;

    public RemoteNodeLocal(int port, URL ipAddress, Node node) {
        super(node.getID(), port, ipAddress);
        this.node = node;
    }

    @Override
    public boolean ping(RemoteNode sender) {
        return node.ping(sender);
    }

    @Override
    public void store(KeyValuePair pair, RemoteNode sender) {
        node.store(pair, sender);
    }

    @Override
    public RemoteNode[] findNodes(HashKey targetID, int k, RemoteNode sender) {
        return node.findNodes(targetID, k, sender);
    }

    @Override
    public RemoteNodesOrKeyValuePair findValue(HashKey targetValueID, int k, RemoteNode sender) {
        return node.findValue(targetValueID, k, sender);
    }
}