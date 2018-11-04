import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;

public class FirstTests {

    private Random r = new Random();

    @Test
    public void hashKeyTest(){
        Assert.assertThat("a and be are different", !HashKey.fromRandom().equals(HashKey.fromRandom()), is(true));

        Assert.assertThat("these are the same",
                HashKey.fromString("abc").equals(HashKey.fromString("abc")),
                is(true));

        Assert.assertThat("these are the different",
                !HashKey.fromString("abc").equals(HashKey.fromString("abcd")),
                is(true));

        HashKey a = HashKey.fromString("abc");
        HashKey b = HashKey.fromString("abcd");
        HashKey c = HashKey.fromString("abcd");

        Assert.assertThat("distance is the same both ways",
                a.getDistance(b) == b.getDistance(a),
                is(true));

        Assert.assertThat("distance is bigger than 0",
                a.getDistance(b) > 0,
                is(true));

        Assert.assertThat("triangle inequality",
                a.getDistance(b) + b.getDistance(c) >= a.getDistance(c),
                is(true));
    }

    public Iterator<RemoteNode> getRandomUniqueNodes(int num){
        //Creating unique random RemoteNodes
        HashSet<RemoteNode> nodes = new HashSet<>();
        while (nodes.size() < num) {
            try {
                nodes.add(new RemoteNode(HashKey.fromRandom(), 10, new URL("http://localhost")));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Maleformed url: " + e.toString());
            }
        }
        return nodes.iterator();
    }

    @Test
    public void bucketTest() throws MalformedURLException, InterruptedException {
        Iterator<RemoteNode> nodeSupplier = getRandomUniqueNodes(1200);

        Bucket b = new Bucket();
        HashKey splittableId = HashKey.fromRandom();

        RemoteNode node = nodeSupplier.next();
        b.addNodeMaybe(node, splittableId);

        //Applies only for first elements

        Assert.assertThat("should be in root container now",
                b.getResponsibleBucket(node.getNodeId()),
                is(b));

        Assert.assertThat("root prefix is an empty one",
                b.getPrefix(),
                is(new BitSet()));

        Assert.assertThat("random address should point to bucket with node, as there is only one bucket",
                b.getNodesFromResponsibleBucket(HashKey.fromRandom()).contains(node),
                is(true));

        Assert.assertThat("is contains should return true now",
                b.containsNode(node),
                is(true));

        Assert.assertThat("should be in node set now",
                b.getAllNodes().contains(node),
                is(true));

        Assert.assertThat("node address should point to bucket with node",
                b.getNodesFromResponsibleBucket(node.getNodeId()).contains(node),
                is(true));

        //Applies for all elements

        RemoteNode possibleDoublicate = null;

        for(int i = 0; i < 1000; i++){
            node = nodeSupplier.next();

            int expectedSize = b.getAllNodes().size();
            boolean added = b.addNodeMaybe(node, splittableId);

            if(added)
                possibleDoublicate = node;

            Assert.assertThat("is contains should return true now",
                    b.containsNode(node),
                    is(added));

            Assert.assertThat("should be in node set now",
                    b.getAllNodes().contains(node),
                    is(added));

            Assert.assertThat("node address should point to bucket with node",
                    b.getNodesFromResponsibleBucket(node.getNodeId()).contains(node),
                    is(added));

            HashSet<RemoteNode> nodesInBuckets = new HashSet<>();
            b.getAllNodes().forEach(n -> {
                Assert.assertThat("There should be no duplicates", nodesInBuckets.add(n), is(true));
            });

            if(added)
                expectedSize++;

            Assert.assertThat("Length should be consistent",
                    b.getAllNodes().size(),
                    is(expectedSize));
        }

        //Checking for duplicates

        //Readding the same node
        node = possibleDoublicate;
        int expectedSize = b.getAllNodes().size();
        boolean added = b.addNodeMaybe(node, splittableId);

        Assert.assertThat("Duplicates should not have been added", added, is(false));

        Assert.assertThat("is contains should return true anyways (was already)",
                b.containsNode(node),
                is(true));

        Assert.assertThat("should be in node set anyways (was already)",
                b.getAllNodes().contains(node),
                is(true));

        Assert.assertThat("node address should point to bucket with node anyways (was already)",
                b.getNodesFromResponsibleBucket(node.getNodeId()).contains(node),
                is(true));

        HashSet<RemoteNode> nodesInBuckets = new HashSet<>();
        b.getAllNodes().forEach(n -> {
            Assert.assertThat("There should be no duplicates", nodesInBuckets.add(n), is(true));
        });

        Assert.assertThat("Length should be consistent",
                b.getAllNodes().size(),
                is(expectedSize));

        Assert.assertThat("Have some buckets", b.getBucketCount() > 10, is(true));
        Assert.assertThat("Many of them should not be saved",b.getAllNodes().size() < 1000, is(true));
        Assert.assertThat("Some of them should be saved",b.getAllNodes().size() > 40, is(true));

        //Todo: Express in an assertion that only one child of any bucket can have childs
    }

