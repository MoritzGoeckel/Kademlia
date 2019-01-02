package com.moritzgoeckel.kademlia;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Implementation of a Kademlia node */
public class Node implements INode, KademliaAPI, RMIExposedNode {

    private static int TIME_BETWEEN_PINGS = 6 * 60 * 1000; //Five minutes
    private static ExecutorService threadExecutor = Executors.newCachedThreadPool();

    private HashKey nodeID;
    private Bucket buckets;
    private boolean shutdown = false;
    private Future pingFuture;
    private int port;
    private String address;
    private INode localNode;

    private Cache<HashKey, String> values;

    private static NodeStatistics statistics = new NodeStatistics();
    static NodeStatistics getStatistics(){
        return statistics;
    }
    static void resetStatistics(){
        statistics = new NodeStatistics();
    }

    int getStateStatistics(){
        return buckets.getAllNodes().size();
    }

    @Override
    public int getPort(){
        return port;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public HashKey getNodeId() {
        return nodeID;
    }

    /** Constructor for nodes joining a network */
    public Node(INode knownNode, int port, String address, int storageLimit){
        this(knownNode, port, address, storageLimit, true, true);
    }

    /** Constructor for the initial node */
    public Node(int port, String address, int storageLimit){
        this(port, address, storageLimit, true, true);
    }

    /**
     * Constructor for nodes joining a network
     * This constructor is only for debugging purposes and is not
     * part of the user interface. Threading and rmi exposure can
     * be set manually
     * */
    Node(INode knownNode, int port, String address, int storageLimit, boolean exposeRMI, boolean useThreading){
        this(port, address, storageLimit, exposeRMI, useThreading);

        recordNode(knownNode);
        performNodeLookup(this.nodeID, storageLimit * 160 * 2);
    }

    /**
     * Constructor for the initial node
     * This constructor is only for debugging purposes and is not
     * part of the user interface. Threading and rmi exposure can
     * be set manually
     * */
    Node(int port, String address, int storageLimit, boolean exposeRMI, boolean useThreading){
        this.port = port;
        this.address = address;
        this.nodeID = HashKey.fromString(address +"/"+ port);
        this.buckets = new Bucket(storageLimit);

        //Using a hash map that allows for expiring entries (by the help of guava)
        this.values = CacheBuilder.newBuilder()
                .maximumSize(1000 * 1000)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build();

        if(exposeRMI) {
            exposeRMI();
            localNode = new RMIConnectedNode(address, port); //Create wrapper to use as sender
        }
        else {
            localNode = this; //Use a direct reference as sender
        }

        if(useThreading){
            pingFuture = threadExecutor.submit(() -> {
                while (!shutdown) {
                    this.performPing();
                    try {
                        Thread.sleep(TIME_BETWEEN_PINGS);
                    } catch (InterruptedException e) { }
                }
            });
        }
    }

    /** Exposes this node to the network via RMI. If no RMI registry exists one gets created */
    private void exposeRMI(){
        try {
            //Create registry if necessary
            Registry registry;
            try {
                registry = LocateRegistry.getRegistry(this.getPort());
                registry.list();
            } catch (RemoteException e){
                registry = LocateRegistry.createRegistry(this.getPort());
            }

            //Expose this node
            RMIExposedNode exposedStub = (RMIExposedNode) UnicastRemoteObject.exportObject(this, this.getPort());
            registry.rebind("kademliaNode", exposedStub);
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new RuntimeException("Exposing exception");
        }
    }

    /** Performs a maintenance ping and removes unavailable nodes from the buckets */
    public void performPing(){
        checkShutdown();

        List<INode> nodes = buckets.getAllNodes();
        nodes.forEach(n -> {
            if (!n.ping(localNode))
                buckets.removeNode(n);
        });
    }

    private void checkShutdown(){
        if(shutdown)
            throw new RuntimeException("Already shut down");
    }

    /** Shuts down the node */
    public void shutdown(){
        shutdown = true;
        if(pingFuture != null && !pingFuture.isCancelled() && !pingFuture.isDone())
            pingFuture.cancel(true);
    }

    public boolean isShutdown() {
        return shutdown;
    }

    /** Provides a shorthand for sorting HashKeys by distance to target HashKey */
    private static Comparator<INode> getDistanceComparator(HashKey target){
        return (a,b) -> (int)(a.getNodeId().getDistance(target).compareTo(b.getNodeId().getDistance(target)));
    }

    /**
     * Maybe records a node in the buckets
     * This is not guaranteed, as the bucket might be full
     * */
    private void recordNode(INode other){
        if(!other.equals(this) && !other.equals(localNode))
            buckets.addNodeMaybe(other, this.nodeID);
    }

    /**
     * The ping method that is exposed to other nodes on the network.
     * Returns true if the receiving node is reachable
     * */
    @Override
    public boolean ping(INode sender) {
        if(isShutdown())
            return false;

        if(!localNode.equals(sender))
            statistics.recordEvent("ping");

        recordNode(sender);
        return true;
    }

    /**
     * The store method that is exposed to other nodes on the network.
     * The given key value pair will be stored on the receiving node.
     * Returns whether or not the operation was successful
     * */
    @Override
    public boolean store(KeyValuePair pair, INode sender) {
        if(isShutdown())
            return false;

        if(!localNode.equals(sender))
            statistics.recordEvent("store");

        recordNode(sender);

        //com.moritzgoeckel.kademlia.Node does not know if he is the closest,
        //he just stores it when instructed to
        values.put(pair.getKey(), pair.getValue());

        return true;
    }

    /**
     * The findNodes method that is exposed to other nodes on the network.
     * Returns the k closest nodes to the target HashKey in the buckets of the receiving node
     * TODO: Caching?
     * */
    @Override
    public INode[] findNodes(HashKey targetID, int k, INode sender) {
        if(isShutdown())
            return null;

        if(!localNode.equals(sender))
            statistics.recordEvent("findNodes");

        recordNode(sender);

        //Could also utilize the buckets for a faster find,
        //it would be hard however to expand the search for k instances
        //and as the number of all nodes in the buckets is small
        //this would not make much a difference
        INode[] output = buckets.getAllNodes().stream()
            .sorted(getDistanceComparator(targetID))
            .limit(k)
            //.map(n -> new RMIConnectedNode(n.getAddress(), n.getPort(), n.getNodeId())) // We dont need to convert it, as only remotes should be in there anyways
            .toArray(INode[]::new);

        assert output.length <= 1 || (output[0].getNodeId().getDistance(targetID).compareTo(output[output.length - 1].getNodeId().getDistance(targetID)) < 0);

        return output;
    }

    /**
     * The findValue method that is exposed to other nodes on the network.
     * Returns either the requested value if present at the node
     * Or returns the k closest nodes to the target HashKey in the buckets of the receiving node
     * TODO: Caching?
     * */
    @Override
    public RemoteNodesOrKeyValuePair findValue(HashKey targetValueID, int k, INode sender) {
        if(isShutdown())
            return new RemoteNodesOrKeyValuePair(new INode[]{});

        if(!localNode.equals(sender))
            statistics.recordEvent("findValue");

        recordNode(sender);

        String value = values.getIfPresent(targetValueID);
        if(value != null)
            return new RemoteNodesOrKeyValuePair(new KeyValuePair(targetValueID, value));
        else
            return new RemoteNodesOrKeyValuePair(findNodes(targetValueID, k, localNode));
    }

    /**
     * The setValue method that is exposed to the user
     * It stores a key with it's value on k nodes in the network
     * */
    @Override
    public void setValue(String key, String value, int k) {
        checkShutdown();

        KeyValuePair pair = new KeyValuePair(HashKey.fromString(key), value);

        Set<INode> closestNodes = performNodeLookup(pair.getKey(), k);

        int successfullyStored = 0;
        for(INode n : closestNodes) {
            if(n.store(pair, localNode))
                successfullyStored++;

            if(successfullyStored >= k)
                break;
        }
    }

    /**
     * The getValue method that is exposed to the user
     * It queries the network for the value of a key
     *
     * The higher k, the higher the chance to find the value
     * and the higher the traffic on the network
     * */
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

            RemoteNodesOrKeyValuePair response = currentNode.findValue(target, k, localNode);
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

    /**
     * Performs a lookup for the closest nodes to a given HashKey on the network
     * The higher k, the higher the network traffic
     * TODO: Perform in parallel
     * */
    private SortedSet<INode> performNodeLookup(HashKey target, int k){
        checkShutdown();

        Set<INode> visitedNodes = new HashSet<>();
        TreeSet<INode> queuedNodes = new TreeSet<>(getDistanceComparator(target));
        TreeSet<INode> closestNodes = new TreeSet<>(getDistanceComparator(target));

        queuedNodes.addAll(Arrays.asList(this.findNodes(target, 999999, localNode)));
        closestNodes.addAll(queuedNodes);

        //The lookup is over as soon it did not find a closer node GETTING_CLOSER_INITIAL times
        final int GETTING_CLOSER_INITIAL = 5;

        int gettingCloser = GETTING_CLOSER_INITIAL;
        while (gettingCloser > 0 && !queuedNodes.isEmpty()){

            assert queuedNodes.size() <= 1 || (queuedNodes.first().getNodeId().getDistance(target).compareTo(queuedNodes.last().getNodeId().getDistance(target)) < 0);

            BigInteger distanceBeforeIteration = closestNodes.first().getNodeId().getDistance(target);

            INode currentNode = queuedNodes.pollFirst();
            visitedNodes.add(currentNode);

            INode[] nodes = currentNode.findNodes(target, k, localNode);
            if(nodes != null) {
                for (INode n : nodes) {
                    if (!visitedNodes.contains(n))
                        queuedNodes.add(n);
                    closestNodes.add(n);
                    recordNode(n);
                }

                if (closestNodes.first().getNodeId().getDistance(target).compareTo(distanceBeforeIteration) < 0){
                    //Found a closer one, set counter to 3
                    gettingCloser = GETTING_CLOSER_INITIAL;
                }
                else{
                    //Did not find a closer one, reduce counter
                    gettingCloser -= 1;
                }
            }
        }

        assert closestNodes.size() <= 1 || (closestNodes.first().getNodeId().getDistance(target).compareTo(closestNodes.last().getNodeId().getDistance(target)) < 0);

        //Lets throw oneself also into the mix :)
        closestNodes.add(this);

        return closestNodes;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof INode
                && getNodeId().equals(((INode)obj).getNodeId()));
    }
}
