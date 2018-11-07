import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Node implements INode, IUserNode {
    private HashKey nodeID;
    private Bucket buckets = new Bucket();

    protected RemoteNodeLocal me;

    private HashMap<HashKey, String> values;
    //Todo: Values need to expire

    private boolean shutdown = false;

    private static int TIME_BETWEEN_PINGS = 6 * 60 * 1000; //Five minutes

    private Thread pingThread;

    public Node(RemoteNode knownNode, int port, URL address){
        this(port, address);

        recordNode(knownNode);
        performNodeLookup(this.nodeID, 50);
    }

    public Node(int port, URL address){
        this.nodeID = HashKey.fromRandom();
        this.me = new RemoteNodeLocal(this, port, address);
        this.values = new HashMap<>();

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
        //Todo: Check correctness (smallest distance is head)
        return (a,b) -> (int)(a.getNodeId().getDistance(target).compareTo(b.getNodeId().getDistance(target)));
    }


    private void recordNode(RemoteNode other){
        if(other != me && other != null)
            buckets.addNodeMaybe(other, this.nodeID);
    }

    @Override
    public boolean ping(RemoteNode sender) {
        recordNode(sender);
        return !shutdown;
    }

    @Override
    public void store(KeyValuePair pair, RemoteNode sender) {
        checkShutdown();

        recordNode(sender);

        //Node does not know if he is the closest,
        //he just stores it when instructed to
        values.put(pair.getKey(), pair.getValue());
    }

    @Override
    public RemoteNode[] findNodes(HashKey targetID, int k, RemoteNode sender) {
        checkShutdown();

        recordNode(sender);
        //TODO: Cache values / Nodes

        //Could also utilize the buckets for a faster find,
        //it would be hard however to expand the search for k instances
        //and as the number of all nodes in the buckets is small
        //this would not make much a difference
        return buckets.getAllNodes().stream()
            .sorted(getDistanceComparator(targetID))
            .limit(k)
            .toArray(RemoteNode[]::new);
    }

    @Override
    public RemoteNodesOrKeyValuePair findValue(HashKey targetValueID, int k, RemoteNode sender) {
        checkShutdown();

        recordNode(sender);
        //TODO: Cache values / Nodes

        if(values.containsKey(targetValueID))
            return new RemoteNodesOrKeyValuePair(new KeyValuePair(targetValueID, values.get(targetValueID)));
        else
            return new RemoteNodesOrKeyValuePair(findNodes(targetValueID, k, sender));
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

        //Does this node have the value?
        //Todo: Should regard me as regular node too. Would be more elegant
        RemoteNodesOrKeyValuePair myResponse = this.findValue(target, k, null);
        if(myResponse.getPair() != null)
            return myResponse.getPair().getValue();

        //This one does not have it, so lets do the lookup dance
        Set<RemoteNode> visitedNodes = new HashSet<>();
        TreeSet<RemoteNode> queuedNodes = new TreeSet<>(getDistanceComparator(target));

        queuedNodes.addAll(Arrays.asList(myResponse.getRemoteNodes()));

        int MAX_ITERATIONS = Integer.MAX_VALUE;
        for (int iteration = 0; iteration < MAX_ITERATIONS && !queuedNodes.isEmpty(); iteration++){

            if(queuedNodes.size() > 1)
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

        queuedNodes.addAll(Arrays.asList(this.findNodes(target, k, null)));
        closestNodes.addAll(queuedNodes);
        closestNodes.add(me);

        boolean gettingCloser = true;
        while (gettingCloser && !queuedNodes.isEmpty()){
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

        if(closestNodes.size() > 1)
            assert (closestNodes.first().getNodeId().getDistance(target).compareTo(closestNodes.last().getNodeId().getDistance(target)) < 0);

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
