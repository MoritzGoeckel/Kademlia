import java.util.*;

public class Bucket {
    private static final int SIZE_LIMIT = 5;
    private Map<Boolean, Bucket> childs = new HashMap<>();
    private BitSet prefix;
    private int level;

    private List<RemoteNode> nodes = new ArrayList<>();

    public Bucket(BitSet prefix, int level){
        this.prefix = prefix;
        this.level = level;

        assert (level >= prefix.length());
    }

    public Bucket(){
        this.prefix = new BitSet();
        this.level = 0;
    }

    public BitSet getPrefix() {
        return prefix;
    }

    public Bucket getResponsibleBucket(HashKey key){
        assert (key.matchesPrefix(this.prefix));

        Boolean nextBit = key.getBit(getPrefix().length()); //TODO: Test. Off by one?

        if(childs.containsKey(nextBit))
            return childs.get(nextBit).getResponsibleBucket(key);
        else
            return this;
    }

    public boolean addNodeMaybe(RemoteNode node, HashKey splittablePrefixes){
        Bucket bucket = getResponsibleBucket(node.getNodeId());

        if (!bucket.nodes.contains(node)) {
            bucket.nodes.add(node);

            //Is it over capacity?
            if (bucket.nodes.size() > SIZE_LIMIT) {
                //Can it split?
                if (splittablePrefixes.matchesPrefix(bucket.prefix) && bucket.level < HashKey.LENGTH)
                    bucket.split(splittablePrefixes);
                else {
                    //Remove the just added node again, as this bucket is not splittable but has too many nodes
                    bucket.nodes.remove(node);
                    return false;
                }
            }

            return true;
        }
        else
            return false;
    }

    public boolean containsNode(RemoteNode node){
        return getResponsibleBucket(node.getNodeId()).nodes.contains(node);
    }

    private void split(HashKey splittablePrefixes){
        assert (childs.size() == 0);
        assert (nodes.size() > SIZE_LIMIT); //TODO: Can it also split before?

        BitSet zero = (BitSet) prefix.clone();
        zero.clear(this.level); //TODO: Off by one?
        assert (zero.length() <= HashKey.LENGTH);

        BitSet one = (BitSet) prefix.clone();
        one.set(this.level); //TODO: Off by one?
        assert (one.length() <= HashKey.LENGTH);

        childs.put(false, new Bucket(zero, this.level + 1));
        childs.put(true, new Bucket(one, this.level + 1));

        //Add all nodes
        nodes.forEach(e -> addNodeMaybe(e, splittablePrefixes));

        //ASSERTION TODO: REMOVE
        Bucket theOneThatDidTheSplit = this;
        nodes.forEach(node -> {
            Bucket responsibleBucket = getResponsibleBucket(node.getNodeId());
            assert(responsibleBucket.nodes.contains(node));
            assert(responsibleBucket != theOneThatDidTheSplit);
        });
        //ASSERTION END

        nodes.clear();
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
}
