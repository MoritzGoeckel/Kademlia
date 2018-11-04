import java.net.URL;
import java.util.*;

public class Node implements INode, IUserNode{
    private HashKey nodeID;
    private Bucket buckets = new Bucket();
    private Thread pingThread;

    private RemoteNode me;

    private HashMap<HashKey, String> values;
    //Todo: Values need to expire

    public Node(RemoteNode knownNode, int port, URL address){
        this(port, address);

        recordNode(knownNode);
        //Todo: Locate some more nodes
    }

    public Node(int port, URL address){
        this.me = new RemoteNode(this.nodeID, port, address);
        this.nodeID = HashKey.fromRandom();
        this.values = new HashMap<>();

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
        if(other != me && other != null)
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

        //Node does not know if he is the closest,
        //he just stores it when instructed to
        values.put(pair.getKey(), pair.getValue());
    }

    @Override
    public RemoteNode[] findNodes(HashKey targetID, int k, RemoteNode sender) {
        recordNode(sender);
        //TODO: Cache values / Nodes

        //Could also utilize the buckets for a faster find,
        //it would be hard however to expand the search for k instances
        //and as the number of all nodes in the buckets is small
        //this would not make much a difference
        return buckets.getAllNodes().stream()
            .sorted(
                //TODO: Correct order?
                (a,b) -> (int)(a.getNodeId().getDistance(targetID) - b.getNodeId().getDistance(targetID))
            )
            .limit(k)
            .toArray(RemoteNode[]::new);
    }

    @Override
    public RemoteNodesOrKeyValuePair findValue(HashKey targetValueID, int k, RemoteNode sender) {
        recordNode(sender);
        //TODO: Cache values / Nodes

        if(values.containsKey(targetValueID))
            return new RemoteNodesOrKeyValuePair(new KeyValuePair(targetValueID, values.get(targetValueID)));
        else
            return new RemoteNodesOrKeyValuePair(findNodes(targetValueID, k, sender));
    }

    @Override
    public void setValue(KeyValuePair pair) {
        SortedSet<RemoteNode> closestNodes = new TreeSet<>(
                //Supplying a comparator
                //TODO: Correct order?
                (a,b) -> (int)(a.getNodeId().getDistance(pair.getKey()) - b.getNodeId().getDistance(pair.getKey()))
        );

        closestNodes.add(me);

        boolean gettingCloser = true;
        while (gettingCloser){
            //closestNodes.addAll(Arrays.asList(closestNodes.first().findNodes(pair.getKey(), 10, me)));

            //TODO: Ask everyone only once. When is it close enough?
        }

        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public KeyValuePair getValue(HashKey id) {
        //TODO
        //Quite similar to setValue
        throw new RuntimeException("Not implemented yet");
    }
}
