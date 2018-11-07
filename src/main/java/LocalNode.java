import java.net.URL;

public class LocalNode extends RemoteNode implements INode {
    private final Node node;

    public LocalNode(Node node, int port, URL address) {
        super(node.getID(), port, address);
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
