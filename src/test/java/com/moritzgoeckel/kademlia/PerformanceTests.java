package com.moritzgoeckel.kademlia;

import com.moritzgoeckel.kademlia.Node;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Random;
import java.util.function.Supplier;

public class PerformanceTests {

    private static Random R = new Random();

    @Test
    public void getNSetStatistics(){
        LinkedList<Node> nodes = new LinkedList<>();

        int port = 10;

        final int NODESCOUNT = 10_000;
        final int MESSAGECOUNT = 500;

        Node firstNode = new Node(port++, "localhost", 5, false, false);
        nodes.add(firstNode);

        for(int i = 0; i < NODESCOUNT; i++) {
            if(i % 1000 == 0)
                System.out.println("Adding nodes: " + i + "/" + NODESCOUNT);

            nodes.add(new Node(nodes.get(R.nextInt(nodes.size())), port++, "localhost", 5, false, false));
        }

        Supplier<Node> randomNode = () -> nodes.get(R.nextInt(nodes.size() - 1));
        //nodes.forEach(Node::performPing); //Maybe with ping?

        Node.resetStatistics();

        for(int i = 0; i < MESSAGECOUNT; i++) {
            System.out.println("Setting value: " + i + "/" + MESSAGECOUNT);
            randomNode.get().setValue("Hello" + i, "world", 5);
        }

        System.out.println("Store");
        Node.getStatistics().print(MESSAGECOUNT, NODESCOUNT);
        Node.resetStatistics();

        int fails = 0;
        for(int i = 0; i < MESSAGECOUNT; i++) {
            if(i % 10 == 0)
                System.out.println("Getting value: " + i + "/" + MESSAGECOUNT);

            if (randomNode.get().getValue("Hello" + i, 50) == null)
                fails++;
        }

        System.out.println("Lookup");
        Node.getStatistics().print(MESSAGECOUNT, NODESCOUNT);
        System.out.println("Failed lookups: " + fails);
    }

    //Todo: Get not found value test (performance?)
    //TODO: Get statistics about the amount of stored nodes per bucket tree
}
