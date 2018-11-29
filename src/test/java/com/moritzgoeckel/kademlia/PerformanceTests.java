package com.moritzgoeckel.kademlia;

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
        final int MESSAGECOUNT = 1000;

        final int STORAGE = 5;

        Node firstNode = new Node(port++, "localhost", STORAGE, false, false);
        nodes.add(firstNode);

        for(int i = 0; i < NODESCOUNT; i++) {
            if(i % 1000 == 0)
                System.out.println("Adding nodes: " + i + "/" + NODESCOUNT);

            nodes.add(new Node(nodes.get(R.nextInt(nodes.size())), port++, "localhost", STORAGE, false, false));
        }

        Supplier<Node> randomNode = () -> nodes.get(R.nextInt(nodes.size() - 1));
        //nodes.forEach(Node::performPing); //Maybe with ping?

        System.out.println("Store");
        for(int i = 0; i < MESSAGECOUNT; i++) {
            //System.out.println("Setting value: " + i + "/" + MESSAGECOUNT);

            Node.resetStatistics();
            randomNode.get().setValue("Hello" + i, "world", 5);
            Node.getStatistics().printSum();
        }

        System.out.println("Lookup");
        int fails = 0;
        for(int i = 0; i < MESSAGECOUNT; i++) {
            //if(i % 10 == 0)
            //    System.out.println("Getting value: " + i + "/" + MESSAGECOUNT);

            Node.resetStatistics();
            if (randomNode.get().getValue("Hello" + i, 50) == null)
                fails++;
            else
                Node.getStatistics().printSum();
        }

        System.out.println("Failed lookups: " + fails);

        System.out.println("State");
        nodes.forEach(node -> System.out.println(node.getStateStatistics()));
    }

    //Todo: Get not found value test (performance?)
}
