import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.*;

public class FirstTests {

    private static Random R = new Random();

    private static int PORT = -1;
    private static URL ADDRESS;
    static {
        try {
            ADDRESS = new URL("http://localhost");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
    }

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
                a.getDistance(b).equals(b.getDistance(a)),
                is(true));

        Assert.assertThat("distance is bigger than 0",
                a.getDistance(b).compareTo(new BigInteger("0")) > 0,
                is(true));

        Assert.assertThat("triangle inequality",
                a.getDistance(b).add(b.getDistance(c)).compareTo(a.getDistance(c)) >= 0,
                is(true));

        Assert.assertThat("HashKey equals should work", HashKey.fromString("Hello").equals(HashKey.fromString("Hello")), is(true));
    }

    public Iterator<RemoteNode> getRandomUniqueNodes(int num){
        //Creating unique random RemoteNodes
        HashSet<RemoteNode> nodes = new HashSet<>();
        while (nodes.size() < num)
            nodes.add(new RemoteNode(HashKey.fromRandom(), PORT, ADDRESS));

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
                RemoteNode nodeToRemove = allNodes.get(R.nextInt(allNodes.size()));
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
        KeyValuePair p = new KeyValuePair(HashKey.fromString("A"), "B");
        Assert.assertThat("Checking key", p.getKey(), is(HashKey.fromString("A")));
        Assert.assertThat("Checking value", p.getValue(), is("B"));
    }

    @Test
    public void remoteNodesOrKeyValuePairTests() throws MalformedURLException {
        RemoteNodesOrKeyValuePair a = new RemoteNodesOrKeyValuePair(new KeyValuePair(HashKey.fromString("A"), "B"));
        Assert.assertThat("Checking pair", a.getPair().getValue(), is("B"));
        Assert.assertThat("Checking nodes", a.getRemoteNodes(), nullValue());


        RemoteNodesOrKeyValuePair b = new RemoteNodesOrKeyValuePair(new RemoteNode[]{
                new RemoteNode(HashKey.fromRandom(), PORT, ADDRESS),
                new RemoteNode(HashKey.fromRandom(), PORT, ADDRESS)
        });
        Assert.assertThat("Checking pair", b.getPair(), nullValue());
        Assert.assertThat("Checking nodes", b.getRemoteNodes().length, is(2));
    }

    @Test
    public void oneNodeTest(){
        final int K = 1;
        IUserNode firstNode = new Node(PORT, ADDRESS);

        firstNode.setValue("Hello", "world", K);

        Assert.assertThat("Should be able to retrieve the set value",
                firstNode.getValue("Hello", K),
                is("world"));
    }

    @Test
    public void twoNodesTest(){
        for(int i = 0; i < 300; i++) {
            Node firstNode = new Node(PORT, ADDRESS);
            Node secondNode = new Node(new RemoteNodeLocal(firstNode, PORT, ADDRESS), PORT, ADDRESS);

            firstNode.performPing();
            secondNode.performPing();

            secondNode.setValue("Hello", "world", 1);

            Assert.assertThat("Should be able to retrieve the set value",
                    firstNode.getValue("Hello", 1),
                    is("world"));

            Assert.assertThat("Should be able to retrieve the set value",
                    secondNode.getValue("Hello", 1),
                    is("world"));
        }
    }

    @Test
    public void manyNodesTest(){
        LinkedList<Node> nodes = new LinkedList<>();

        Node firstNode = new Node(PORT, ADDRESS);
        nodes.add(firstNode);

        for(int i = 0; i < 100; i++)
            nodes.add(new Node(new RemoteNodeLocal(nodes.get(R.nextInt(nodes.size())), PORT, ADDRESS), PORT, ADDRESS));

        Supplier<Node> randomNode = () -> nodes.get(R.nextInt(nodes.size() - 1));
        nodes.forEach(Node::performPing);

        for(int i = 0; i < 100; i++) {
            randomNode.get().setValue("Hello" + i, "world", 1);
            Assert.assertThat("Should be able to retrieve the set value ("+i+")",
                    randomNode.get().getValue("Hello" + i, 5),
                    is("world"));
        }
    }

    //Todo: Create statistics
    //Todo: Create churn tests
}
