package com.moritzgoeckel.kademlia;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.function.Supplier;

public class PerformanceTests {

    private static Random R = new Random();
    private final static String PATH = "/home/moritz/sync/KademliaPlotting/changed/";

    @Test
    public void getStatisticsWith100k() throws IOException {
        getNetworkStatistics(100_000);
    }

    @Test
    public void getStatisticsWith10k() throws IOException {
        getNetworkStatistics(10_000);
    }

    @Test
    public void getStatisticsWith5k() throws IOException {
        getNetworkStatistics(5000);
    }

    @Test
    public void getStatisticsWith1k() throws IOException {
        getNetworkStatistics(1000);
    }

    @Test
    public void getStatisticsWith500() throws IOException {
        getNetworkStatistics(500);
    }

    @Test
    public void getStatisticsWith100() throws IOException {
        getNetworkStatistics(100);
    }

    @Test
    public void getStatisticsWith10() throws IOException {
        getNetworkStatistics(10);
    }

    private void getNetworkStatistics(final int NODESCOUNT) throws IOException {
        int port = 10;

        final int MESSAGECOUNT = 3000;

        final int STORAGE = 25;

        assert new File(PATH).exists();

        FileWriter stateWriter = new FileWriter(PATH + "/state_"+NODESCOUNT+".txt");
        FileWriter lookupWriter = new FileWriter(PATH + "/lookup_"+NODESCOUNT+".txt");
        FileWriter setWriter = new FileWriter(PATH + "/set_"+NODESCOUNT+".txt");

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
            randomNode.get().setValue("" + i, "world", 10);
            setWriter.write(Node.getStatistics().getSum() + "\r\n");
        }

        lookupWriter.write("Lookup\n\n");
        int fails = 0;
        for(int i = 0; i < MESSAGECOUNT; i++) {
            if(i % 10 == 0)
                System.out.println("Getting value: " + i + "/" + MESSAGECOUNT);

            Node.resetStatistics();
            if (randomNode.get().getValue("" + i, 5) == null)
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

    //Todo: Get not found value test
}
