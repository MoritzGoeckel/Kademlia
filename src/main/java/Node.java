import java.net.URL;
import java.util.List;

public class Node implements INode{
    private HashKey nodeID;
    private Bucket buckets = new Bucket();
    private Thread pingThread;

    private RemoteNode me;

    public Node(RemoteNode knownNode, int port, URL address){
        this(port, address);

        recordNode(knownNode);
        //Todo: Locate some more nodes
    }

    public Node(int port, URL address){
        this.me = new RemoteNode(this.nodeID, port, address);
        this.nodeID = HashKey.fromRandom();

        startPingThread();
    }

    private void startPingThread(){
        pingThread = new Thread(() -> {
            while (true) {
                List<RemoteNode> nodes = buckets.getAllNodes();
                nodes.forEach(n -> {
                    if (!n.ping(me))
                        buckets.removeNode(n);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
        pingThread.setDaemon(true);
        pingThread.start();
    }

    private void recordNode(RemoteNode other){
        buckets.addNodeMaybe(other, this.nodeID);
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