    @Test
    public void bucketRemoveNodeTest(){
        Iterator<RemoteNode> nodeSupplier = getRandomUniqueNodes(1200);

        Bucket b = new Bucket();
        HashKey splittableId = HashKey.fromRandom();

        for(int i = 0; i < 1000; i++){
            RemoteNode node = nodeSupplier.next();

            int expectedSize = b.getAllNodes().size();
            boolean added = b.addNodeMaybe(node, splittableId);

            Assert.assertThat("is contains should return true now",
                    b.containsNode(node),
                    is(added));

            Assert.assertThat("should be in node set now",
                    b.getAllNodes().contains(node),
                    is(added));

            Assert.assertThat("node address should point to bucket with node",
                    b.getNodesFromResponsibleBucket(node.getNodeId()).contains(node),
                    is(added));

            HashSet<RemoteNode> nodesInBuckets = new HashSet<>();
            b.getAllNodes().forEach(n -> {
                Assert.assertThat("There should be no duplicates", nodesInBuckets.add(n), is(true));
            });

            if(added)
                expectedSize++;

            Assert.assertThat("Length should be consistent",
                    b.getAllNodes().size(),
                    is(expectedSize));

            if(i % 8 == 0){
                List<RemoteNode> allNodes = b.getAllNodes();
                RemoteNode nodeToRemove = allNodes.get(r.nextInt(allNodes.size()));
                b.removeNode(nodeToRemove);
                expectedSize--;

                Assert.assertThat("Length should be consistent",
                        b.getAllNodes().size(),
                        is(expectedSize));

                Assert.assertThat("is contains should return false now",
                        b.containsNode(nodeToRemove),
                        is(false));

                Assert.assertThat("node address should point to bucket without the node",
                        b.getNodesFromResponsibleBucket(nodeToRemove.getNodeId()).contains(nodeToRemove),
                        is(false));
            }
        }
    }

    @Test
    public void keyValuePairTests(){
        KeyValuePair p = new KeyValuePair("A", "B");
        Assert.assertThat("Checking key", p.getKey(), is("A"));
        Assert.assertThat("Checking key", p.getValue(), is("B"));
    }

    @Test
    public void remoteNodesOrKeyValuePairTests() throws MalformedURLException {
        RemoteNodesOrKeyValuePair a = new RemoteNodesOrKeyValuePair(new KeyValuePair("A", "B"));
        Assert.assertThat("Checking pair", a.getPair().getValue(), is("B"));
        Assert.assertThat("Checking nodes", a.getRemoteNodes(), nullValue());


        RemoteNodesOrKeyValuePair b = new RemoteNodesOrKeyValuePair(new RemoteNode[]{
                new RemoteNode(HashKey.fromRandom(), 10, new URL("http://localhost")),
                new RemoteNode(HashKey.fromRandom(), 10, new URL("http://localhost"))
        });
        Assert.assertThat("Checking pair", b.getPair(), nullValue());
        Assert.assertThat("Checking nodes", b.getRemoteNodes().length, is(2));
    }

}
