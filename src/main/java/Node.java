import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;

public class Node implements INode, IUserNode, IRemoteNode {
    private HashKey nodeID;
    private Bucket buckets;

    private HashMap<HashKey, String> values;
    //Todo: Values need to expire

    private boolean shutdown = false;

    private static int TIME_BETWEEN_PINGS = 6 * 60 * 1000; //Five minutes

    private Thread pingThread;

    private int port;
    private String address;

    private INode meForOthers;

    private static NodeStatistics statistics = new NodeStatistics();
    public static NodeStatistics getStatistics(){
        return statistics;
    }

    public static void resetStatistics(){
        statistics = new NodeStatistics();
    }

    public Node(INode knownNode, int port, String address, int storageLimit, boolean expose){
        this(port, address, storageLimit, expose);

        recordNode(knownNode);
        performNodeLookup(this.nodeID, 50);
    }

    public Node(int port, String address, int storageLimit, boolean expose){
        this.port = port;
        this.address = address;
        this.nodeID = HashKey.fromRandom();
        this.values = new HashMap<>();
        this.buckets = new Bucket(storageLimit);

        if(expose) {
            try {
                //Create registry if necessary
                Registry registry;
                try {
                    registry = LocateRegistry.getRegistry(this.getPort());
                    registry.list();
                } catch (RemoteException e){
                    registry = LocateRegistry.createRegistry(this.getPort());
                }

                //Expose the LocalNode
                IRemoteNode myStub = (IRemoteNode) UnicastRemoteObject.exportObject(this, this.getPort());
                registry.rebind("kademliaNode", myStub);
                meForOthers = new RemoteNode(address, port, nodeID);
            } catch (RemoteException e) {
                e.printStackTrace();
                throw new RuntimeException("Exposing exception");
            }
        }
        else {
            meForOthers = this;
        }

        //startPingThread(); //Todo: Remove?
    }

    @Override
    public int getPort(){
        return port;
    }

    @Override
    public String getAddress() {
        return address;
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

        List<INode> nodes = buckets.getAllNodes();
        nodes.forEach(n -> {
            if (!n.ping(meForOthers))
                buckets.removeNode(n);
        });
    }

    private static Comparator<INode> getDistanceComparator(HashKey target){
        return (a,b) -> (int)(a.getNodeId().getDistance(target).compareTo(b.getNodeId().getDistance(target)));
    }


    private void recordNode(INode other){
        if(other != this)
            buckets.addNodeMaybe(other, this.nodeID);
    }

    @Override
    public boolean ping(INode sender) {
        if(sender != meForOthers)
            statistics.recordEvent("ping");

        if(!shutdown)
            recordNode(sender);

        return !shutdown;
    }

    @Override
    public void store(KeyValuePair pair, INode sender) {
        checkShutdown();

        if(sender != meForOthers)
            statistics.recordEvent("store");

        recordNode(sender);

        //Node does not know if he is the closest,
        //he just stores it when instructed to
        values.put(pair.getKey(), pair.getValue());
    }

    @Override
    public INode[] findNodes(HashKey targetID, int k, INode sender) {
        checkShutdown();

        if(sender != meForOthers)
            statistics.recordEvent("findNodes");

        recordNode(sender);
        //TODO: Cache values / Nodes

        //Could also utilize the buckets for a faster find,
        //it would be hard however to expand the search for k instances
        //and as the number of all nodes in the buckets is small
        //this would not make much a difference
        INode[] output = buckets.getAllNodes().stream()
            .sorted(getDistanceComparator(targetID))
            .limit(k)
            //.map(n -> new RemoteNode(n.getAddress(), n.getPort(), n.getNodeId())) // We dont need to convert it, as only remotes should be in there anyways
            .toArray(INode[]::new);

        assert output.length <= 1 || (output[0].getNodeId().getDistance(targetID).compareTo(output[output.length - 1].getNodeId().getDistance(targetID)) < 0);

        return output;
    }

    @Override
    public RemoteNodesOrKeyValuePair findValue(HashKey targetValueID, int k, INode sender) {
        checkShutdown();

        if(sender != meForOthers)
            statistics.recordEvent("findValue");

        recordNode(sender);
        //TODO: Cache values / Nodes

        if(values.containsKey(targetValueID))
            return new RemoteNodesOrKeyValuePair(new KeyValuePair(targetValueID, values.get(targetValueID)));
        else
            return new RemoteNodesOrKeyValuePair(findNodes(targetValueID, k, meForOthers));
    }

    @Override
    public HashKey getNodeId() {
        return nodeID;
    }

    @Override
    public void setValue(String key, String value, int k) {
        checkShutdown();

        KeyValuePair pair = new KeyValuePair(HashKey.fromString(key), value);

        for(INode n : performNodeLookup(pair.getKey(), k))
            n.store(pair, meForOthers);
    }

    @Override
    public String getValue(String key, int k) {
        checkShutdown();

        HashKey target = HashKey.fromString(key);

        //This one does not have it, so lets do the lookup dance
        Set<INode> visitedNodes = new HashSet<>();
        TreeSet<INode> queuedNodes = new TreeSet<>(getDistanceComparator(target));

        queuedNodes.add(this);

        int MAX_ITERATIONS = Integer.MAX_VALUE;
        for (int iteration = 0; iteration < MAX_ITERATIONS && !queuedNodes.isEmpty(); iteration++){

            assert queuedNodes.size() <= 1 || (queuedNodes.first().getNodeId().getDistance(target).compareTo(queuedNodes.last().getNodeId().getDistance(target)) < 0);

            INode currentNode = queuedNodes.pollFirst();
            visitedNodes.add(currentNode);

            RemoteNodesOrKeyValuePair response = currentNode.findValue(target, k, meForOthers);
            if(response.getRemoteNodes() != null) {
                for (INode n : response.getRemoteNodes()) {
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

    private SortedSet<INode> performNodeLookup(HashKey target, int k){
        checkShutdown();

        Set<INode> visitedNodes = new HashSet<>();
        TreeSet<INode> queuedNodes = new TreeSet<>(getDistanceComparator(target));
        TreeSet<INode> closestNodes = new TreeSet<>(getDistanceComparator(target));

        queuedNodes.addAll(Arrays.asList(this.findNodes(target, 999999, meForOthers)));
        closestNodes.addAll(queuedNodes);

        boolean gettingCloser = true;
        while (gettingCloser && !queuedNodes.isEmpty()){

            //Assertion
            assert queuedNodes.size() <= 1 || (queuedNodes.first().getNodeId().getDistance(target).compareTo(queuedNodes.last().getNodeId().getDistance(target)) < 0);

            BigInteger distanceBeforeIteration = closestNodes.first().getNodeId().getDistance(target);

            INode currentNode = queuedNodes.pollFirst();
            visitedNodes.add(currentNode);

            //Todo: Perform this in parallel
            for(INode n : currentNode.findNodes(target, k, meForOthers)) {
                if (!visitedNodes.contains(n))
                    queuedNodes.add(n);
                closestNodes.add(n);
                recordNode(n);
            }

            gettingCloser = closestNodes.first().getNodeId().getDistance(target).compareTo(distanceBeforeIteration) < 0;
        }

        //Assertion
        assert closestNodes.size() <= 1 || (closestNodes.first().getNodeId().getDistance(target).compareTo(closestNodes.last().getNodeId().getDistance(target)) < 0);

        //Lets throw oneself also into the mix :)
        closestNodes.add(this);

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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof INode
                && getNodeId().equals(((INode)obj).getNodeId());
    }
}
