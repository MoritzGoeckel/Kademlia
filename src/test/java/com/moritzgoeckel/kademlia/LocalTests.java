package com.moritzgoeckel.kademlia;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.*;

public class LocalTests {

    private static Random R = new Random();

    private static int PORT = -1;
    private static String ADDRESS = "http://localhost";

    @Test
    public void oneNodeTest(){
        final int K = 1;
        IKademliaNode firstNode = new Node(PORT, ADDRESS, 5, false, false);
        Assert.assertThat("Should still have the same addres",
                ((Node) firstNode).getAddress(),
                is(ADDRESS));

        firstNode.setValue("Hello", "world", K);

        Assert.assertThat("Should be able to retrieve the set value",
                firstNode.getValue("Hello", K),
                is("world"));
    }

    @Test
    public void twoNodesTest(){
        for(int i = 0; i < 300; i++) {
            Node firstNode = new Node(PORT++, ADDRESS, 5, false, false);
            Node secondNode = new Node(firstNode, PORT++, ADDRESS, 5, false, false);

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

        Node firstNode = new Node(PORT++, ADDRESS, 10, false, false);
        nodes.add(firstNode);

        for(int i = 0; i < 100; i++)
            nodes.add(new Node(nodes.get(R.nextInt(nodes.size())), PORT++, ADDRESS, 10, false, false));

        Supplier<Node> randomNode = () -> nodes.get(R.nextInt(nodes.size() - 1));
        nodes.forEach(Node::performPing);

        for(int i = 0; i < 100; i++) {
            Node from = randomNode.get();
            Node to = randomNode.get();

            from.setValue("Hello" + i, "world", 1);
            Assert.assertThat("Should be able to retrieve the set value ("+i+")",
                    to.getValue("Hello" + i, 30),
                    is("world"));
        }
    }

    @Test
    public void churnPutGetTest(){
        LinkedList<Node> nodes = new LinkedList<>();

        Node firstNode = new Node(PORT++, ADDRESS, 10, false, false);
        nodes.add(firstNode);

        Consumer<Integer> addNodes = num -> {
            for(int i = 0; i < num; i++)
                nodes.add(new Node(nodes.get(R.nextInt(nodes.size())), PORT++, ADDRESS, 10, false, false));
        };

        Consumer<Integer> removeNodes = num -> {
            for(int i = 0; i < num; i++) {
                int index = R.nextInt(nodes.size() - 1);
                nodes.get(index).shutdown();
                nodes.remove(index);
            }
        };

        Supplier<Node> randomNode = () -> nodes.get(R.nextInt(nodes.size() - 1));

        addNodes.accept(50);
        nodes.forEach(Node::performPing);

        for(int i = 0; i < 100; i++) {
            Node from = randomNode.get();

            from.setValue("Hello" + i, "world", 10);

            removeNodes.accept(5);
            Node to = randomNode.get();

            Assert.assertThat("Should be able to retrieve the set value ("+i+")",
                    to.getValue("Hello" + i, 30),
                    is("world"));

            addNodes.accept(5);
            nodes.forEach(Node::performPing);
        }
    }

    @Test
    public void initialChurnTest(){
        LinkedList<Node> nodes = new LinkedList<>();

        Node firstNode = new Node(PORT++, ADDRESS, 10, false, false);
        nodes.add(firstNode);

        Consumer<Integer> addNodes = num -> {
            for(int i = 0; i < num; i++)
                nodes.add(new Node(nodes.get(R.nextInt(nodes.size())), PORT++, ADDRESS, 10, false, false));
        };

        Consumer<Integer> removeNodes = num -> {
            for(int i = 0; i < num; i++) {
                int index = R.nextInt(nodes.size() - 1);
                nodes.get(index).shutdown();
                nodes.remove(index);
            }
        };

        Supplier<Node> randomNode = () -> nodes.get(R.nextInt(nodes.size() - 1));

        addNodes.accept(50);
        nodes.forEach(Node::performPing);

        for(int i = 0; i < 100; i++) {
            int num = R.nextInt(25);
            removeNodes.accept(num);
            addNodes.accept(num);

            nodes.forEach(Node::performPing);
        }

        for(int i = 0; i < 100; i++) {
            Node from = randomNode.get();
            Node to = randomNode.get();

            from.setValue("Hello" + i, "world", 5);
            Assert.assertThat("Should be able to retrieve the set value ("+i+")",
                    to.getValue("Hello" + i, 30),
                    is("world"));
        }
    }
}
