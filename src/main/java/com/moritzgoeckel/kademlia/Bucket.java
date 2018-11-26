package com.moritzgoeckel.kademlia;

import java.util.*;

/** Implementation of the Kademlia Bucket*/
class Bucket {

    /** Maximum number of nodes stored for every bit in the HashKey */
    private int storageLimitPerLayer;

    /** Splittable buckets have two children (one Bucket for next bit is TRUE, one Bucket for next bit is FALSE)
     * Many buckets are not splittable and do not have children
     * A bucket is only splittable if it is a prefix of the current Node (the node owning all the buckets here)*/
    private Map<Boolean, Bucket> children = new HashMap<>();

    /** Depth of the bucket in the tree of buckets */
    private int level;

    private BitSet prefix; //Rev: This could also just be one bit
    private List<INode> nodes = new ArrayList<>();

    Bucket(BitSet prefix, int level, int storageLimitPerLayer){
        this.prefix = prefix;
        this.level = level;
        this.storageLimitPerLayer = storageLimitPerLayer;

        assert (level + 1 >= prefix.length());
    }

    Bucket(int storageLimitPerLayer){
        this.storageLimitPerLayer = storageLimitPerLayer;
        this.prefix = new BitSet();
        this.level = -1;
    }

    synchronized BitSet getPrefix() {
        return prefix;
    }

    /** Recursively finds the bucket that is responsible for holding a node with the given key */
    synchronized Bucket getResponsibleBucket(HashKey key){
        assert (key.matchesPrefix(this.prefix));

        boolean nextBit = key.getBit(level + 1); //Rev: Test. Off by one?

        if(children.containsKey(nextBit))
            return children.get(nextBit).getResponsibleBucket(key);
        else
            return this;
    }

    /** Adds the node to the responsible bucket
     * If the bucket is already full and not splittable, the node is not added
     * Returns whether or not the node has been added */
    synchronized boolean addNodeMaybe(INode node, HashKey splittablePrefixes) {
        Bucket bucket = getResponsibleBucket(node.getNodeId());
        assert(!bucket.inSplittingProcess);

        if (!bucket.nodes.contains(node)) {
            bucket.nodes.add(node);

            //Is it over capacity?
            if (bucket.nodes.size() > storageLimitPerLayer) {
                //Can it split?
                if (splittablePrefixes.matchesPrefix(bucket.prefix) && bucket.level < HashKey.LENGTH)
                    return bucket.split(splittablePrefixes);
                else {
                    //Remove the just added node again, as this bucket
                    //is not splittable and has too many nodes
                    bucket.nodes.remove(node);
                    return false;
                }
            }

            return true;
        }
        else {
            return false;
        }
    }

    /** Returns true if the node is in the tree of buckets */
    synchronized boolean containsNode(INode node) {
        return getResponsibleBucket(node.getNodeId()).nodes.contains(node);
    }

    private boolean inSplittingProcess = false; //This variable only exists for assertion purposes. TODO: Remove?

    /** Splits the current bucket */
    private synchronized boolean split(HashKey splittablePrefixes) {
        assert (children.size() == 0);
        assert (nodes.size() > storageLimitPerLayer); //TODO: Rev. spec! Can it also split before?

        inSplittingProcess = true;

        BitSet zero = (BitSet) prefix.clone();
        zero.clear(this.level + 1); //Rev: Off by one?
        assert (zero.length() <= HashKey.LENGTH);

        BitSet one = (BitSet) prefix.clone();
        one.set(this.level + 1); //Rev: Off by one?
        assert (one.length() <= HashKey.LENGTH);

        children.put(false, new Bucket(zero, this.level + 1, storageLimitPerLayer));
        children.put(true, new Bucket(one, this.level + 1, storageLimitPerLayer));

        //Add all nodes
        boolean addedAll = true;
        for(INode n : nodes){
            addedAll = addNodeMaybe(n, splittablePrefixes) && addedAll;
            //addedAll can not be asserted!
            //In rare cases all nodes fall into the split that is not splittable
            //Then they are still over the limit and the last one cant be added
        }

        //ASSERTION START TODO: Remove?
        final boolean addedAllf = addedAll; //Needs to be effectively final
        Bucket theOneThatDidTheSplit = this;
        nodes.forEach(node -> {
            Bucket responsibleBucket = null;
            responsibleBucket = getResponsibleBucket(node.getNodeId());
            assert(responsibleBucket.nodes.contains(node) || !addedAllf);
            assert(responsibleBucket != theOneThatDidTheSplit);
        });
        //ASSERTION END

        nodes.clear();

        inSplittingProcess = false;
        return addedAll; //Returns whether or not all nodes have been kept
    }

    /** Returns the list of nodes from the bucket that is responsible for a given key*/
    synchronized List<INode> getNodesFromResponsibleBucket(HashKey key){
        return getResponsibleBucket(key).getAllNodes();
    }

    /** Returns all nodes from the entire tree of buckets */
    synchronized List<INode> getAllNodes() {
        LinkedList<INode> out = new LinkedList<>();
        addNodesToList(out);
        return out;
    }

    private synchronized void addNodesToList(LinkedList<INode> list){
        list.addAll(nodes);
        children.forEach((key, value) -> value.addNodesToList(list));
    }

    /** Removes a given node from the entire tree of buckets */
    synchronized void removeNode(INode node) {
        getResponsibleBucket(node.getNodeId()).nodes.remove(node);
    }

    /** Returns the number of nodes in the entire tree of buckets */
    synchronized int getBucketCount(){
        int sum = 1;

        for(Map.Entry<Boolean, Bucket> e : children.entrySet())
            sum += e.getValue().getBucketCount();

        return  sum;
    }
}
