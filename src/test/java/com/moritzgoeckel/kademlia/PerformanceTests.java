package com.moritzgoeckel.kademlia;

import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Random;
import java.util.function.Supplier;

public class PerformanceTests {

    private static Random R = new Random();

    @Test
    public void getNSetStatistics() throws IOException {
        int port = 10;

        final int NODESCOUNT = 10_000;
        final int MESSAGECOUNT = 1000;

        final int STORAGE = 20;
        final String path = "/home/moritz/sync/KademliaPlotting/";

        assert new File(path).exists();

        FileWriter stateWriter = new FileWriter(path + "/state.txt");
        FileWriter lookupWriter = new FileWriter(path + "/lookup.txt");
        FileWriter setWriter = new FileWriter(path + "/set.txt");

        LinkedList<Node> nodes = new LinkedList<>();

        Node firstNode = new Node(port++, R.nextDouble() + "", STORAGE, false, false);
        nodes.add(firstNode);

        for(int i = 0; i < NODESCOUNT; i++) {
            if(i % 1000 == 0)
                System.out.println("Adding nodes: " + i + "/" + NODESCOUNT);

            nodes.add(new Node(nodes.get(R.nextInt(nodes.size())), port++, R.nextDouble() + "", STORAGE, false, false));
        }

        Supplier<Node> randomNode = () -> nodes.get(R.nextInt(nodes.size() - 1));
        nodes.forEach(Node::performPing); //Maybe with ping?

        setWriter.write("Store\r\n");
        for(int i = 0; i < MESSAGECOUNT; i++) {
            System.out.println("Setting value: " + i + "/" + MESSAGECOUNT);

            Node.resetStatistics();
            randomNode.get().setValue("Hello" + i, "world", 10);
            setWriter.write(Node.getStatistics().getSum() + "\r\n");
        }

        lookupWriter.write("Lookup\n\n");
        int fails = 0;
        for(int i = 0; i < MESSAGECOUNT; i++) {
            if(i % 10 == 0)
                System.out.println("Getting value: " + i + "/" + MESSAGECOUNT);

            Node.resetStatistics();
            if (randomNode.get().getValue("Hello" + i, 50) == null)
                fails++;
            else
                lookupWriter.write(Node.getStatistics().getSum() + "\r\n");
        }

        System.out.println("Failed lookups: " + fails);

        stateWriter.write("State\r\n");
        nodes.forEach(node -> {
            try {
                stateWriter.write(node.getStateStatistics() + "\r\n");
            } catch (IOException e) {
                throw new RuntimeException();
            }
        });

        stateWriter.close();
        lookupWriter.close();
        setWriter.close();
    }

    //Todo: Get not found value test (performance?)
}
