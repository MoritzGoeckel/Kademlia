import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Node implements INode, IUserNode {
    private HashKey nodeID;
    private Bucket buckets;

    private LocalNode me;

    private HashMap<HashKey, String> values;
    //Todo: Values need to expire

    private boolean shutdown = false;

    private static int TIME_BETWEEN_PINGS = 6 * 60 * 1000; //Five minutes

    private Thread pingThread;

    private static NodeStatistics statistics = new NodeStatistics();
    public static NodeStatistics getStatistics(){
        return statistics;
    }

    public static void resetStatistics(){
        statistics = new NodeStatistics();
    }

    public Node(RemoteNode knownNode, int port, URL address, int storageLimit){
        this(port, address, storageLimit);

        recordNode(knownNode);
        performNodeLookup(this.nodeID, 50);
    }

    public Node(int port, URL address, int storageLimit){
        this.nodeID = HashKey.fromRandom();
        this.me = new LocalNode(this, port, address);
        this.values = new HashMap<>();
        this.buckets = new Bucket();

        //startPingThread(); //Todo: Remove?
    }

    public void startPingThread(){
        if(pingThread != null)
            throw new RuntimeException("Ping thread already running");

        pingThread = new Thread(() -> {
            while (!shutdown) {
                this.performPing();

                try {
                    Thread.sleep(TIME_BETWEEN_PINGS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        pingThread.setDaemon(true);
        pingThread.start();
    }

    public void performPing(){
        checkShutdown();

        List<RemoteNode> nodes = buckets.getAllNodes();
        nodes.forEach(n -> {
            if (!n.ping(me))
                buckets.removeNode(n);
        });
    }

    public HashKey getID(){
        return nodeID;
    }

    private static Comparator<RemoteNode> getDistanceComparator(HashKey target){
        return (a,b) -> (int)(a.getNodeId().getDistance(target).compareTo(b.getNodeId().getDistance(target)));
    }


    private void recordNode(RemoteNode other){
        if(other != me)
            buckets.addNodeMaybe(other, this.nodeID);
    }

    @Override
    public boolean ping(RemoteNode sender) {
        if(sender != me)
            statistics.recordEvent("ping");

        if(!shutdown)
            recordNode(sender);

        return !shutdown;
    }

    @Override
    public void store(KeyValuePair pair, RemoteNode sender) {
        checkShutdown();

        if(sender != me)
            statistics.recordEvent("store");

        recordNode(sender);

        //Node does not know if he is the closest,
        //he just stores it when instructed to
        values.put(pair.getKey(), pair.getValue());
    }

    @Override
    public RemoteNode[] findNodes(HashKey targetID, int k, RemoteNode sender) {
        checkShutdown();

        if(sender != me)
            statistics.recordEvent("findNodes");

        recordNode(sender);
        //TODO: Cache values / Nodes

        //Could also utilize the buckets for a faster find,
        //it would be hard however to expand the search for k instances
        //and as the number of all nodes in the buckets is small
        //this would not make much a difference
        RemoteNode[] output = buckets.getAllNodes().stream()
            .sorted(getDistanceComparator(targetID))
            .limit(k)
            .toArray(RemoteNode[]::new);

        if(output.length > 1) //Assertion
            assert (output[0].getNodeId().getDistance(targetID).compareTo(output[output.length - 1].getNodeId().getDistance(targetID)) < 0);

        return output;
    }

    @Override
    public RemoteNodesOrKeyValuePair findValue(HashKey targetValueID, int k, RemoteNode sender) {
        checkShutdown();

        if(sender != me)
            statistics.recordEvent("findValue");

        recordNode(sender);
        //TODO: Cache values / Nodes

        if(values.containsKey(targetValueID))
            return new RemoteNodesOrKeyValuePair(new KeyValuePair(targetValueID, values.get(targetValueID)));
        else
            return new RemoteNodesOrKeyValuePair(findNodes(targetValueID, k, me));
    }

    @Override
    public void setValue(String key, String value, int k) {
        checkShutdown();

        KeyValuePair pair = new KeyValuePair(HashKey.fromString(key), value);

        for(RemoteNode n : performNodeLookup(pair.getKey(), k))
            n.store(pair, me);
    }

    @Override
    public String getValue(String key, int k) {
        checkShutdown();

        HashKey target = HashKey.fromString(key);

        //This one does not have it, so lets do the lookup dance
        Set<RemoteNode> visitedNodes = new HashSet<>();
        TreeSet<RemoteNode> queuedNodes = new TreeSet<>(getDistanceComparator(target));

        queuedNodes.add(me);

        int MAX_ITERATIONS = Integer.MAX_VALUE;
        for (int iteration = 0; iteration < MAX_ITERATIONS && !queuedNodes.isEmpty(); iteration++){

            if(queuedNodes.size() > 1) //Assertion
                assert (queuedNodes.first().getNodeId().getDistance(target).compareTo(queuedNodes.last().getNodeId().getDistance(target)) < 0);

            RemoteNode currentNode = queuedNodes.pollFirst();
            visitedNodes.add(currentNode);

            RemoteNodesOrKeyValuePair response = currentNode.findValue(target, k, me);
            if(response.getRemoteNodes() != null) {
                for (RemoteNode n : response.getRemoteNodes()) {
                    if (!visitedNodes.contains(n))
                        queuedNodes.add(n);
                    recordNode(n);
                }
            }
            else{
                return response.getPair().getValue();
            }
        }

        return null;
    }

    private SortedSet<RemoteNode> performNodeLookup(HashKey target, int k){
        checkShutdown();

        Set<RemoteNode> visitedNodes = new HashSet<>();
        TreeSet<RemoteNode> queuedNodes = new TreeSet<>(getDistanceComparator(target));
        TreeSet<RemoteNode> closestNodes = new TreeSet<>(getDistanceComparator(target));

        queuedNodes.addAll(Arrays.asList(this.findNodes(target, 999999, me)));
        closestNodes.addAll(queuedNodes);

        boolean gettingCloser = true;
        while (gettingCloser && !queuedNodes.isEmpty()){

            //Assertion
            if(queuedNodes.size() > 1)
                assert (queuedNodes.first().getNodeId().getDistance(target).compareTo(queuedNodes.last().getNodeId().getDistance(target)) < 0);

            BigInteger distanceBeforeIteration = closestNodes.first().getNodeId().getDistance(target);

            RemoteNode currentNode = queuedNodes.pollFirst();
            visitedNodes.add(currentNode);

            for(RemoteNode n : currentNode.findNodes(target, k, me)) {
                if (!visitedNodes.contains(n))
                    queuedNodes.add(n);
                closestNodes.add(n);
                recordNode(n);
            }

            gettingCloser = closestNodes.first().getNodeId().getDistance(target).compareTo(distanceBeforeIteration) < 0;
        }

        //Assertion
        if(closestNodes.size() > 1)
            assert (closestNodes.first().getNodeId().getDistance(target).compareTo(closestNodes.last().getNodeId().getDistance(target)) < 0);

        //Lets throw oneself also into the mix :)
        closestNodes.add(me);
        return closestNodes.stream()
                .limit(k) //Return only the k closest elements
                .collect(Collectors.toCollection(() -> new TreeSet<>(getDistanceComparator(target))));
    }

    private void checkShutdown(){
        if(shutdown)
            throw new RuntimeException("Already shut down");
    }

    public void shutdown(){
        checkShutdown();
        shutdown = true;
    }

    public boolean isShutdown() {
        return shutdown;
    }
}
