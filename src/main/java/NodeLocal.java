import java.net.URL;

public class NodeLocal extends Node {

    public NodeLocal(RemoteNode knownNode, int port, URL address) {
        super(knownNode, port, address);
        me = new RemoteNodeLocal(port, address, this);
    }

    public NodeLocal(int port, URL address) {
        super(port, address);
        me = new RemoteNodeLocal(port, address, this);
    }
}
