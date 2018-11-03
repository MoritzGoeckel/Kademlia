import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.*;

public class FirstTests {

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

    @Test
    public void bucketTest() throws MalformedURLException, InterruptedException {
        //Creating unique random RemoteNodes
        HashSet<RemoteNode> nodes = new HashSet<>();
        while (nodes.size() < 120)
            nodes.add(new RemoteNode(HashKey.fromRandom(), 10, new URL("http://localhost")));
        Iterator<RemoteNode> nodeSupplier = nodes.iterator();

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

        for(int i = 0; i < 100; i++){
            node = nodeSupplier.next();

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
                Assert.assertThat("There should be no doublicates", nodesInBuckets.add(n), is(true));
            });

            if(added)
                expectedSize++;

            Assert.assertThat("Length should be consistent",
                    b.getAllNodes().size(),
                    is(expectedSize));

            System.out.println(b.getAllNodes().size());
        }

    }

}
