import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Node implements INode, IUserNode {
    private HashKey nodeID;
    private Bucket buckets = new Bucket();

    protected RemoteNode me;

    private HashMap<HashKey, String> values;
    //Todo: Values need to expire

    private boolean shutdown = false;

    private static int TIME_BETWEEN_PINGS = 6 * 60 * 1000; //Five minutes

    public Node(RemoteNode knownNode, int port, URL address){
        this(port, address);

        recordNode(knownNode);
        performNodeLookup(this.nodeID, 50);
    }

    public Node(int port, URL address){
        this.nodeID = HashKey.fromRandom();
        this.me = new RemoteNode(this.nodeID, port, address);
        this.values = new HashMap<>();

        startPingThread();
    }

    private void startPingThread(){
        Thread pingThread = new Thread(() -> {
            while (!shutdown) {
                List<RemoteNode> nodes = buckets.getAllNodes();
                nodes.forEach(n -> {
                    if (!n.ping(me))
                        buckets.removeNode(n);

                    try {
                        Thread.sleep(TIME_BETWEEN_PINGS / nodes.size());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
        pingThread.setDaemon(true);
        pingThread.start();
    }

    @Override
    public HashKey getID(){
        return nodeID;
    }

    private static Comparator<RemoteNode> getDistanceComparator(HashKey target){
        //Todo: Check correctness (smallest distance is head)
        return (a,b) -> (int)(a.getNodeId().getDistance(target) - b.getNodeId().getDistance(target));
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
        if(shutdown)
            return;

        recordNode(sender);

        //Node does not know if he is the closest,
        //he just stores it when instructed to
        values.put(pair.getKey(), pair.getValue());
    }

    @Override
    public RemoteNode[] findNodes(HashKey targetID, int k, RemoteNode sender) {
        if(shutdown)
            return null;

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
        if(shutdown)
            return null;

        recordNode(sender);
        //TODO: Cache values / Nodes

        if(values.containsKey(targetValueID))
            return new RemoteNodesOrKeyValuePair(new KeyValuePair(targetValueID, values.get(targetValueID)));
        else
            return new RemoteNodesOrKeyValuePair(findNodes(targetValueID, k, sender));
    }

    @Override
    public void setValue(KeyValuePair pair, int k) {
        if(shutdown)
            return;

        for(RemoteNode n : performNodeLookup(pair.getKey(), k))
            n.store(pair, me);
    }

    @Override
    public KeyValuePair getValue(HashKey target, int k, int maxIterations) {
        if(shutdown)
            return null;

        Set<RemoteNode> visitedNodes = new HashSet<>();
        TreeSet<RemoteNode> queuedNodes = new TreeSet<>(getDistanceComparator(target));

        queuedNodes.addAll(Arrays.asList(this.findNodes(target, k, null)));

        for (int iteration = 0; iteration < maxIterations && !queuedNodes.isEmpty(); iteration++){
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
                return response.getPair();
            }
        }

        return null;
    }

    private SortedSet<RemoteNode> performNodeLookup(HashKey target, int k){
        Set<RemoteNode> visitedNodes = new HashSet<>();
        TreeSet<RemoteNode> queuedNodes = new TreeSet<>(getDistanceComparator(target));
        TreeSet<RemoteNode> closestNodes = new TreeSet<>(getDistanceComparator(target));

        queuedNodes.addAll(Arrays.asList(this.findNodes(target, k, null)));
        closestNodes.addAll(queuedNodes);

        boolean gettingCloser = true;
        while (gettingCloser && !queuedNodes.isEmpty()){
            long distanceBeforeIteration = closestNodes.first().getNodeId().getDistance(target);

            RemoteNode currentNode = queuedNodes.pollFirst();
            visitedNodes.add(currentNode);

            for(RemoteNode n : currentNode.findNodes(target, k, me)) {
                if (!visitedNodes.contains(n))
                    queuedNodes.add(n);
                closestNodes.add(n);
                recordNode(n);
            }

            gettingCloser = closestNodes.first().getNodeId().getDistance(target) < distanceBeforeIteration;
        }

        return closestNodes.stream()
                .limit(k) //Return only the k closest elements
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public void shutdown(){
        shutdown = true;
    }

    public boolean isShutdown() {
        return shutdown;
    }
}
