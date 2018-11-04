import java.util.*;

public class Bucket {
    private static final int SIZE_LIMIT = 5;
    private Map<Boolean, Bucket> childs = new HashMap<>();
    private BitSet prefix; //Rev: This could also just be one bit
    private int level;

    private List<RemoteNode> nodes = new ArrayList<>();

    public Bucket(BitSet prefix, int level){
        this.prefix = prefix;
        this.level = level;

        assert (level + 1 >= prefix.length());
    }

    public Bucket(){
        this.prefix = new BitSet();
        this.level = -1;
    }

    public BitSet getPrefix() {
        return prefix;
    }

    public Bucket getResponsibleBucket(HashKey key){
        assert (key.matchesPrefix(this.prefix));

        boolean nextBit = key.getBit(level + 1); //Rev: Test. Off by one?

        if(childs.containsKey(nextBit))
            return childs.get(nextBit).getResponsibleBucket(key);
        else
            return this;
    }

    public boolean addNodeMaybe(RemoteNode node, HashKey splittablePrefixes){
        Bucket bucket = getResponsibleBucket(node.getNodeId());
        assert(!bucket.inSplittingProcess);

        if (!bucket.nodes.contains(node)) {
            bucket.nodes.add(node);

            //Is it over capacity?
            if (bucket.nodes.size() > SIZE_LIMIT) {
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

    public boolean containsNode(RemoteNode node){
        return getResponsibleBucket(node.getNodeId()).nodes.contains(node);
    }

    private boolean inSplittingProcess = false; //Just for assertion purposes. Remove?
    private boolean split(HashKey splittablePrefixes){
        assert (childs.size() == 0);
        assert (nodes.size() > SIZE_LIMIT); //TODO: Rev. spec! Can it also split before?

        inSplittingProcess = true;

        BitSet zero = (BitSet) prefix.clone();
        zero.clear(this.level + 1); //Rev: Off by one?
        assert (zero.length() <= HashKey.LENGTH);

        BitSet one = (BitSet) prefix.clone();
        one.set(this.level + 1); //Rev: Off by one?
        assert (one.length() <= HashKey.LENGTH);

        childs.put(false, new Bucket(zero, this.level + 1));
        childs.put(true, new Bucket(one, this.level + 1));

        //Add all nodes
        boolean addedAll = true;
        for(RemoteNode n : nodes){
            addedAll = addNodeMaybe(n, splittablePrefixes) && addedAll;
            //This can not be asserted!
            //In rare cases all nodes fall into the split that is not splittable
            //Then they are still over the limit and the last one cant be added
        }

        //ASSERTION START TODO: Remove?
        final boolean addedAllf = addedAll; //Needs to be effectively final
        Bucket theOneThatDidTheSplit = this;
        nodes.forEach(node -> {
            Bucket responsibleBucket = getResponsibleBucket(node.getNodeId());
            assert(responsibleBucket.nodes.contains(node) || !addedAllf);
            assert(responsibleBucket != theOneThatDidTheSplit);
        });
        //ASSERTION END

        nodes.clear();

        inSplittingProcess = false;
        return addedAll; //Returns whether or not all nodes have been kept
    }

    public List<RemoteNode> getNodesFromResponsibleBucket(HashKey key){
        return getResponsibleBucket(key).getAllNodes();
    }

    public List<RemoteNode> getAllNodes() {
        LinkedList<RemoteNode> out = new LinkedList<>();
        addNodesToList(out);
        return out;
    }

    private void addNodesToList(LinkedList<RemoteNode> list){
        list.addAll(nodes);
        childs.forEach((key, value) -> value.addNodesToList(list));
    }

    public void removeNode(RemoteNode node){
        getResponsibleBucket(node.getNodeId()).nodes.remove(node);
    }

    public int getBucketCount(){
        int sum = 1;

        for(Map.Entry<Boolean, Bucket> e : childs.entrySet())
            sum += e.getValue().getBucketCount();

        return  sum;
    }
}
