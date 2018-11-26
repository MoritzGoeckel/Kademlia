package com.moritzgoeckel.kademlia;

import com.moritzgoeckel.kademlia.Bucket;
import com.moritzgoeckel.kademlia.HashKey;
import com.moritzgoeckel.kademlia.INode;
import com.moritzgoeckel.kademlia.Node;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;

public class BucketTests {

    private static Random R = new Random();

    private Iterator<INode> getRandomUniqueNodes(int num) {
        //Creating unique random RemoteNodes
        HashSet<INode> nodes = new HashSet<>();
        while (nodes.size() < num)
            nodes.add(new Node(0, "http://localhost", 10, false, false));

        return nodes.iterator();
    }

    @Test
    public void bucketTest() {
        Iterator<INode> nodeSupplier = getRandomUniqueNodes(1200);

        Bucket b = new Bucket(5);
        HashKey splittableId = HashKey.fromRandom();

        INode node = nodeSupplier.next();
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

        INode possibleDoublicate = null;

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

            HashSet<INode> nodesInBuckets = new HashSet<>();
            b.getAllNodes().forEach(n -> Assert.assertThat("There should be no duplicates", nodesInBuckets.add(n), is(true)));

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

        HashSet<INode> nodesInBuckets = new HashSet<>();
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
    public void bucketRemoveNodeTest() throws MalformedURLException {
        Iterator<INode> nodeSupplier = getRandomUniqueNodes(1200);

        Bucket b = new Bucket(5);
        HashKey splittableId = HashKey.fromRandom();

        for(int i = 0; i < 1000; i++){
            INode node = nodeSupplier.next();

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

            HashSet<INode> nodesInBuckets = new HashSet<>();
            b.getAllNodes().forEach(n -> Assert.assertThat("There should be no duplicates", nodesInBuckets.add(n), is(true)));

            if(added)
                expectedSize++;

            Assert.assertThat("Length should be consistent",
                    b.getAllNodes().size(),
                    is(expectedSize));

            if(i % 8 == 0){
                List<INode> allNodes = b.getAllNodes();
                INode nodeToRemove = allNodes.get(R.nextInt(allNodes.size()));
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

}
