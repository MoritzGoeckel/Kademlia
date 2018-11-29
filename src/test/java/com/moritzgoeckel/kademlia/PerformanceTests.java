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

        Node firstNode = new Node(port++, "localhost", 5, false, false);
        nodes.add(firstNode);

        for(int i = 0; i < 1000; i++)
            nodes.add(new Node(nodes.get(R.nextInt(nodes.size())), port++, "localhost", 5, false, false));

        Supplier<Node> randomNode = () -> nodes.get(R.nextInt(nodes.size() - 1));
        nodes.forEach(Node::performPing);

        Node.resetStatistics();

        for(int i = 0; i < 200; i++)
            randomNode.get().setValue("Hello" + i, "world", 5);

        System.out.println("Store");
        Node.getStatistics().print(200, 1000);
        Node.resetStatistics();

        int fails = 0;
        for(int i = 0; i < 200; i++)
            if(randomNode.get().getValue("Hello" + i, 50) == null)
                fails++;

        System.out.println("Lookup");
        Node.getStatistics().print(200, 1000);
        System.out.println("Failed lookups: " + fails);
    }

    //Todo: Get not found value test (performance?)
    //TODO: Get statistics about the amount of stored nodes per bucket tree
}
